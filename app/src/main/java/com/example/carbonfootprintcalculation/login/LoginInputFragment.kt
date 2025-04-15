package com.example.carbonfootprintcalculation.login

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.carbonfootprintcalculation.MainActivity
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.databinding.FragmentLoginInputBinding
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.example.carbonfootprintcalculation.util.SharePreferencesUtil
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
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
class LoginInputFragment : Fragment() {
    @Inject
    lateinit var activityUtil : SMMActivityUtil
    @Inject
    lateinit var sharedPrefs: SharePreferencesUtil
    private lateinit var binding : FragmentLoginInputBinding
    lateinit var auth: FirebaseAuth
    lateinit var database: DatabaseReference

    val actionSignUp = Navigation.createNavigateOnClickListener(R.id.action_loginInputFragment_to_signUpFragment)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login_input, container, false)
        binding.model = this
        auth = Firebase.auth
        database = Firebase.database.reference
        activityUtil.hideBottomNavigation(true)
        binding.loginErrorTv.visibility = View.GONE
        binding.btnSignIn.setOnClickListener {
            val email: String = binding.emailEt.text.toString().trim()
            val password: String = binding.passwordEt.text.toString().trim()
            login(email, password)
        }
        
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun login(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            binding.loginErrorTv.visibility = View.GONE
            activityUtil.setFullScreenLoading(true)

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    database = FirebaseDatabase.getInstance().getReference("User").child(uid)
                    database.get().addOnSuccessListener { data ->
                        Log.d("Tag", "onSuccess" + data.getValue())
                        if (data.exists()) {
                            Handler().postDelayed({
                                sharedPrefs.setAuthToken(uid)
                                activity?.let {
                                    startActivity(MainActivity.getLaunchIntent(it))
                                }
                            }, 500)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Show the error message in loginErrorTv
                    binding.loginErrorTv.text = exception.localizedMessage
                    binding.loginErrorTv.visibility = View.VISIBLE
                    activityUtil.setFullScreenLoading(false)
                }
        } else {
            binding.loginErrorTv.visibility = View.VISIBLE
        }
    }

}