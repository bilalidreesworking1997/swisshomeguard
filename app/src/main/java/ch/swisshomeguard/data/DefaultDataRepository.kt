package ch.swisshomeguard.data

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ch.swisshomeguard.FCM_TAG
import ch.swisshomeguard.model.dummy.DummyCamera
import ch.swisshomeguard.model.events.Event
import ch.swisshomeguard.model.events.EventPagingSource
import ch.swisshomeguard.model.notifications.NotificationEnabledSet
import ch.swisshomeguard.model.notifications.NotificationEnabledStatusResponse
import ch.swisshomeguard.model.notifications.NotificationSetup
import ch.swisshomeguard.model.player.VideoChannel
import ch.swisshomeguard.model.status.*
import ch.swisshomeguard.model.system.HomeguardSystem
import ch.swisshomeguard.utils.ViewModelEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat

class DefaultDataRepository(private val webService: WebService) : DataRepository {

    private val _selectedSystemId = MutableLiveData<Int>()
    override val selectedSystemId: LiveData<Int> = _selectedSystemId

    private val _systems = MutableLiveData<Result<List<HomeguardSystem>>>()
    override val systems: LiveData<Result<List<HomeguardSystem>>> = _systems

    private val _cameras = MutableLiveData<Result<List<DummyCamera>>>()
    override val cameras: LiveData<Result<List<DummyCamera>>> = _cameras

    private val _systemStatus = MutableLiveData<Result<SystemStatus>>()
    override val systemStatus: LiveData<Result<SystemStatus>> = _systemStatus

    private val _notificationStatus = MutableLiveData<Result<NotificationEnabledStatusResponse>>()
    override val notificationStatus: LiveData<Result<NotificationEnabledStatusResponse>>
        get() = _notificationStatus

    private val _isLogoutSuccessful = MutableLiveData<ViewModelEvent<Result<Boolean>>>()
    override val isLogoutSuccessful: LiveData<ViewModelEvent<Result<Boolean>>> = _isLogoutSuccessful

    override suspend fun sendFirebaseTokenToServer(firebaseToken: String) {
        firebaseToken.let {
            try {
                webService.storeFirebaseToken(NotificationSetup(it, deviceModel = Build.MODEL))
                Log.d(FCM_TAG, "Sending Firebase token to server")
            } catch (exception: Exception) {
                // If Firebase token is already present in the server
                // a 422 Unprocessable Entity response will be returned
                Log.w(FCM_TAG, "Error sending Firebase token to server: ${exception.message}")
            }
        }
    }

    override suspend fun deleteFirebaseToken(firebaseToken: String) {
        Log.d(FCM_TAG, "Deleting Firebase token from server")
        webService.deleteFirebaseToken(firebaseToken)
    }

    override fun selectSystem(systemId: Int) {
        _selectedSystemId.value = systemId
    }

    override suspend fun fetchSystems() {
        _systems.value = Result.Loading
        try {
            val data = webService.fetchSystems()
            _systems.value = Result.Success(data.systems)
        } catch (exception: Exception) {
            _systems.value = Result.Error(exception)
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    override suspend fun fetchCameras() {
        TODO("Not yet implemented")
    }

    override fun fetchPagedEvents(
        systemId: Int,
        startDateMillis: Long?,
        endDateMillis: Long?,
        systemDeviceIds: List<Int>?
    ): Flow<PagingData<Event>> {
        val startDate = convertDateToServerFormat(startDateMillis)
        val endDate = convertDateToServerFormat(endDateMillis)
        val systemDevices = systemDeviceIds?.joinToString(separator = ",")
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE),
            pagingSourceFactory = {
                EventPagingSource(
                    systemId,
                    startDate,
                    endDate,
                    systemDevices,
                    webService
                )
            }
        ).flow
    }

    override suspend fun fetchEventDetails(systemId: Int, eventIds: String): Result<List<Event>> {
        return try {
            val data = webService.fetchEventDetails(systemId, eventIds)
            Result.Success(data.events)
        } catch (exception: Exception) {
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
            Result.Error(exception)
        }
    }

    override suspend fun fetchSystemStatus(systemId: Int) {
        _systemStatus.value = Result.Loading
        try {
            val data = webService.fetchSystemStatus(systemId)
            _systemStatus.value = Result.Success(data.systemStatus)
        } catch (exception: Exception) {
            _systemStatus.value = Result.Error(exception)
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    override suspend fun setRecordingStatus(systemId: Int, recordingStatus: RecordingStatus) {
        _systemStatus.value = Result.Loading
        try {
            val data = webService.setRecordingStatus(systemId, recordingStatus)
            _systemStatus.value = Result.Success(data.systemStatus)
        } catch (exception: Exception) {
            _systemStatus.value = Result.Error(exception)
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    override suspend fun setAlarmCentralStatus(
        systemId: Int,
        alarmCentralStatus: AlarmCentralStatus
    ) {
        _systemStatus.value = Result.Loading
        try {
            val data = webService.setAlarmCentralStatus(systemId, alarmCentralStatus)
            _systemStatus.value = Result.Success(data.systemStatus)
        } catch (exception: Exception) {
            _systemStatus.value = Result.Error(exception)
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    override suspend fun setAlarmSignalStatus(
        systemId: Int,
        alarmSignalStatus: AlarmSignalStatus
    ) {
        _systemStatus.value = Result.Loading
        try {
            val data = webService.setAlarmSignalStatus(systemId, alarmSignalStatus)
            _systemStatus.value = Result.Success(data.systemStatus)
        } catch (exception: Exception) {
            _systemStatus.value = Result.Error(exception)
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    override suspend fun setCalendarStatus(
        systemId: Int,
        scheduleStatus: ScheduleStatus
    ) {
        _systemStatus.value = Result.Loading
        try {
            val data = webService.setCalendarStatus(systemId, scheduleStatus)
            _systemStatus.value = Result.Success(data.systemStatus)
        } catch (exception: Exception) {
            _systemStatus.value = Result.Error(exception)
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    override suspend fun setNotificationStatus(notificationTypeId: Int) {
        try {
            webService.setNotificationStatus(NotificationEnabledSet(notificationTypeId))
        } catch (exception: Exception) {
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    override suspend fun setMaintenanceModeStatus(
        systemId: Int,
        maintenanceModeStatus: MaintenaceModeStatus
    ) {
        _systemStatus.value = Result.Loading
        try {
            val data = webService.setMaintenanceModeStatus(systemId, maintenanceModeStatus)
            _systemStatus.value = Result.Success(data.systemStatus)
        } catch (exception: Exception) {
            _systemStatus.value = Result.Error(exception)
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    override suspend fun fetchNotificationStatus() {
        _notificationStatus.value = Result.Loading
        try {
            val data = webService.fetchNotificationStatus()
            _notificationStatus.value = Result.Success(data)
        } catch (exception: Exception) {
            _notificationStatus.value = Result.Error(exception)
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    override suspend fun fetchVideo(streamChannelUrl: String): Result<VideoChannel> {
        Log.d("VIDEO_TAG", "Repository: Fetch video")
        return try {
//            val data =
//                webService.fetchVideo(streamChannelUrl.removeRange(0, 1)) // Remove starting "/"
            val data =
                webService.fetchVideo(streamChannelUrl.substringAfter("user/stream/video/").substringBefore("/channel/2")) // Remove starting "/"
            Result.Success(data.videoChannel)
        } catch (exception: Exception) {
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
            Result.Error(exception)
        }
    }

    override suspend fun keepVideoAlive(keepAliveUrl: String) {
        try {
//            webService.keepVideoAlive(keepAliveUrl.removeRange(0, 1)) // Remove starting "/"
            webService.keepVideoAlive(keepAliveUrl.substringAfter("user/stream/video/").substringBefore("/channel/2"))

        } catch (exception: Exception) {
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    override suspend fun logout() {
        _isLogoutSuccessful.value = ViewModelEvent(Result.Loading)
        try {
            webService.logout()
            _isLogoutSuccessful.value = ViewModelEvent(Result.Success(true))
        } catch (exception: Exception) {
            _isLogoutSuccessful.value = ViewModelEvent(Result.Error(exception))
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun convertDateToServerFormat(dateMillis: Long?): String? {
        return try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd") // Date format required by server
            simpleDateFormat.format(dateMillis)
        } catch (exception: Exception) {
            null
        }
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 10
    }
}