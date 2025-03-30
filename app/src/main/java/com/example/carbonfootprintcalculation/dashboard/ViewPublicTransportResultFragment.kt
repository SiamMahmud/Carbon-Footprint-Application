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
import com.example.carbonfootprintcalculation.dashboard.model.PublicTransportResult
import com.example.carbonfootprintcalculation.databinding.FragmentViewCarResultBinding
import com.example.carbonfootprintcalculation.databinding.FragmentViewPublicTransportResultBinding
import com.example.carbonfootprintcalculation.presentation.adapter.CarAdapter
import com.example.carbonfootprintcalculation.presentation.adapter.PublicTransportAdapter
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ViewPublicTransportResultFragment : Fragment() {
    private lateinit var binding: FragmentViewPublicTransportResultBinding
    private lateinit var adapter: PublicTransportAdapter
    private lateinit var lineChart: LineChart

    @Inject
    lateinit var activityUtil: SMMActivityUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_public_transport_result, container, false)
        binding.model = this

        activityUtil.hideBottomNavigation(true)

        binding.backIv.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.transportResultListRecycle.layoutManager = LinearLayoutManager(activity)
        adapter = PublicTransportAdapter(mutableListOf())
        binding.transportResultListRecycle.adapter = adapter

        lineChart = binding.lineChart
        setupLineChart()

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

        val transRef = FirebaseDatabase.getInstance()
            .getReference("User")
            .child(userId)
            .child("PublicTransportCarbonFootprintRecords")

        Log.d("Firebase", "Reading data from: User/$userId/PublicTransportCarbonFootprintRecords")

        transRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transportResults = mutableListOf<PublicTransportResult>()
                val emissionData = mutableListOf<Entry>()
                if (snapshot.exists()) {
                    var index = 0
                    for (snap in snapshot.children) {
                        val transportType = snap.child("transportType").getValue(String::class.java) ?: ""
                        val distance = snap.child("distance").getValue(Double::class.java) ?: 0.0
                        val emissionRate = snap.child("emissionRate").getValue(Double::class.java) ?: 0.0
                        val timestamp = snap.child("timestamp").getValue(String::class.java) ?: ""

                        val result = PublicTransportResult(
                            transportType = transportType,
                            distance = distance,
                            emissionRate = emissionRate,
                            timestamp = timestamp
                        )

                        Log.d("TransportData", "Loaded item: $result")
                        transportResults.add(result)
                        emissionData.add(Entry(index.toFloat(), emissionRate.toFloat()))
                        index++
                    }
                    adapter.updateData(transportResults)

                    val totalEmission = emissionData.sumOf { it.y.toDouble() }
                    binding.totalEmissionTv.text = "Total Carbon Emission: %.2f kg CO₂".format(totalEmission)

                    updateLineChart(emissionData)

                    Toast.makeText(requireContext(), "Data loaded: ${transportResults.size} items", Toast.LENGTH_SHORT).show()
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

    private fun setupLineChart() {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)

        val rightAxis = lineChart.axisRight
        rightAxis.isEnabled = false
    }

    private fun updateLineChart(entries: List<Entry>) {
        val dataSet = LineDataSet(entries, "Carbon Emission (kg CO₂)")
        dataSet.color = resources.getColor(R.color.colorPrimary, null)
        dataSet.valueTextColor = resources.getColor(R.color.black, null)
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(resources.getColor(R.color.hint_text_color, null))
        dataSet.circleRadius = 4f

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate()
    }

}