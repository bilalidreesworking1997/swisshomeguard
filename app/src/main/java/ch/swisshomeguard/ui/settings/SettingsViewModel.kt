package ch.swisshomeguard.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.swisshomeguard.data.DataRepository
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.model.notifications.NotificationEnabledStatusResponse
import ch.swisshomeguard.model.status.MaintenaceModeStatus
import ch.swisshomeguard.model.status.SystemStatus
import ch.swisshomeguard.utils.ViewModelEvent
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: DataRepository) : ViewModel() {

    val notificationStatus: LiveData<Result<NotificationEnabledStatusResponse>> =
        repository.notificationStatus
    val systemStatus: LiveData<Result<SystemStatus>> = repository.systemStatus
    val isLogoutSuccessful: LiveData<ViewModelEvent<Result<Boolean>>> =
        repository.isLogoutSuccessful

    fun fetchNotificationStatus() {
        viewModelScope.launch {
            repository.fetchNotificationStatus()
        }
    }

    fun fetchMaintenanceModeStatus(systemId: Int) {
        viewModelScope.launch {
            repository.fetchSystemStatus(systemId)
        }
    }

    fun setNotificationStatus(notificationTypeId: Int) {
        viewModelScope.launch {
            repository.setNotificationStatus(notificationTypeId)
        }
    }

    fun setUpMaintenance(systemId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setMaintenanceModeStatus(systemId, MaintenaceModeStatus(isEnabled))
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun deleteFirebaseToken(firebaseToken: String) {
        viewModelScope.launch {
            repository.deleteFirebaseToken(firebaseToken)
        }
    }

    class Factory(private val repository: DataRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}