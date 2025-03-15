package com.example.carbonfootprintcalculation.dashboard

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.databinding.FragmentFoodBinding
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.Interpreter
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class FoodFragment : Fragment() {

    @Inject
    lateinit var activityUtil: SMMActivityUtil
    private lateinit var binding: FragmentFoodBinding
    private val REQUEST_IMAGE_PICK = 1
    private val REQUEST_IMAGE_CAPTURE = 2
    private var selectedImageUri: Uri? = null
    private lateinit var tflite: Interpreter
    private lateinit var labels: List<String>
    private val imageSize = 224
    private var identifiedFoodType: String? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_food, container, false)
        activityUtil.hideBottomNavigation(true)

        binding.viewResultPage.setOnClickListener {
            findNavController().navigate(R.id.action_foodFragment_to_foodResultFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadModelAndLabels()

        binding.imageInputButton.setOnClickListener {
            showImageSelectionDialog()
        }

        binding.foodIdentify.setOnClickListener {
            selectedImageUri?.let { uri ->
                analyzeImage(uri)
            } ?: run {
                binding.foodTypeField.text = "Please select an image first"
            }
        }

        binding.calculateButton.setOnClickListener {
            calculateCarbonFootprint()
        }
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Image")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> captureImageFromCamera()
                1 -> pickImageFromGallery()
            }
        }
        builder.show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun captureImageFromCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        }
        selectedImageUri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    selectedImageUri = data?.data
                    selectedImageUri?.let {
                        Glide.with(this).load(it).into(binding.imageInputButton)
                        binding.foodTypeField.text = "Image Selected"
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                    selectedImageUri?.let {
                        Glide.with(this).load(it).into(binding.imageInputButton)
                        binding.foodTypeField.text = "Image Captured"
                    }
                }
            }
        }
    }

    private fun loadModelAndLabels() {
        try {
            val assetManager = requireContext().assets

            val fileDescriptor = assetManager.openFd("model_unquant.tflite")
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            tflite = Interpreter(modelBuffer)

            val labelsStream = assetManager.open("labels.txt")
            labels = labelsStream.bufferedReader().use { it.readLines() }

        } catch (e: IOException) {
            binding.foodTypeField.text = "Failed to load model or labels"
        }
    }

    private fun analyzeImage(imageUri: Uri) {
        try {
            val imageBitmap = decodeAndResizeBitmap(imageUri)
            val inputBuffer = convertBitmapToByteBuffer(imageBitmap)
            val outputArray = Array(1) { FloatArray(labels.size) }

            tflite.run(inputBuffer, outputArray)

            val predictedIndex = outputArray[0].indices.maxByOrNull { outputArray[0][it] } ?: -1
            identifiedFoodType = labels.getOrNull(predictedIndex) ?: "Unknown"

            binding.foodTypeField.text = "Food Type: $identifiedFoodType"

        } catch (e: Exception) {
            binding.foodTypeField.text = "Failed to process image"
        }
    }

    private fun decodeAndResizeBitmap(imageUri: Uri): Bitmap {
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        return Bitmap.createScaledBitmap(originalBitmap!!, imageSize, imageSize, true)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(imageSize * imageSize)
        bitmap.getPixels(intValues, 0, imageSize, 0, 0, imageSize, imageSize)

        for (pixelValue in intValues) {
            byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
            byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }
        return byteBuffer
    }

    private fun calculateCarbonFootprint() {
        val quantity = binding.quantityField.text.toString().toFloatOrNull()
        if (quantity == null || identifiedFoodType == null) {
            Toast.makeText(requireContext(), "Please enter quantity and identify food type", Toast.LENGTH_SHORT).show()
            return
        }

        val emissionFactors = mapOf(
            "Meat" to 0.027f,
            "Plant-Based" to 0.005f,
            "Processed Food" to 0.012f
        )

        val emissionFactor = emissionFactors[identifiedFoodType] ?: 0.0f
        val carbonFootprint = quantity * emissionFactor
        binding.successMessage.text = "Your food emission rate: %.2f kg CO₂".format(carbonFootprint)

        saveFirebases(identifiedFoodType!!, quantity, carbonFootprint)
    }

    private fun saveFirebases(identifiedFoodType: String, quantity: Float, carbonFootprint: Float) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val foodData = mapOf(
            "foodType" to identifiedFoodType,
            "quantity" to quantity,
            "emission" to carbonFootprint
        )

        val userRef = Firebase.database.reference.child("User").child(uid).child("FoodCarbonFootprint").push()
        userRef.setValue(foodData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Carbon footprint data saved successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save data", Toast.LENGTH_SHORT).show()
            }
        }

    }

}