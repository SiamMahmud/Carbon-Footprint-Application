package com.example.carbonfootprintcalculation.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.databinding.FragmentAdminUserViewBinding
import com.example.carbonfootprintcalculation.databinding.FragmentViewCarResultBinding
import com.example.carbonfootprintcalculation.presentation.adapter.AdminViewUserAdapter
import com.example.carbonfootprintcalculation.presentation.adapter.CarAdapter
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.auth.User
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdminUserViewFragment : Fragment() {
    private lateinit var binding: FragmentAdminUserViewBinding
    lateinit var database: DatabaseReference

    @Inject
    lateinit var activityUtil: SMMActivityUtil


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin_user_view, container, false)
        binding.model = this



        return binding.root
    }


}