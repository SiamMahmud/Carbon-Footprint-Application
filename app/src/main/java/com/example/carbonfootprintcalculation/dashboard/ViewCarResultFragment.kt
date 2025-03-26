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
import com.example.carbonfootprintcalculation.databinding.FragmentViewCarResultBinding
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.example.carbonfootprintcalculation.presentation.adapter.CarAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ViewCarResultFragment : Fragment() {

    private lateinit var binding: FragmentViewCarResultBinding
    private lateinit var adapter: CarAdapter

    @Inject
    lateinit var activityUtil: SMMActivityUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_car_result, container, false)
        binding.model = this

        activityUtil.hideBottomNavigation(true)

        binding.backIv.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.carResultListRecycle.layoutManager = LinearLayoutManager(activity)
        adapter = CarAdapter(mutableListOf())
        binding.carResultListRecycle.adapter = adapter

        fetchCarData()

        return binding.root
    }

    private fun fetchCarData() {
        val userId = arguments?.getString("id") ?: FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            Log.e("Firebase", "User ID is null")
            return
        }

        val carRef = FirebaseDatabase.getInstance()
            .getReference("User")
            .child(userId)
            .child("carCarbonFootprint")

        Log.d("Firebase", "Reading data from: User/$userId/carCarbonFootprint")

        carRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val carResults = mutableListOf<CarResult>()
                var totalEmission = 0.0

                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val carType = snap.child("carType").getValue(String::class.java) ?: ""
                        val fuelType = snap.child("fuelType").getValue(String::class.java) ?: ""
                        val distance = snap.child("distance").getValue(Double::class.java) ?: 0.0
                        val emissionRate = snap.child("emissionRate").getValue(Double::class.java) ?: 0.0
                        val timestamp = snap.child("timestamp").getValue(String::class.java) ?: ""

                        val result = CarResult(
                            carType = carType,
                            fuelType = fuelType,
                            distance = distance,
                            emissionRate = emissionRate,
                            timestamp = timestamp
                        )

                        Log.d("CarData", "Loaded item: $result")
                        carResults.add(result)
                        totalEmission += emissionRate
                    }

                    adapter.updateData(carResults)

                    binding.totalEmissionTv.text = "Total Carbon Emission: $totalEmission kg CO₂"
                    Toast.makeText(requireContext(), "Data loaded: ${carResults.size} items", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("Firebase", "No data found in carCarbonFootprint")
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
