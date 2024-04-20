package ch.swisshomeguard.ui.cameras

import android.util.Log
import androidx.lifecycle.*
import ch.swisshomeguard.TOKEN_REFRESH_ERROR
import ch.swisshomeguard.data.DataRepository
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.model.system.HomeguardSystem
import ch.swisshomeguard.model.system.SystemDevice
import ch.swisshomeguard.utils.ViewModelEvent
import kotlinx.coroutines.launch

class CamerasViewModel(private val repository: DataRepository) : ViewModel() {

    private val _listLayout = MutableLiveData<ListLayout>()
    val listLayout: LiveData<ListLayout> = _listLayout

    private val _navigateToCameraDetailsEvent = MutableLiveData<ViewModelEvent<SystemDevice>>()
    val navigateToCameraDetailsViewModelEvent: LiveData<ViewModelEvent<SystemDevice>> =
        _navigateToCameraDetailsEvent

    private val systems: LiveData<Result<List<HomeguardSystem>>> = repository.systems
    private val selectedSystemPosition = MutableLiveData<Int>()

    val selectedSystemResult = MediatorLiveData<Result<HomeguardSystem>>()
    val camerasResult = MediatorLiveData<Result<List<SystemDevice>>>()

    val systemNamesResult: LiveData<Result<List<String>>> = Transformations.map(systems) {
        when (it) {
            is Result.Success -> {
                val systemNames = ArrayList<String>()
                it.data.forEach { system ->
                    systemNames.add(system.toString())
                }
                Result.Success(systemNames)
            }
            is Result.Loading -> Result.Loading
            is Result.Error -> Result.Error(it.exception)
        }
    }

    val isLogout: LiveData<ViewModelEvent<Boolean>> = Transformations.map(systems) {
        if (it is Result.Error && it.exception.message == TOKEN_REFRESH_ERROR) {
            ViewModelEvent(true)
        } else {
            ViewModelEvent(false)
        }
    }

    init {
        reset()
        createSelectedSystemResult(systems, selectedSystemPosition)
        createCamerasResult(systems, selectedSystemPosition)
    }

    fun reset() {
        selectedSystemPosition.value = DEFAULT_SYSTEM_POSITION
        _listLayout.value = DEFAULT_LIST_LAYOUT
        fetchSystems()
    }

    fun fetchSystems() {
        Log.d("RELOAD_TAG", "CamerasViewModel: fetchSystems()")
        viewModelScope.launch {
            repository.fetchSystems()
        }
    }

    private fun createSelectedSystemResult(
        systems: LiveData<Result<List<HomeguardSystem>>>,   // comes from repository
        selectedSystemPosition: LiveData<Int>               // comes from CamerasFragment#selectSystem()
    ) {
        fun combineLatestData(
            systems: LiveData<Result<List<HomeguardSystem>>>,
            selectedSystemPosition: LiveData<Int>
        ): Result<HomeguardSystem> {

            val systemsValue = systems.value
            val selectedSystemPositionValue = selectedSystemPosition.value

            return when {
                systemsValue is Result.Success && selectedSystemPositionValue != null -> {
                    val system = systemsValue.data[selectedSystemPositionValue]
                    repository.selectSystem(system.id)
                    Result.Success(system)
                }
                systemsValue is Result.Error -> Result.Error(systemsValue.exception)
                else -> Result.Loading
            }
        }

        selectedSystemResult.addSource(systems) {
            selectedSystemResult.value = combineLatestData(systems, selectedSystemPosition)
        }
        selectedSystemResult.addSource(selectedSystemPosition) {
            selectedSystemResult.value = combineLatestData(systems, selectedSystemPosition)
        }
    }

    private fun createCamerasResult(
        systems: LiveData<Result<List<HomeguardSystem>>>,
        selectedSystemPosition: LiveData<Int>
    ) {
        fun combineLatestData(
            systems: LiveData<Result<List<HomeguardSystem>>>,
            selectedSystemPosition: LiveData<Int>
        ): Result<List<SystemDevice>> {

            val systemsValue = systems.value
            val selectedSystemPositionValue = selectedSystemPosition.value

            return when {
                systemsValue is Result.Success && selectedSystemPositionValue != null -> {
                    val cameras = systemsValue.data[selectedSystemPositionValue].cameras
                    Result.Success(cameras)
                }
                systemsValue is Result.Error -> Result.Error(systemsValue.exception)
                else -> Result.Loading
            }
        }

        camerasResult.addSource(systems) {
            camerasResult.value = combineLatestData(this.systems, this.selectedSystemPosition)
        }
        camerasResult.addSource(selectedSystemPosition) {
            camerasResult.value = combineLatestData(this.systems, this.selectedSystemPosition)
        }
    }

    fun selectSystem(position: Int) {
        selectedSystemPosition.value = position
    }

    fun toggleListLayout() {
        _listLayout.value = _listLayout.value?.nextState()
    }

    fun navigateToCameraDetails(cameraId: SystemDevice) {
        Log.d("LOG_CAMERA", "Navigate to camera $cameraId")
        _navigateToCameraDetailsEvent.value = ViewModelEvent(cameraId)
    }

    class Factory(private val repository: DataRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CamerasViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CamerasViewModel(repository) as T
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

    companion object {
        const val DEFAULT_SYSTEM_POSITION = 0
        val DEFAULT_LIST_LAYOUT = ListLayout.SINGLE_COLUMN
    }
}