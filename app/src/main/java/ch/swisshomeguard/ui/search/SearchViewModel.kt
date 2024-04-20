package ch.swisshomeguard.ui.search

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import ch.swisshomeguard.data.DataRepository
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.model.events.Event
import ch.swisshomeguard.model.system.HomeguardSystem
import ch.swisshomeguard.model.system.SystemDevice
import ch.swisshomeguard.utils.ViewModelEvent
import kotlinx.coroutines.flow.Flow

class SearchViewModel(private val repository: DataRepository) : ViewModel() {

    private val _listLayout = MutableLiveData<ListLayout>()
    val listLayout: LiveData<ListLayout> = _listLayout

    val navigateToPlayer = MediatorLiveData<ViewModelEvent<Result<Pair<String, List<String>>>>>()

    private val systems: LiveData<Result<List<HomeguardSystem>>> = repository.systems
    private val selectedSystemId: LiveData<Int> = repository.selectedSystemId
    private val selectedEvent = MutableLiveData<Event>()

    private val selectedCameras = MutableLiveData<List<Boolean>>()
    private val startDateInMillis = MutableLiveData<Long?>()
    private val endDateInMillis = MutableLiveData<Long?>()

    private val selectedSystemResult = MutableLiveData<Result<HomeguardSystem>>()
    private val camerasResult = Transformations.map(selectedSystemResult) {
        when (it) {
            is Result.Success -> {
                val cameras = it.data.cameras
                Result.Success(cameras)
            }
            is Result.Loading -> Result.Loading
            is Result.Error -> Result.Error(it.exception)
        }
    }
    val camerasFilterResult =
        MediatorLiveData<Result<Triple<Int, List<SystemDevice>, List<Boolean>>>>()
    val datesFilterResult = MediatorLiveData<Result<Triple<Int, Long?, Long?>>>()

    private var currentSystemId: Int? = null
    private var currentEventsResult: Flow<PagingData<Event>>? = null

    init {
        _listLayout.value = ListLayout.SINGLE_COLUMN

        createNavigateResult(systems, selectedSystemId, selectedEvent)
        createCamerasFilterResult(selectedSystemResult, camerasResult, selectedCameras)
        createDateFilterResult(selectedSystemResult, startDateInMillis, endDateInMillis)
    }

    fun fetchEvents(
        systemId: Int,
        startDate: Long? = null,
        endDate: Long? = null,
        systemDeviceIds: List<Int>? = null
    ): Flow<PagingData<Event>> {
        // TODO is this necessary?
        val lastResult = currentEventsResult
//        if (systemId == currentSystemId && lastResult != null) {
//            return lastResult
//        }
        currentSystemId = systemId
        val newResult = repository.fetchPagedEvents(systemId, startDate, endDate, systemDeviceIds)
            .cachedIn(viewModelScope)
        currentEventsResult = newResult
        return newResult
    }

    fun changeListLayout() {
        _listLayout.value = _listLayout.value?.nextState()
    }

    fun selectEvent(event: Event) {
        selectedEvent.value = event
    }

    fun selectSystem(result: Result<HomeguardSystem>) {
        selectedSystemResult.value = result
        resetFilters()
    }

    fun setCameraFilter(result: BooleanArray) {
        selectedCameras.value = result.toList()
    }

    fun setDateFilter(startDateInMillis: Long?, endDateInMillis: Long?) {
        this.startDateInMillis.value = startDateInMillis
        this.endDateInMillis.value = endDateInMillis
    }

    private fun createNavigateResult(
        systems: LiveData<Result<List<HomeguardSystem>>>,
        selectedSystemId: LiveData<Int>,
        selectedEvent: LiveData<Event>
    ) {
        fun combineLatestData(
            systems: LiveData<Result<List<HomeguardSystem>>>,
            selectedSystemId: LiveData<Int>,
            selectedEvent: LiveData<Event>
        ): ViewModelEvent<Result<Pair<String, List<String>>>> {

            val systemsValue = systems.value
            val selectedSystemIdValue = selectedSystemId.value
            val selectedEventValue = selectedEvent.value

            return when {
                systemsValue is Result.Success && selectedSystemIdValue != null && selectedEventValue != null -> {
                    val systemPosition =
                        findSystemPositionById(systemsValue.data, selectedSystemIdValue)
                            ?: return ViewModelEvent(Result.Loading)
                    val system = systemsValue.data[systemPosition]

                    val devicePosition =
                        findSystemDevicePositionById(
                            system.cameras,
                            selectedEventValue.systemDeviceId
                        )
                            ?: return ViewModelEvent(Result.Loading)
                    val liveStream = system.cameras[devicePosition].urls.stream1

                    val eventStreams = emptyList<String>().toMutableList()
                    selectedEventValue.eventRecordings.forEach { eventRecording ->
                        eventRecording.urls.stream?.let {
                            eventStreams.add(it)
                        }
                    }

                    val liveStreamAndEventStreams = Pair(liveStream!!, eventStreams)
                    ViewModelEvent(Result.Success(liveStreamAndEventStreams))
                }
                systemsValue is Result.Error -> {
                    ViewModelEvent(Result.Error(systemsValue.exception))
                }
                else -> {
                    ViewModelEvent(Result.Loading)
                }
            }
        }

        navigateToPlayer.addSource(systems) {
            navigateToPlayer.value = combineLatestData(systems, selectedSystemId, selectedEvent)
        }
        navigateToPlayer.addSource(selectedSystemId) {
            navigateToPlayer.value = combineLatestData(systems, selectedSystemId, selectedEvent)
        }
        navigateToPlayer.addSource(selectedEvent) {
            navigateToPlayer.value = combineLatestData(systems, selectedSystemId, selectedEvent)
        }
    }

    private fun createCamerasFilterResult(
        system: MutableLiveData<Result<HomeguardSystem>>,
        systemDevices: LiveData<Result<List<SystemDevice>>>,
        selectedCameras: LiveData<List<Boolean>>
    ) {
        fun combineLatestData(
            system: MutableLiveData<Result<HomeguardSystem>>,
            systemDevices: LiveData<Result<List<SystemDevice>>>,
            selectedCameras: LiveData<List<Boolean>>
        ): Result<Triple<Int, List<SystemDevice>, List<Boolean>>> {
            val systemValue = system.value
            val systemDevicesValue = systemDevices.value
            var selectedCamerasValue: List<Boolean>? = selectedCameras.value

            return when {
                systemValue is Result.Success && systemDevicesValue is Result.Success && selectedCamerasValue != null -> {
                    if (selectedCamerasValue.isEmpty()) {
                        selectedCamerasValue = Array(systemDevicesValue.data.size) { true }.toList()
                    }
                    Result.Success(
                        Triple(
                            systemValue.data.id,
                            systemDevicesValue.data,
                            selectedCamerasValue
                        )
                    )
                }
                systemValue is Result.Error -> {
                    Result.Error(systemValue.exception)
                }
                systemDevicesValue is Result.Error -> {
                    Result.Error(systemDevicesValue.exception)
                }
                else -> {
                    Result.Loading
                }
            }
        }

        camerasFilterResult.addSource(system) {
            camerasFilterResult.value = combineLatestData(system, systemDevices, selectedCameras)
        }
        camerasFilterResult.addSource(systemDevices) {
            camerasFilterResult.value = combineLatestData(system, systemDevices, selectedCameras)
        }
        camerasFilterResult.addSource(selectedCameras) {
            camerasFilterResult.value = combineLatestData(system, systemDevices, selectedCameras)
        }
    }

    private fun createDateFilterResult(
        system: MutableLiveData<Result<HomeguardSystem>>,
        startDateInMillis: LiveData<Long?>,
        endDateInMillis: LiveData<Long?>
    ) {
        fun combineLatestData(
            system: LiveData<Result<HomeguardSystem>>,
            startDateInMillis: LiveData<Long?>,
            endDateInMillis: LiveData<Long?>
        ): Result<Triple<Int, Long?, Long?>> {
            val systemValue = system.value
            val startDateInMillisValue = startDateInMillis.value
            val endDateInMillisValue = endDateInMillis.value

            return when (systemValue) {
                is Result.Success -> {
                    Result.Success(
                        Triple(
                            systemValue.data.id,
                            startDateInMillisValue,
                            endDateInMillisValue
                        )
                    )
                }
                is Result.Error -> {
                    Result.Error(systemValue.exception)
                }
                else -> {
                    Result.Loading
                }
            }
        }

        datesFilterResult.addSource(system) {
            datesFilterResult.value = combineLatestData(system, startDateInMillis, endDateInMillis)
        }
        datesFilterResult.addSource(startDateInMillis) {
            datesFilterResult.value = combineLatestData(system, startDateInMillis, endDateInMillis)
        }
        datesFilterResult.addSource(endDateInMillis) {
            datesFilterResult.value = combineLatestData(system, startDateInMillis, endDateInMillis)
        }
    }

    private fun resetFilters() {
        selectedCameras.value = emptyList<Boolean>().toMutableList()
        startDateInMillis.value = null
        endDateInMillis.value = null
    }

    private fun findSystemPositionById(systems: List<HomeguardSystem>, systemId: Int): Int? {
        for (i in systems.indices) {
            if (systems[i].id == systemId) return i
        }
        return null
    }

    private fun findSystemDevicePositionById(
        systemDevices: List<SystemDevice>,
        systemDeviceId: Int
    ): Int? {
        for (i in systemDevices.indices) {
            if (systemDevices[i].id == systemDeviceId) return i
        }
        return null
    }

    class Factory(private val repository: DataRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

    enum class ListLayout {
        SINGLE_COLUMN {
            override fun nextState(): ListLayout = TWO_COLUMN
        },
        TWO_COLUMN {
            override fun nextState(): ListLayout = SINGLE_COLUMN
        };

        abstract fun nextState(): ListLayout
    }
}