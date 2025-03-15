package com.example.carbonfootprintcalculation.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.databinding.FragmentPublicTransportBinding
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class PublicTransportFragment : Fragment() {
    @Inject
    lateinit var activityUtil: SMMActivityUtil
    private lateinit var binding: FragmentPublicTransportBinding
    val actionTransportResult = Navigation.createNavigateOnClickListener(R.id.action_publicTransportFragment_to_viewPublicTransportResultFragment)

    private val emissionFactors = mapOf(
        "Local Bus" to 0.089,
        "Coach" to 0.053,
        "MRT" to 0.035,
        "Intercity Train" to 0.041
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_public_transport, container, false)
        binding.model = this

        setupDropdowns()
        setupListeners()

        binding.selectLocationButton.setOnClickListener { openMapActivity() }
        binding.resultButton.setOnClickListener { calculateEmission() }

        activityUtil.hideBottomNavigation(true)

        return binding.root
    }

    private fun setupDropdowns() {
        val busTypes = arrayOf("Select Bus Type", "Local Bus", "Coach")
        val trainTypes = arrayOf("Select Train Type", "MRT", "Intercity Train")

        val busAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, busTypes)
        val trainAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, trainTypes)

        binding.busTypeSpinner.adapter = busAdapter
        binding.trainTypeSpinner.adapter = trainAdapter
    }

    private fun setupListeners() {
        binding.busTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedBus = parent?.getItemAtPosition(position).toString()
                binding.trainTypeSpinner.isEnabled = selectedBus == "Select Bus Type"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.trainTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTrain = parent?.getItemAtPosition(position).toString()
                binding.busTypeSpinner.isEnabled = selectedTrain == "Select Train Type"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun openMapActivity() {
        val intent = Intent(requireContext(), MapActivity::class.java)
        startActivityForResult(intent, 1002)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002 && resultCode == Activity.RESULT_OK) {
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
        val busType = binding.busTypeSpinner.selectedItem?.toString()
        val trainType = binding.trainTypeSpinner.selectedItem?.toString()
        val distanceText = binding.distanceField.text.toString()

        if (distanceText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter travel distance", Toast.LENGTH_SHORT).show()
            return
        }

        val distance = distanceText.toDouble()
        var selectedTransport: String? = null

        // Determine selected transport type
        if (busType != "Select Bus Type") {
            selectedTransport = busType
        } else if (trainType != "Select Train Type") {
            selectedTransport = trainType
        }

        if (selectedTransport == null) {
            Toast.makeText(requireContext(), "Please select a transport type", Toast.LENGTH_SHORT).show()
            return
        }

        val emissionFactor = emissionFactors[selectedTransport] ?: 0.0
        val emissionRate = emissionFactor * distance

        binding.emissionResult.text = "Your emission rate: %.2f kg CO₂".format(emissionRate)


        saveFirebase(selectedTransport, distance, emissionRate)
    }


    private fun saveFirebase(transportType: String, distance: Double, emissionRate: Double) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        if (uid == null) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val carbonData = mapOf(
            "transportType" to transportType,
            "distance" to distance,
            "emissionRate" to emissionRate,
            "timestamp" to timestamp
        )

        val userRef = Firebase.database.reference.child("User").child(uid).child("PublicTransportCarbonFootprintRecords").push()
        userRef.setValue(carbonData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Carbon footprint data saved successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save data", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
