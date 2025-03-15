package com.example.carbonfootprintcalculation.login

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.databinding.FragmentSignUpBinding
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    @Inject
    lateinit var activityUtil: SMMActivityUtil
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)
        binding.model = this
        activityUtil.hideBottomNavigation(true)

        auth = Firebase.auth
        database = Firebase.database.reference

        binding.continueButton.setOnClickListener {
            registerUser()
        }

        return binding.root
    }



    private fun registerUser() {
        val email = binding.emailEt.text.toString().trim()
        val password = binding.passwordEt.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            activityUtil.setFullScreenLoading(true)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    activityUtil.setFullScreenLoading(false)
                    if (task.isSuccessful) {
                        saveData()
                        Toast.makeText(activity, getString(R.string.auth_massage), Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_signUpFragment_to_loginInputFragment)
                    } else {
                        Toast.makeText(activity, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun saveData() {
        activityUtil.setFullScreenLoading(true)
        database = FirebaseDatabase.getInstance().getReference("User")
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val user = HashMap<String, String>()
        user.put("uid", userId)
        user.put("name", binding.fullNameEt.text.toString().trim())
        user.put("email", binding.emailEt.text.toString().trim())
        user.put("number", binding.phoneNumberEt.text.toString().trim())
        user.put("password", binding.passwordEt.text.toString().trim())
        user.put("carCc", "")
        user.put("fuelType", "")
        user.put("User", "1")
        database.child(userId).setValue(user)
        activityUtil.setFullScreenLoading(false)
        binding.fullNameEt.text?.clear()
        binding.emailEt.text?.clear()
        binding.phoneNumberEt.text?.clear()
        binding.passwordEt.text?.clear()
        binding.confirmPassEt.text?.clear()
    }

}
