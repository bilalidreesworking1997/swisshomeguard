package ch.swisshomeguard.ui.search.cameraselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.swisshomeguard.data.DataRepository
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.model.dummy.DummyCamera
import kotlinx.coroutines.launch
import java.util.*

class CameraSelectionViewModel(repository: DataRepository) : ViewModel() {

    val cameras: LiveData<Result<List<DummyCamera>>> = repository.cameras

    init {
        viewModelScope.launch {
            repository.fetchCameras()
        }
    }

    class Factory(private val repository: DataRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraSelectionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CameraSelectionViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}