package ch.swisshomeguard.ui.home

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import ch.swisshomeguard.FCM_TAG
import ch.swisshomeguard.R
import ch.swisshomeguard.ServiceLocator
import ch.swisshomeguard.TOKEN_TAG
import ch.swisshomeguard.UserRoleViewModel
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.ui.cameras.CamerasViewModel
import ch.swisshomeguard.utils.FirebaseTokenUtils
import ch.swisshomeguard.utils.HomeguardTokenUtils
import ch.swisshomeguard.utils.SharedPreferencesUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.home_fragment.alarmCentralButton
import kotlinx.android.synthetic.main.home_fragment.alarmSignalButton
import kotlinx.android.synthetic.main.home_fragment.buttonsGroup
import kotlinx.android.synthetic.main.home_fragment.calendarButton
import kotlinx.android.synthetic.main.home_fragment.loadingOrErrorLayout
import kotlinx.android.synthetic.main.home_fragment.logoImage
import kotlinx.android.synthetic.main.home_fragment.recordingButton
import kotlinx.android.synthetic.main.home_fragment.splashLayout
import kotlinx.android.synthetic.main.home_fragment.systemNotesList
import kotlinx.android.synthetic.main.home_fragment.systemsTitle
import kotlinx.android.synthetic.main.home_fragment.toolbar
import kotlinx.android.synthetic.main.loading_or_error.errorMessage
import kotlinx.android.synthetic.main.loading_or_error.progressBar
import kotlinx.android.synthetic.main.loading_or_error.retryButton
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class HomeFragment : Fragment() {

    private lateinit var camerasViewModel: CamerasViewModel
    private lateinit var homeViewModel: HomeViewModel
    private val userRoleViewModel: UserRoleViewModel by activityViewModels()
    private val args: HomeFragmentArgs by navArgs()
    private var navView: BottomNavigationView? = null;
    private var isFragmentCreated: Boolean = false;
    private var additionalConfirmation: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val navController = findNavController()
        navView = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
        val shouldReload = args.shouldReload
        val shouldReloadFromPreference = SharedPreferencesUtil.readShouldReload()
        Log.d("RELOAD_TAG", "shouldReload: $shouldReload")

        val settingsPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        additionalConfirmation = settingsPref.getBoolean(getString(R.string.pref_key_additional_confirmation), false)

        if (HomeguardTokenUtils.readHomeguardToken() == null) {
            navController.navigate(R.id.action_navigation_home_to_loginFragment)
            return
        }

        val repository = ServiceLocator.provideRepository()
        camerasViewModel = ViewModelProvider(
            requireActivity(),
            CamerasViewModel.Factory(repository)
        ).get(CamerasViewModel::class.java)

        if (shouldReload && shouldReloadFromPreference) {
            camerasViewModel.reset()
            SharedPreferencesUtil.saveShouldReload(false)
        }

        homeViewModel = ViewModelProvider(
            this,
            HomeViewModel.Factory(repository)
        ).get(HomeViewModel::class.java)

        sendFirebaseTokenToServer()

        camerasViewModel.systemNamesResult.observe(viewLifecycleOwner) {
            when (it) {
                is Result.Success -> {
                    showDataViews()
                    val systemNames = it.data
                    systemsTitle.setOnClickListener {
                        MaterialAlertDialogBuilder(requireContext())
                            .setItems(systemNames.toTypedArray()) { dialog, which ->
                                Log.i("HomeFragment", "dialog $dialog")
                                Log.i("HomeFragment", "which $which")
                                camerasViewModel.selectSystem(which)
                            }
                            .show()
                    }
                }

                is Result.Loading -> {
                    showLoading()
                }

                is Result.Error -> {
                    isNetworkAvailable(it.exception.message)
                    showError(it)
                }
            }
        }

        camerasViewModel.isLogout.observe(viewLifecycleOwner) { isLogOut ->
            isLogOut.getContentIfNotHandled()?.let {
                Log.d(TOKEN_TAG, "HomeFragment isLogout $it")
                if (it) {
                    navController.navigate(R.id.action_navigation_home_to_loginFragment)
                }
            }
        }

        val systemNotesAdapter = SystemNotesAdapter(homeViewModel)
        systemNotesList.adapter = systemNotesAdapter
        systemNotesList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        homeViewModel.systemStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {

                    showDataViews()

                    // System status
                    systemNotesAdapter.submitList(result.data.systemNotes)

                    // Recording button
                    setUpButton(
                        button = recordingButton,
                        isButtonEnabled = result.data.editableOptions.isRecording,
                        isButtonOn = result.data.isRecording,
                        hideIfDisabled = false,
                        onText = getString(R.string.home_recording_enabled),
                        offText = getString(R.string.home_recording_disabled),
                        turnOnFunction = {
                            if (additionalConfirmation) {
                                showAdditionalConfirmation(
                                    title = this@HomeFragment.getString(R.string.video_recording_activation),
                                    message = this@HomeFragment.getString(R.string.video_recording_activation_message),
                                    negativeButtonText = this@HomeFragment.getString(R.string.home_alarm_dialog_negative_button),
                                    positiveButtonText = this@HomeFragment.getString(R.string.activate_video_recording),
                                    onNegativeButtonClick = {
                                        // If the turn on alarm signal dialog is cancelled, the alarm signal has to be off again.
                                        this@HomeFragment.recordingButton.isChecked = false
                                    },
                                    onPositiveButtonClick = {
                                        Toast.makeText(
                                            this@HomeFragment.activity,
                                            this@HomeFragment.getString(R.string.video_recording_activated),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        homeViewModel.setRecordingState(true)
                                    })
                            } else {
                                homeViewModel.setRecordingState(true)
                            }
                        },
                        turnOffFunction = {
                            if (additionalConfirmation) {
                                showAdditionalConfirmation(
                                    title = this@HomeFragment.getString(R.string.video_recording_deactivation),
                                    message = this@HomeFragment.getString(R.string.video_recording_deactivation_message),
                                    negativeButtonText = this@HomeFragment.getString(R.string.home_alarm_dialog_negative_button),
                                    positiveButtonText = this@HomeFragment.getString(R.string.deactivate_video_recording),
                                    onNegativeButtonClick = {
                                        this@HomeFragment.recordingButton.isChecked = true
                                    },
                                    onPositiveButtonClick = {
                                        homeViewModel.setRecordingState(false)
                                    })
                            } else {
                                homeViewModel.setRecordingState(false)
                            }
                        }
                    )

                    // Alarm Central button
                    setUpButton(
                        button = alarmCentralButton,
                        isButtonEnabled = result.data.editableOptions.isAlarmCentral,
                        isButtonOn = result.data.isAlarmCentral,
                        hideIfDisabled = false,
                        onText = getString(R.string.home_alarm_central_enabled),
                        offText = getString(R.string.home_alarm_central_disabled),
                        turnOnFunction = {
                            if (additionalConfirmation) {
                                showAdditionalConfirmation(
                                    title = this@HomeFragment.getString(R.string.alarm_central_activation),
                                    message = this@HomeFragment.getString(R.string.alarm_central_activation_message),
                                    negativeButtonText = this@HomeFragment.getString(R.string.home_alarm_dialog_negative_button),
                                    positiveButtonText = this@HomeFragment.getString(R.string.activate_alarm_central),
                                    onNegativeButtonClick = {
                                        // If the turn on alarm signal dialog is cancelled, the alarm signal has to be off again.
                                        this@HomeFragment.alarmCentralButton.isChecked = false
                                    },
                                    onPositiveButtonClick = {
                                        Toast.makeText(
                                            this@HomeFragment.activity,
                                            this@HomeFragment.getString(R.string.alarm_central_activated),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        homeViewModel.setAlarmCentralState(true)
                                    })
                            } else {
                                homeViewModel.setAlarmCentralState(true)
                            }
                        },
                        turnOffFunction = {
                            if (additionalConfirmation) {
                                showAdditionalConfirmation(
                                    title = this@HomeFragment.getString(R.string.alarm_central_deactivation),
                                    message = this@HomeFragment.getString(R.string.alarm_central_deactivation_message),
                                    negativeButtonText = this@HomeFragment.getString(R.string.home_alarm_dialog_negative_button),
                                    positiveButtonText = this@HomeFragment.getString(R.string.deactivate_alarm_central),
                                    onNegativeButtonClick = {
                                        this@HomeFragment.alarmCentralButton.isChecked = true
                                    },
                                    onPositiveButtonClick = {
                                        homeViewModel.setAlarmCentralState(false)
                                    })
                            } else {
                                homeViewModel.setAlarmCentralState(false)
                            }

                        }
                    )

                    // Alarm signal button
                    setUpButton(
                        button = alarmSignalButton,
                        isButtonEnabled = result.data.editableOptions.isSiren,
                        isButtonOn = result.data.isSiren,
                        hideIfDisabled = true,
                        onText = getString(R.string.home_alarm_signal_is_on),
                        offText = getString(R.string.home_alarm_signal_is_off),
                        turnOnFunction = {
                            showAdditionalConfirmation(
                                title = this@HomeFragment.getString(R.string.home_alarm_dialog_title),
                                message = this@HomeFragment.getString(R.string.home_alarm_dialog_message),
                                negativeButtonText = this@HomeFragment.getString(R.string.home_alarm_dialog_negative_button),
                                positiveButtonText = this@HomeFragment.getString(R.string.home_alarm_dialog_positive_button),
                                onNegativeButtonClick = {
                                    // If the turn on alarm signal dialog is cancelled, the alarm signal has to be off again.
                                    this@HomeFragment.alarmSignalButton.isChecked = false
                                },
                                onPositiveButtonClick = {
                                    Toast.makeText(
                                        this@HomeFragment.activity,
                                        this@HomeFragment.getString(R.string.home_alarm_sent_toast),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    homeViewModel.sendAlarm(true)
                                })
                        },
                        turnOffFunction = {
                            showAdditionalConfirmation(
                                title = this@HomeFragment.getString(R.string.home_alarm_dialog_title_deactivation),
                                message = this@HomeFragment.getString(R.string.home_alarm_dialog_message_deactivation),
                                negativeButtonText = this@HomeFragment.getString(R.string.home_alarm_dialog_negative_button),
                                positiveButtonText = this@HomeFragment.getString(R.string.home_alarm_sent_toast_deactivation),
                                onNegativeButtonClick = {
                                    this@HomeFragment.alarmSignalButton.isChecked = true
                                },
                                onPositiveButtonClick = {
                                    homeViewModel.sendAlarm(false)
                                })
                        }
                    )

                    // Calendar button
                    setUpButton(
                        button = calendarButton,
                        isButtonEnabled = result.data.editableOptions.isSchedule,
                        isButtonOn = result.data.isSchedule,
                        hideIfDisabled = false,
                        onText = getString(R.string.home_calendar_is_on),
                        offText = getString(R.string.home_calendar_is_off),
                        turnOnFunction = {
                            if (additionalConfirmation) {
                                showAdditionalConfirmation(
                                    title = this@HomeFragment.getString(R.string.automatic_calendar_control_activation),
                                    message = this@HomeFragment.getString(R.string.automatic_calendar_control_activation_message),
                                    negativeButtonText = this@HomeFragment.getString(R.string.home_alarm_dialog_negative_button),
                                    positiveButtonText = this@HomeFragment.getString(R.string.activate_calendar_control),
                                    onNegativeButtonClick = {
                                        // If the turn on alarm signal dialog is cancelled, the alarm signal has to be off again.
                                        this@HomeFragment.calendarButton.isChecked = false
                                    },
                                    onPositiveButtonClick = {
                                        Toast.makeText(
                                            this@HomeFragment.activity,
                                            this@HomeFragment.getString(R.string.automatic_calendar_control_activated),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        homeViewModel.setCalendarStatus(true)
                                    })
                            } else {
                                homeViewModel.setCalendarStatus(true)
                            }
                        },
                        turnOffFunction = {
                            if (additionalConfirmation) {
                                showAdditionalConfirmation(
                                    title = this@HomeFragment.getString(R.string.automatic_calendar_control_deactivation),
                                    message = this@HomeFragment.getString(R.string.automatic_calendar_control_deactivation_message),
                                    negativeButtonText = this@HomeFragment.getString(R.string.home_alarm_dialog_negative_button),
                                    positiveButtonText = this@HomeFragment.getString(R.string.deactivate_calendar_control),
                                    onNegativeButtonClick = {
                                        this@HomeFragment.calendarButton.isChecked = true
                                    },
                                    onPositiveButtonClick = {
                                        homeViewModel.setCalendarStatus(false)
                                    })
                            } else {
                                homeViewModel.setCalendarStatus(false)
                            }
                        }
                    )
                }

                is Result.Loading -> {}
                is Result.Error -> {
                    isNetworkAvailable(result.exception.message)
                    showError(result)
                }
            }
        }

        retryButton.setOnClickListener {
            camerasViewModel.fetchSystems()
        }
    }

    private fun showAdditionalConfirmation(
        title: String,
        message: String,
        negativeButtonText: String,
        positiveButtonText: String,
        onNegativeButtonClick: () -> Unit,
        onPositiveButtonClick: () -> Unit,
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(negativeButtonText) { _, _ ->
                onNegativeButtonClick()
            }
            .setPositiveButton(positiveButtonText) { _, _ ->
                onPositiveButtonClick()
            }
            .create()
            .show()
    }

    private fun isNetworkAvailable(errorMessage: String?) {
        InternetCheck { internet ->
            if (!internet) Toast.makeText(
                context,
                context?.getString(R.string.noInternetConnectivity),
                Toast.LENGTH_SHORT
            ).show()
            else
                Toast.makeText(
                    context,
                    errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFragmentCreated = true;
    }

    override fun onResume() {
        super.onResume()

        // Refresh data every time the app comes from the background
        camerasViewModel.selectedSystemResult.observe(viewLifecycleOwner) {
            when (it) {
                is Result.Success -> {
                    showDataViews()
                    val system = it.data
                    systemsTitle.text = system.toString()
                    homeViewModel.fetchSystemStatus(system.id)
                    userRoleViewModel.setBasicUser(system.systemUserRole.isBasicUser())
                }

                is Result.Loading -> {
                    showLoading()
                }

                is Result.Error -> {
                    isNetworkAvailable(it.exception.message)
                    showError(it)
                }
            }
        }
    }

    private fun setUpButton(
        button: MaterialButton,
        isButtonEnabled: Boolean,
        isButtonOn: Boolean,
        hideIfDisabled: Boolean,
        onText: String,
        offText: String,
        turnOnFunction: (v: View) -> Unit,
        turnOffFunction: (v: View) -> Unit
    ) {
        // Check state
        button.isEnabled = true     // Make sure button is enabled before checking/unchecking
        button.isChecked = isButtonOn
        button.isEnabled = isButtonEnabled  // Correctly enable/disable button at the end

        // Text and function
        if (isButtonOn) {
            button.text = onText
            button.setOnClickListener(turnOffFunction)
        } else {
            button.text = offText
            button.setOnClickListener(turnOnFunction)
        }

        // Visibility
        if (hideIfDisabled && !isButtonEnabled) {
            button.visibility = View.GONE
        } else {
            button.visibility = View.VISIBLE
        }
    }

    private fun sendFirebaseTokenToServer() {
        // https://firebase.google.com/docs/cloud-messaging/android/first-message?authuser=0
        // TODO If this method retrieves the Firebase ID internally,
        //  without fetching from the Firebase server,
        //  I don't need to save and retrieve from SharedPreferences
//        FirebaseInstanceId.getInstance().instanceId
//            .addOnCompleteListener(OnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    Log.w(FCM_TAG, "getInstanceId failed", task.exception)
//                    return@OnCompleteListener
//                }
//
//                // Get new Instance ID token
//                val firebaseToken = task.result?.token
//
//                // Log
//                if (firebaseToken != null) {
//                    Log.d(FCM_TAG, "Firebase token retrieved $firebaseToken")
//                    FirebaseTokenUtils.saveFirebaseTokenIfChanged(firebaseToken)
//                    homeViewModel.sendFirebaseTokenToServer(firebaseToken)
//                } else {
//                    Log.d(FCM_TAG, "Firebase token could not be retrieved")
//                }
//            })

        FirebaseMessaging.getInstance().token.addOnSuccessListener { firebaseToken ->
            if (firebaseToken != null) {
                if (firebaseToken != null) {
                    Log.d(FCM_TAG, "Firebase token retrieved $firebaseToken")
                    FirebaseTokenUtils.saveFirebaseTokenIfChanged(firebaseToken)
                    homeViewModel.sendFirebaseTokenToServer(firebaseToken)
                } else {
                    Log.d(FCM_TAG, "Firebase token could not be retrieved")
                }
            }
        }
    }

    private fun showDataViews() {
        toolbar.visibility = View.VISIBLE
        navView?.visibility = View.VISIBLE
        splashLayout.visibility = View.GONE
        logoImage.visibility = View.VISIBLE
        systemsTitle.visibility = View.VISIBLE
        systemNotesList.visibility = View.VISIBLE
        buttonsGroup.visibility = View.VISIBLE
        loadingOrErrorLayout.visibility = View.GONE
    }

    private fun showLoading() {
        toolbar.visibility = View.GONE
        navView?.visibility = View.GONE
        if (isFragmentCreated) {
            splashLayout.visibility = View.VISIBLE
            isFragmentCreated = false;
        }
        logoImage.visibility = View.GONE
        systemsTitle.visibility = View.GONE
        systemNotesList.visibility = View.GONE
        buttonsGroup.visibility = View.GONE
        loadingOrErrorLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
        retryButton.visibility = View.GONE
        errorMessage.visibility = View.GONE
    }

    private fun showError(it: Result.Error) {
        toolbar.visibility = View.GONE
        navView?.visibility = View.GONE
        splashLayout.visibility = View.VISIBLE
        logoImage.visibility = View.GONE
        systemsTitle.visibility = View.GONE
        systemNotesList.visibility = View.GONE
        buttonsGroup.visibility = View.GONE
        loadingOrErrorLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
        retryButton.visibility = View.GONE
        errorMessage.visibility = View.GONE
        errorMessage.text = it.exception.message
    }

    internal class InternetCheck(private val onInternetChecked: (Boolean) -> Unit) :
        AsyncTask<Void, Void, Boolean>() {
        init {
            execute()
        }

        override fun doInBackground(vararg voids: Void): Boolean {
            return try {
                val sock = Socket()
                sock.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                sock.close()
                true
            } catch (e: IOException) {
                false
            }
        }

        override fun onPostExecute(internet: Boolean) {
            onInternetChecked(internet)
        }
    }
}