package ch.swisshomeguard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import ch.swisshomeguard.ui.player.EventPlayerActivityArgs
import ch.swisshomeguard.utils.EVENT_ID
import ch.swisshomeguard.utils.SYSTEM_ID
import ch.swisshomeguard.utils.SharedPreferencesUtil
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navView: BottomNavigationView
    private val userRoleViewModel: UserRoleViewModel by viewModels()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var isAppFirstLaunched: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        navView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.bottom_nav_host_fragment)

        // Hide bottom navigation component when login screen is shown
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> {
                    navView.visibility = View.GONE
                }
                R.id.forgotPasswordFragment -> {
                    navView.visibility = View.GONE
                }
                else -> {
                    navView.visibility = View.VISIBLE
                }
            }
        }

        navView.setupWithNavController(navController)


        val extras = intent.extras
        val keySet = extras?.keySet()
        val systemId = extras?.getString(SYSTEM_ID)?.toInt()
        val eventId = extras?.getString(EVENT_ID)?.toInt()

        Log.d(FCM_TAG, "extras: $extras")
        Log.d(FCM_TAG, "keySet: $keySet")
        Log.d(FCM_TAG, "systemId: $systemId")
        Log.d(FCM_TAG, "eventId: $eventId")

        if (systemId != null && eventId != null) {
            // Clear the extras, otherwise they will be processed the next time the Activity is started, such as on device rotation
            intent.replaceExtras(null)

            // Navigate to specific event when the notification is clicked
            val bundle = EventPlayerActivityArgs(systemId, eventId).toBundle()
            navController.navigate(R.id.eventPlayerActivity, bundle)
            Log.d(FCM_TAG, "Navigating to Event")
        } // TODO else systemId or eventId are missing

        userRoleViewModel.isBasicUser.observe(this, {
            showTabs(it)
        })
    }

    private fun showTabs(isBasicUser: Boolean) {
        if (isBasicUser) {
            navView.menu.clear()
            navView.inflateMenu(R.menu.bottom_nav_menu_basic)
        } else {
            navView.menu.clear()
            navView.inflateMenu(R.menu.bottom_nav_menu)
        }
    }

    override fun onResume() {
        super.onResume()
        //when app comes to foreground from recent apps relaunching activity
        if (!isAppFirstLaunched && SharedPreferencesUtil.readIsFromRecent()) {
            SharedPreferencesUtil.saveIsFromRecent(false)
            finish()
            startActivity(getIntent())
        }
    }

    override fun onStop() {
        super.onStop()
        isAppFirstLaunched = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isAppFirstLaunched = true
    }
}