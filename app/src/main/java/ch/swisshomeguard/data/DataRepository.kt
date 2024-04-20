package ch.swisshomeguard.data

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import ch.swisshomeguard.model.dummy.DummyCamera
import ch.swisshomeguard.model.events.Event
import ch.swisshomeguard.model.notifications.NotificationEnabledStatusResponse
import ch.swisshomeguard.model.player.VideoChannel
import ch.swisshomeguard.model.status.*
import ch.swisshomeguard.model.system.HomeguardSystem
import ch.swisshomeguard.utils.ViewModelEvent
import kotlinx.coroutines.flow.Flow

interface DataRepository {
    val selectedSystemId: LiveData<Int>
    val cameras: LiveData<Result<List<DummyCamera>>>
    val systems: LiveData<Result<List<HomeguardSystem>>>
    val systemStatus: LiveData<Result<SystemStatus>>
    val notificationStatus: LiveData<Result<NotificationEnabledStatusResponse>>
    val isLogoutSuccessful: LiveData<ViewModelEvent<Result<Boolean>>>

    suspend fun sendFirebaseTokenToServer(firebaseToken: String)
    suspend fun deleteFirebaseToken(firebaseToken: String)
    fun selectSystem(systemId: Int)
    suspend fun fetchCameras()
    fun fetchPagedEvents(
        systemId: Int,
        startDateMillis: Long? = null,
        endDateMillis: Long? = null,
        systemDeviceIds: List<Int>? = null
    ): Flow<PagingData<Event>>

    suspend fun fetchEventDetails(systemId: Int, eventIds: String): Result<List<Event>>
    suspend fun fetchSystems()
    suspend fun fetchSystemStatus(systemId: Int)
    suspend fun setRecordingStatus(systemId: Int, recordingStatus: RecordingStatus)
    suspend fun setAlarmCentralStatus(systemId: Int, alarmCentralStatus: AlarmCentralStatus)
    suspend fun setAlarmSignalStatus(systemId: Int, alarmSignalStatus: AlarmSignalStatus)
    suspend fun setCalendarStatus(systemId: Int, calendarStatus: ScheduleStatus)
    suspend fun setMaintenanceModeStatus(systemId: Int, maintenanceModeStatus: MaintenaceModeStatus)
    suspend fun setNotificationStatus(notificationTypeId: Int)
    suspend fun fetchNotificationStatus()
    suspend fun fetchVideo(streamChannelUrl: String): Result<VideoChannel>
    suspend fun keepVideoAlive(keepAliveUrl: String)
    suspend fun logout()
}