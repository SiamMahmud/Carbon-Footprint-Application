package com.example.carbonfootprintcalculation.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.databinding.FragmentHomeBinding
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    lateinit var database: DatabaseReference
    @Inject
    lateinit var activityUtil : SMMActivityUtil
    val actionCar = Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_carFragment)
    val actionFood = Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_foodFragment)
    val actionTransport = Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_publicTransportFragment)
    val actionEditProfile = Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_editProfileFragment)
    val actionAdminUserView = Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_adminUserViewFragment)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.model = this
        activityUtil.hideBottomNavigation(false)

        binding.carCv.visibility = View.GONE
        binding.publicTransCV.visibility = View.GONE
        binding.foodCv.visibility = View.GONE
        binding.setProfileCv.visibility = View.GONE
        binding.adminUserViewCv.visibility = View.GONE

        activityUtil.setFullScreenLoading(true)
        database = Firebase.database.reference
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        database = FirebaseDatabase.getInstance().getReference("User").child(uid)
        activityUtil.setFullScreenLoading(false)
        database.get().addOnSuccessListener {
            if (it.hasChild("Admin")) {
                binding.carCv.visibility = View.VISIBLE
                binding.adminUserViewCv.visibility = View.VISIBLE
            }  else if (it.hasChild("User")) {
                binding.carCv.visibility = View.VISIBLE
                binding.publicTransCV.visibility = View.VISIBLE
                binding.foodCv.visibility = View.VISIBLE
                binding.setProfileCv.visibility = View.VISIBLE
            }
        }

        return binding.root
    }

}