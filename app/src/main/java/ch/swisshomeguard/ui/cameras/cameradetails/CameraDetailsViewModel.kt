package ch.swisshomeguard.ui.cameras.cameradetails

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import ch.swisshomeguard.data.DataRepository
import ch.swisshomeguard.model.events.Event
import ch.swisshomeguard.utils.ViewModelEvent
import ch.swisshomeguard.utils.convertDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CameraDetailsViewModel(private val repository: DataRepository, private val app: Application) :
    AndroidViewModel(app) {

    private val _navigateToEventPlayer = MutableLiveData<ViewModelEvent<Pair<List<String>, Int>>>()
    val navigateToEventPlayer: LiveData<ViewModelEvent<Pair<List<String>, Int>>> =
        _navigateToEventPlayer

    private var currentSystemId: Int? = null
    private var currentEventsResult: Flow<PagingData<UiModel>>? = null

    fun fetchEvents(systemId: Int, systemDeviceIds: List<Int>): Flow<PagingData<UiModel>> {
        // TODO is this necessary?
        val lastResult = currentEventsResult
//        if (systemId == currentSystemId && lastResult != null) {
//            return lastResult
//        }
        currentSystemId = systemId
        val newResult = repository.fetchPagedEvents(systemId, systemDeviceIds = systemDeviceIds)
            .map { pagingData -> pagingData.map { UiModel.EventItem(it) } }
            .map {
                it.insertSeparators<UiModel.EventItem, UiModel> { before, after ->
                    if (after == null) {
                        // we're at the end of the list
                        return@insertSeparators null
                    }

                    if (before == null) {
                        // we're at the beginning of the list
                        return@insertSeparators UiModel.SeparatorItem(
                            convertDate(
                                app,
                                after.event.eventCreatedAt
                            )
                        )
                    }
                    // check between 2 items
                    val beforeDate = convertDate(app, before.event.eventCreatedAt)
                    val afterDate = convertDate(app, after.event.eventCreatedAt)
                    if (beforeDate != afterDate) {
                        UiModel.SeparatorItem(afterDate)
                    } else {
                        // no separator
                        null
                    }
                }
            }
            .cachedIn(viewModelScope)
        currentEventsResult = newResult
        return newResult
    }

    fun navigateToEventPlayer(streams: List<String>, position: Int) {
        _navigateToEventPlayer.value = ViewModelEvent(Pair(streams, position))
    }

    class Factory(private val repository: DataRepository, private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraDetailsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CameraDetailsViewModel(repository, application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

    sealed class UiModel {
        data class EventItem(val event: Event) : UiModel()
        data class SeparatorItem(val description: String) : UiModel()
    }

}

