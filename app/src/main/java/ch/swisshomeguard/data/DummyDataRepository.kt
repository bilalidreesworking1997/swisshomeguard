package ch.swisshomeguard.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import ch.swisshomeguard.model.dummy.DummyCamera
import ch.swisshomeguard.model.events.Event
import ch.swisshomeguard.model.notifications.NotificationEnabledStatusResponse
import ch.swisshomeguard.model.player.VideoChannel
import ch.swisshomeguard.model.status.*
import ch.swisshomeguard.model.system.HomeguardSystem
import ch.swisshomeguard.utils.ViewModelEvent
import kotlinx.coroutines.flow.Flow
import java.util.*

class DummyDataRepository : DataRepository {

    override val selectedSystemId: LiveData<Int>
        get() = TODO("Not yet implemented")

    override val systems: LiveData<Result<List<HomeguardSystem>>>
        get() = TODO("Not yet implemented")

    private val _cameras = MutableLiveData<Result<List<DummyCamera>>>()
    override val cameras: LiveData<Result<List<DummyCamera>>> = _cameras

    override val systemStatus: LiveData<Result<SystemStatus>>
        get() = TODO("Not yet implemented")

    override suspend fun sendFirebaseTokenToServer(firebaseToken: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFirebaseToken(firebaseToken: String) {
        TODO("Not yet implemented")
    }

    override suspend fun fetchSystems() {
        TODO("Not yet implemented")
    }

    override val notificationStatus: LiveData<Result<NotificationEnabledStatusResponse>>
        get() = TODO("Not yet implemented")

    override val isLogoutSuccessful: LiveData<ViewModelEvent<Result<Boolean>>>
        get() = TODO("Not yet implemented")

    override fun selectSystem(systemId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun fetchCameras() {
        val camera1 = DummyCamera(
            camera_id = UUID.randomUUID(),
            stream_url = "stream_url1",
            detecting = true,
            active_alarm = true
        )
        val camera2 = DummyCamera(
            camera_id = UUID.randomUUID(),
            stream_url = "stream_url2",
            detecting = true,
            active_alarm = true
        )
        val camera3 = DummyCamera(
            camera_id = UUID.randomUUID(),
            stream_url = "stream_url3",
            detecting = true,
            active_alarm = true
        )
        val camera4 = DummyCamera(
            camera_id = UUID.randomUUID(),
            stream_url = "stream_url4",
            detecting = false,
            active_alarm = false
        )
        val camera5 = DummyCamera(
            camera_id = UUID.randomUUID(),
            stream_url = "stream_url5",
            detecting = false,
            active_alarm = false
        )
        val camera6 = DummyCamera(
            camera_id = UUID.randomUUID(),
            stream_url = "stream_url6",
            detecting = false,
            active_alarm = false
        )

        val data = arrayListOf(camera1, camera2, camera3, camera4, camera5, camera6)
        _cameras.value = Result.Success(data)
    }

    override fun fetchPagedEvents(
        systemId: Int,
        startDateMillis: Long?,
        endDateMillis: Long?,
        systemDeviceIds: List<Int>?
    ): Flow<PagingData<Event>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchEventDetails(systemId: Int, eventIds: String): Result<List<Event>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchSystemStatus(systemId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun setRecordingStatus(systemId: Int, recordingStatus: RecordingStatus) {
        TODO("Not yet implemented")
    }

    override suspend fun setAlarmCentralStatus(
        systemId: Int,
        alarmCentralStatus: AlarmCentralStatus
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun setAlarmSignalStatus(
        systemId: Int,
        alarmSignalStatus: AlarmSignalStatus
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun setCalendarStatus(systemId: Int, calendarStatus: ScheduleStatus) {
        TODO("Not yet implemented")
    }

    override suspend fun setMaintenanceModeStatus(
        systemId: Int,
        maintenanceModeStatus: MaintenaceModeStatus
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun setNotificationStatus(notificationTypeId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun fetchNotificationStatus() {
        TODO("Not yet implemented")
    }

    override suspend fun fetchVideo(streamChannelUrl: String): Result<VideoChannel> {
        TODO("Not yet implemented")
    }

    override suspend fun keepVideoAlive(keepAliveUrl: String) {
        TODO("Not yet implemented")
    }

    override suspend fun logout() {
        TODO("Not yet implemented")
    }
}