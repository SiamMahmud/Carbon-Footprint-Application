package com.example.carbonfootprintcalculation.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.dashboard.model.CarResult
import com.example.carbonfootprintcalculation.dashboard.model.FoodResult
import com.example.carbonfootprintcalculation.dashboard.model.PublicTransportResult
import com.example.carbonfootprintcalculation.databinding.FragmentFoodResultBinding
import com.example.carbonfootprintcalculation.databinding.FragmentViewPublicTransportResultBinding
import com.example.carbonfootprintcalculation.presentation.adapter.FoodAdapter
import com.example.carbonfootprintcalculation.presentation.adapter.PublicTransportAdapter
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class FoodResultFragment : Fragment() {

    private lateinit var binding: FragmentFoodResultBinding
    private lateinit var adapter: FoodAdapter

    @Inject
    lateinit var activityUtil: SMMActivityUtil
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_food_result, container, false)
        binding.model = this

        activityUtil.hideBottomNavigation(true)

        binding.backIv.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.foodResultListRecycle.layoutManager = LinearLayoutManager(activity)
        adapter = FoodAdapter(mutableListOf())
        binding.foodResultListRecycle.adapter = adapter

        fetchCarData()

        return binding.root
    }

    private fun fetchCarData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            Log.e("Firebase", "User ID is null")
            return
        }

        val foodRef = FirebaseDatabase.getInstance()
            .getReference("User")
            .child(userId)
            .child("FoodCarbonFootprint")

        Log.d("Firebase", "Reading data from: User/$userId/FoodCarbonFootprint")

        foodRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val foodResults = mutableListOf<FoodResult>()
                var totalCarbonEmission = 0.0

                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val foodType = snap.child("foodType").getValue(String::class.java) ?: ""
                        val quantity = snap.child("quantity").getValue(Double::class.java) ?: 0.0
                        val emission = snap.child("emission").getValue(Double::class.java) ?: 0.0

                        val result = FoodResult(
                            foodType = foodType,
                            quantity = quantity,
                            emission = emission
                        )

                        Log.d("FoodData", "Loaded item: $result")
                        foodResults.add(result)
                        totalCarbonEmission +=emission
                    }

                    adapter.updateData(foodResults)
                    binding.totalEmissionTv.text ="Total Carbon Emission: $totalCarbonEmission kg CO₂"
                    Toast.makeText(requireContext(), "Data loaded: ${foodResults.size} items", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("Firebase", "No data found in FoodCarbonFootprint")
                    Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



}