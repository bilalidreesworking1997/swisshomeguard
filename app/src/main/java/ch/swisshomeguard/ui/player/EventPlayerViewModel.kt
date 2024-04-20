package ch.swisshomeguard.ui.player

import android.util.Log
import androidx.lifecycle.*
import ch.swisshomeguard.data.DataRepository
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.model.events.Event
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.timerTask

class EventPlayerViewModel(private val repository: DataRepository) : ViewModel() {

    private val _event = MutableLiveData<Result<List<Event>>>()
    val event: LiveData<Result<List<Event>>> = _event

    private var timer: Timer? = null

    fun fetchVideo(systemId: Int, eventId: Int) {
        viewModelScope.launch {
            _event.value = Result.Loading
            Log.d("VIDEO_TAG", "ViewModel: Fetch video $repository")
            _event.value = repository.fetchEventDetails(systemId, eventId.toString())
        }
    }

    // https://stackoverflow.com/questions/57968658/how-to-use-timer-scheduleatfixedrate-in-kotlin
    fun startKeepAlive(keepAliveUrl: String, intervalInSeconds: Int) {
        Log.d("VIDEO_TAG", "ViewModel: Start keep alive")
        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(timerTask {
            viewModelScope.launch {
                Log.d("VIDEO_TAG", "ViewModel: Keep alive $keepAliveUrl")
                repository.keepVideoAlive(keepAliveUrl)
            }
        }, 0L, intervalInSeconds * 1000L)
    }

    fun stopKeepAlive() {
        Log.d("VIDEO_TAG", "ViewModel: Stop keep alive")
        timer?.cancel()
    }

    class Factory(private val repository: DataRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventPlayerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EventPlayerViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}
