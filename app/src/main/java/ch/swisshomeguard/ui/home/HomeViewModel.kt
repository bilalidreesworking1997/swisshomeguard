package ch.swisshomeguard.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.swisshomeguard.data.DataRepository
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.model.status.*
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: DataRepository) : ViewModel() {

    val systemStatus: LiveData<Result<SystemStatus>> = repository.systemStatus

    private var systemId: Int? = null

    fun sendFirebaseTokenToServer(firebaseToken: String) {
        viewModelScope.launch {
            repository.sendFirebaseTokenToServer(firebaseToken)
        }
    }

    fun fetchSystemStatus(id: Int) {
        viewModelScope.launch {
            systemId = id
            repository.fetchSystemStatus(id)
        }
    }

    fun setRecordingState(isEnabled: Boolean) {
        systemId?.let {
            viewModelScope.launch {
                repository.setRecordingStatus(it, RecordingStatus(isEnabled))
            }
        }
    }

    fun setAlarmCentralState(isEnabled: Boolean) {
        systemId?.let {
            viewModelScope.launch {
                repository.setAlarmCentralStatus(it, AlarmCentralStatus(isEnabled))
            }
        }
    }

    fun sendAlarm(isSignalOn: Boolean) {
        systemId?.let {
            viewModelScope.launch {
                repository.setAlarmSignalStatus(it, AlarmSignalStatus(isSignalOn))
            }
        }
    }

    fun setCalendarStatus(isCalendarOn: Boolean) {
        systemId?.let {
            viewModelScope.launch {
                repository.setCalendarStatus(it, ScheduleStatus(isCalendarOn))
            }
        }
    }

    class Factory(private val repository: DataRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}