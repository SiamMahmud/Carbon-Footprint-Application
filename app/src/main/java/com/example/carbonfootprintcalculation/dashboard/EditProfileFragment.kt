package com.example.carbonfootprintcalculation.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.databinding.FragmentEditProfileBinding
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    @Inject
    lateinit var activityUtil: SMMActivityUtil
    private lateinit var binding: FragmentEditProfileBinding
    private var uid = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var database: DatabaseReference

    private val engineSizes = arrayOf("Select Engine Size", "1000cc", "1500cc", "2000cc")
    private val fuelTypes = arrayOf("Select Fuel Type", "Petrol", "Diesel", "Electric")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)
        binding.model = this
        activityUtil.hideBottomNavigation(true)

        setupSpinners()
        showToData()

        binding.saveTv.setOnClickListener {
            val selectedEngineSize = binding.spEngineSize.selectedItem.toString()
            val selectedFuelType = binding.spFuelType.selectedItem.toString()

            if (selectedEngineSize == "Select Engine Size" || selectedFuelType == "Select Fuel Type") {
                Toast.makeText(activity, "Please select both engine size and fuel type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateData(
                binding.fullNameEt.text.toString().trim(),
                binding.phoneNumberEt.text.toString().trim(),
                binding.pEmailEt.text.toString().trim(),
                selectedEngineSize,
                selectedFuelType
            )
        }

        return binding.root
    }

    private fun setupSpinners() {
        val engineAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, engineSizes)
        engineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spEngineSize.adapter = engineAdapter

        val fuelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fuelTypes)
        fuelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spFuelType.adapter = fuelAdapter
    }

    private fun updateData(name: String, phone: String, email: String, engineSize: String, fuelType: String) {
        activityUtil.setFullScreenLoading(true)
        if (name.isNotEmpty() && phone.isNotEmpty()) {
            database = FirebaseDatabase.getInstance().getReference("User")
            val user = hashMapOf<String, Any>(
                "name" to name,
                "number" to phone,
                "email" to email,
                "engineSize" to engineSize,
                "fuelType" to fuelType
            )
            database.child(uid).updateChildren(user).addOnSuccessListener {
                findNavController().navigate(R.id.action_editProfileFragment_to_homeFragment)
                activityUtil.setFullScreenLoading(false)
                binding.fullNameEt.text!!.clear()
                binding.phoneNumberEt.text!!.clear()
                binding.pEmailEt.text!!.clear()
                Toast.makeText(activity, getString(R.string.update_massage), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showToData() {
        activityUtil.setFullScreenLoading(true)
        database = FirebaseDatabase.getInstance().reference
        database.child("User").child(uid).get().addOnSuccessListener {
            activityUtil.setFullScreenLoading(false)
            binding.fullNameEt.setText(it.child("name").value.toString())
            binding.phoneNumberEt.setText(it.child("number").value.toString())
            binding.pEmailEt.setText(it.child("email").value.toString())

            val engineSize = it.child("engineSize").value?.toString() ?: ""
            val fuelType = it.child("fuelType").value?.toString() ?: ""

            binding.spEngineSize.setSelection(if (engineSize in engineSizes) engineSizes.indexOf(engineSize) else 0)
            binding.spFuelType.setSelection(if (fuelType in fuelTypes) fuelTypes.indexOf(fuelType) else 0)
        }
    }
}
