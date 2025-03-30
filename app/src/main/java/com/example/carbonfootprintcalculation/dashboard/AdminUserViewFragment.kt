package com.example.carbonfootprintcalculation.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.dashboard.model.User
import com.example.carbonfootprintcalculation.databinding.FragmentAdminUserViewBinding
import com.example.carbonfootprintcalculation.presentation.adapter.AdminViewUserAdapter
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdminUserViewFragment : Fragment() {
    private lateinit var binding: FragmentAdminUserViewBinding
    lateinit var database: DatabaseReference

    @Inject
    lateinit var activityUtil: SMMActivityUtil
    lateinit var UserArray: ArrayList<User>
    lateinit var adapter: AdminViewUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin_user_view, container, false)
        binding.model = this
        binding.backIv.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.adminViewUserListRecycle.layoutManager = LinearLayoutManager(activity)
        UserArray = arrayListOf()
        adapter = AdminViewUserAdapter(UserArray, requireContext())
        binding.adminViewUserListRecycle.adapter = adapter
        viewUser()
        adapter.onItemClick = { user ->
            showOptionsPopup(user.uid)

        }
        return binding.root
    }

    private fun showOptionsPopup(userId: String?) {
        val popup = PopupMenu(requireContext(), binding.adminViewUserListRecycle)
        popup.menuInflater.inflate(R.menu.user_result_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            val bundle = Bundle().apply {
                putString("id", userId)
            }
            when (menuItem.itemId) {
                R.id.viewCarResult -> findNavController().navigate(R.id.action_adminUserViewFragment_to_viewCarResultFragment, bundle)
                R.id.viewPublicTransportResult -> findNavController().navigate(R.id.action_adminUserViewFragment_to_viewPublicTransportResultFragment, bundle)
                R.id.viewFoodResult -> findNavController().navigate(R.id.action_adminUserViewFragment_to_foodResultFragment, bundle)
            }
            true
        }
        popup.show()

    }

    private fun viewUser() {
        database = FirebaseDatabase.getInstance().getReference("User")
        database.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (snap in snapshot.children){
                        if (snap.hasChild("User")){
                            val user = snap.getValue(User::class.java)
                            user?.uid = snap.key
                            if (!UserArray.contains(user)){
                                UserArray.add(user!!)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireActivity(),error.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }


}