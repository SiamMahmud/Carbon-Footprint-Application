package com.example.carbonfootprintcalculation.dashboard

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import android.graphics.Paint
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.databinding.FragmentCarBinding
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import androidx.core.content.FileProvider
import android.net.Uri
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CarFragment : Fragment() {
    @Inject
    lateinit var activityUtil: SMMActivityUtil
    private lateinit var binding: FragmentCarBinding
    lateinit var database: DatabaseReference
    val actionCarResult = Navigation.createNavigateOnClickListener(R.id.action_carFragment_to_viewCarResultFragment)

    private val emissionFactors = mapOf(
        "Petrol_1000cc" to 0.108,
        "Petrol_1500cc" to 0.110,
        "Petrol_2000cc" to 0.136,
        "Diesel_1000cc" to 0.096,
        "Diesel_1500cc" to 0.095,
        "Diesel_2000cc" to 0.117,
        "Electric" to 0.041
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_car, container, false)
        binding.model = this
        setupDropdowns()
        binding.selectLocationButton.setOnClickListener { openMapActivity() }
        binding.resultButton.setOnClickListener { calculateEmission() }
        activityUtil.hideBottomNavigation(true)
        return binding.root
    }

    private fun setupDropdowns() {
        val carTypes = arrayOf("1000cc", "1500cc", "2000cc")
        val fuelTypes = arrayOf("Petrol", "Diesel", "Electric")

        binding.carTypeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, carTypes)
        binding.fuelTypeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, fuelTypes)
    }

    private fun openMapActivity() {
        val intent = Intent(requireContext(), MapActivity::class.java)
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val distance = data?.getStringExtra("DISTANCE")?.trim()

            if (!distance.isNullOrEmpty()) {
                binding.distanceResult.text = "Selected distance: $distance km"
                binding.distanceField.setText(distance)
            } else {
                binding.distanceResult.text = "No distance selected"
            }
        }
    }

    private fun calculateEmission() {
        val carType = binding.carTypeSpinner.selectedItem.toString()
        val fuelType = binding.fuelTypeSpinner.selectedItem.toString()
        val distanceText = binding.distanceField.text.toString()

        if (distanceText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter travel distance", Toast.LENGTH_SHORT).show()
            return
        }

        val distance = distanceText.toDouble()
        val key = if (fuelType == "Electric") "Electric" else "${fuelType}_$carType"
        val emissionFactor = emissionFactors[key] ?: 0.0
        val emissionRate = emissionFactor * distance
        binding.emissionResult.text = "Your emission rate: %.2f kg CO₂".format(emissionRate)

        saveToFirebase(carType, fuelType, distance, emissionRate)

        binding.viewReportButton.visibility = View.GONE
        database = Firebase.database.reference
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        database = FirebaseDatabase.getInstance().getReference("User").child(uid)
        database.get().addOnSuccessListener {
            if (it.hasChild("Admin")) {
                binding.viewReportButton.visibility = View.VISIBLE
                val pdfUri = generatePDFReport(carType, fuelType, distance, emissionRate)

                if (pdfUri != null) {
                    binding.viewReportButton.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(pdfUri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                }

            }  else if (it.hasChild("User")) {
                binding.viewReportButton.visibility = View.GONE

            }
        }
    }

    private fun saveToFirebase(carType: String, fuelType: String, distance: Double, emissionRate: Double) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())


        val data = mapOf(
            "carType" to carType,
            "fuelType" to fuelType,
            "distance" to distance,
            "emissionRate" to emissionRate,
            "timestamp" to timestamp
        )

        val userRef = Firebase.database.reference.child("User").child(uid).child("carCarbonFootprint").push()
        userRef.setValue(data).addOnSuccessListener {
            Toast.makeText(requireContext(), "Data saved successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to save data", Toast.LENGTH_SHORT).show()
        }
    }



    private fun generatePDFReport(carType: String, fuelType: String, distance: Double, emissionRate: Double): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.textSize = 18f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Car Carbon Footprint Report", 50f, 50f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Car Type: $carType", 50f, 100f, paint)
        canvas.drawText("Fuel Type: $fuelType", 50f, 130f, paint)
        canvas.drawText("Travel Distance: $distance km", 50f, 160f, paint)
        canvas.drawText("Emission Rate: %.2f kg CO₂".format(emissionRate), 50f, 190f, paint)

        pdfDocument.finishPage(page)

        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Car_Carbon_Report.pdf")

        return try {
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            // Return file URI using FileProvider
            FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)

        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

}