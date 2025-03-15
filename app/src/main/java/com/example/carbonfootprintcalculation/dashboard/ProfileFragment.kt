package com.example.carbonfootprintcalculation.dashboard

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.carbonfootprintcalculation.MainActivity
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.databinding.FragmentProfileBinding
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.example.carbonfootprintcalculation.util.SharePreferencesUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    @Inject
    lateinit var activityUtil: SMMActivityUtil
    @Inject
    lateinit var sharePrefs: SharePreferencesUtil
    lateinit var binding: FragmentProfileBinding
    private lateinit var database: DatabaseReference


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding.model = this
        database = Firebase.database.reference
        binding.btnSignOut.setOnClickListener {
            logout(it)
        }
        profile()
        return binding.root
       
    }

    private fun profile(){
        activityUtil.setFullScreenLoading(true)
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        database.child("User").child(uid).get().addOnSuccessListener {
            activityUtil.setFullScreenLoading(false)
            binding.fullNameTv.text = it.child("name").value.toString()
            binding.phoneNumberTv.text = it.child("number").value.toString()
            binding.ccTv.text = it.child("engineSize").value.toString()
            binding.fuelTypeTv.text = it.child("fuelType").value.toString()

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun logout(view: View) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure!")
        builder.setCancelable(false)
        builder.setPositiveButton("Yes") { _, _ ->
            sharePrefs.setAuthToken("")
            activity?.let {
                startActivity(MainActivity.getLaunchIntent(it))
            }
        }
        builder.setNegativeButton("No") { _, _ -> }
        val alartDialog = builder.create()
        alartDialog.show()
    }

}