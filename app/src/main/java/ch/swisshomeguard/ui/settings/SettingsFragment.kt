package ch.swisshomeguard.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import ch.swisshomeguard.BuildConfig
import ch.swisshomeguard.R
import ch.swisshomeguard.ServiceLocator
import ch.swisshomeguard.UserRoleViewModel
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.ui.cameras.CamerasViewModel
import ch.swisshomeguard.utils.EventObserver
import ch.swisshomeguard.utils.FirebaseTokenUtils
import ch.swisshomeguard.utils.HomeguardTokenUtils
import kotlin.properties.Delegates


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var camerasViewModel: CamerasViewModel
    private val userRoleViewModel: UserRoleViewModel by activityViewModels()

    private var systemId: Int? = null
    private var rootKey: String? = null
    private lateinit var notificationTypeStatus: String
    private var maintenanceModeStatus by Delegates.notNull<Boolean>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        this.rootKey = rootKey

        // Inflate an empty temporary preferenceScreen screen, since a null preferenceScreen will cause a crash
        setPreferencesFromResource(R.xml.root_preferences_empty, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val repository = ServiceLocator.provideRepository()
        settingsViewModel = ViewModelProvider(
            requireActivity(), SettingsViewModel.Factory(repository)
        ).get(SettingsViewModel::class.java)
        camerasViewModel = ViewModelProvider(
            requireActivity(), CamerasViewModel.Factory(repository)
        ).get(CamerasViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        // Refresh data every time the app comes from the background
        userRoleViewModel.isBasicUser.observe(viewLifecycleOwner) {
            createPreferences(it)
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // TODO Fix edge case when there is no network.
        //  Ideally the preferences should not be changed in that case.
        if (sharedPreferences == null) return
        when (key) {
            getString(R.string.pref_key_notification_list) -> {
                val newValue = sharedPreferences.getString(key, null)
                Log.d("SETTINGS_TAG", "new notification value: $newValue")
                // Send new value to server only if it is different from the last one received
                if (newValue != null && newValue != notificationTypeStatus) {
                    Log.d("SETTINGS_TAG", "sending new push value to the server: $newValue")
                    settingsViewModel.setNotificationStatus(newValue.toInt())
                }
            }
            getString(R.string.pref_key_maintenance) -> {
                systemId?.let {
                    val newValue = sharedPreferences.getBoolean(key, false)
                    Log.d("SETTINGS_TAG", "new maintenance value: $newValue")
                    // Send new value to server only if it is different from the last one received
                    if (newValue != maintenanceModeStatus) {
                        Log.d("SETTINGS_TAG", "sending new maintenance value to server: $newValue")
                        settingsViewModel.setUpMaintenance(it, newValue)
                    }
                }
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == getString(R.string.pref_key_logout)) {
            FirebaseTokenUtils.readFirebaseToken()?.let { firebaseToken ->
                settingsViewModel.deleteFirebaseToken(firebaseToken)
            }
            FirebaseTokenUtils.clearDefaultSharedPreferences()
            settingsViewModel.logout()
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun createPreferences(isBasicUser: Boolean) {
        // Inflate the appropriate preferenceScreen after the user role is received
        if (isBasicUser) {
            setPreferencesFromResource(R.xml.root_preferences_basic, rootKey)
        } else {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        // Push notifications preference
        settingsViewModel.fetchNotificationStatus()
        settingsViewModel.notificationStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    val notificationPref =
                        findPreference<ListPreference>(getString(R.string.pref_key_notification_list))

                    val notificationTypeNames =
                        result.data.additionalData.userNotificationType.map { it.name }
                            .toTypedArray()
                    notificationPref?.entries = notificationTypeNames
                    Log.d(
                        "SETTINGS_TAG",
                        "Notification type names: ${notificationTypeNames.contentToString()}"
                    )

                    val notificationTypesIndices =
                        result.data.additionalData.userNotificationType.map { it.id.toString() }
                            .toTypedArray()
                    notificationPref?.entryValues = notificationTypesIndices
                    Log.d(
                        "SETTINGS_TAG",
                        "Notification type indices: ${notificationTypesIndices.contentToString()}"
                    )

                    notificationTypeStatus = result.data.data.userNotificationTypeId.toString()
                    notificationPref?.value = null
                    notificationPref?.value = notificationTypeStatus
                    Log.d(
                        "SETTINGS_TAG",
                        "Selected notification type index: $notificationTypeStatus"
                    )
                }

                is Result.Loading -> {
                }

                is Result.Error -> {
                }
            }
        }

        // Maintenance mode preference
        camerasViewModel.selectedSystemResult.observe(viewLifecycleOwner) {
            when (it) {
                is Result.Success -> {
                    val system = it.data
                    systemId = system.id
                    settingsViewModel.fetchMaintenanceModeStatus(system.id)
                }

                is Result.Loading -> {
                }

                is Result.Error -> {
                }
            }
        }
        settingsViewModel.systemStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Result.Success -> {
                    val maintenancePref =
                        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_key_maintenance))

                    maintenanceModeStatus = it.data.isMaintenance
                    maintenancePref?.isChecked = maintenanceModeStatus
                    Log.d("SETTINGS_TAG", "Maintenance mode value: $maintenanceModeStatus")

                    val isMaintenanceEditable = it.data.editableOptions.isMaintenance
                    maintenancePref?.isEnabled = isMaintenanceEditable
                    Log.d("SETTINGS_TAG", "Maintenance mode editable: $isMaintenanceEditable")
                }

                is Result.Loading -> {
                }

                is Result.Error -> {
                }
            }
        }

        // App version
        val pref: Preference? = findPreference(getString(R.string.pref_key_version))
        pref?.summary = BuildConfig.VERSION_NAME

        // Logout
        settingsViewModel.isLogoutSuccessful.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                // Even if there is a server error, the app should logout for a better user experience
                // Throw token away and navigate to login screen
                is Result.Success, is Result.Error -> {
                    HomeguardTokenUtils.deleteHomeguardToken()
                    findNavController().navigate(R.id.action_navigation_settings_to_loginFragment)
                }
                is Result.Loading -> {

                }
            }
        })
    }

}