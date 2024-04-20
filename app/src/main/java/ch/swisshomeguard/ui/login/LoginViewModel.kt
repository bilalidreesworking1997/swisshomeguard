package ch.swisshomeguard.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.swisshomeguard.data.LoginRepository
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.utils.ViewModelEvent
import kotlinx.coroutines.launch
import java.util.*

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {

    val loginResult: LiveData<ViewModelEvent<Result<String>>> = repository.loginResult
    val forgotPasswordResult: LiveData<ViewModelEvent<Result<String>>> =
        repository.forgotPasswordResult

    fun login(email: String, password: String, deviceLanguage: String) {
        // TODO do client side check on email and password before calling the server
        viewModelScope.launch {
            val language = chooseLoginLanguage(deviceLanguage)
            repository.login(email, password, language)
        }
    }

    fun forgotPassword(email: String) {
        // TODO do client side check on email and password before calling the server
        viewModelScope.launch {
            repository.forgotPassword(email)
        }
    }

    /**
     * Chooses the language to be sent to the server during login.
     * This language will be used for the push notification options in Settings.
     * Supported languages are: de (default), en, fr
     */
    private fun chooseLoginLanguage(deviceLanguage: String) = when (deviceLanguage) {
        Locale("en").language -> "en"
        Locale("fr").language -> "fr"
        else -> "de"
    }

    class Factory(private val repository: LoginRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}