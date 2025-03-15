package com.example.carbonfootprintcalculation
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.carbonfootprintcalculation.databinding.ActivityMainBinding
import com.example.carbonfootprintcalculation.util.SMMActivityUtil
import com.example.carbonfootprintcalculation.util.SharePreferencesUtil
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : AppCompatActivity() , SMMActivityUtil.ActivityListener{
    @Inject
    lateinit var sharePrefs: SharePreferencesUtil
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply { setContentView(root) }
        val authedUser: Boolean = try {
            !sharePrefs.getAuthToken().isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController

        if (savedInstanceState == null) {
            val inflater = navController.navInflater
            val graph = if (authedUser) {
                inflater.inflate(R.navigation.dashboard_navigation)
            } else {
                inflater.inflate(R.navigation.login_navigation)
            }
            navController.setGraph(graph, savedInstanceState) // Restore state
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> navHostFragment.findNavController().navigate(R.id.homeFragment)
                R.id.profileInfo -> navHostFragment.findNavController().navigate(R.id.profileFragment)
            }
            true
        }

    }
    override fun hideBottomNavigation(hide: Boolean) {
        if (hide) {
            binding.bottomNavigationView.visibility = View.GONE
        } else {
            binding.bottomNavigationView.visibility = View.VISIBLE
        }
    }

    override fun setFullScreenLoading(short: Boolean) {
        if (short) {
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            binding.fullscreenLoading.visibility = View.VISIBLE
        } else {
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            binding.fullscreenLoading.visibility = View.GONE
        }
    }

    override fun closeKeyboard() {
        TODO("Not yet implemented")
    }


    companion object {
        fun getLaunchIntent(context: Context) = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
}
