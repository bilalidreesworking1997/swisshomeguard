package ch.swisshomeguard.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.swisshomeguard.model.user.ForgotPasswordRequest
import ch.swisshomeguard.model.user.UserAuthRequest
import ch.swisshomeguard.utils.ViewModelEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics

class DefaultLoginRepository(private val loginService: LoginService) : LoginRepository {
    private val _loginResult = MutableLiveData<ViewModelEvent<Result<String>>>()
    private val _forgotPasswordResult = MutableLiveData<ViewModelEvent<Result<String>>>()
    override val loginResult: LiveData<ViewModelEvent<Result<String>>> = _loginResult
    override val forgotPasswordResult: LiveData<ViewModelEvent<Result<String>>> =
        _forgotPasswordResult

    override suspend fun login(email: String, password: String, language: String) {
        _loginResult.value = ViewModelEvent(Result.Loading)
        try {
            val data = loginService.login(UserAuthRequest(email, password, language))
            _loginResult.value = ViewModelEvent(Result.Success(data.access_token))
        } catch (exception: Exception) {
            _loginResult.value = ViewModelEvent(Result.Error(exception))
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }

    //forgot password API handlingsss
    override suspend fun forgotPassword(email: String) {
        _forgotPasswordResult.value = ViewModelEvent(Result.Loading)
        try {
            val data = loginService.forgotPassword(ForgotPasswordRequest(email))
            _forgotPasswordResult.value = ViewModelEvent(Result.Success(data.toString()))
        } catch (exception: Exception) {
            _forgotPasswordResult.value = ViewModelEvent(Result.Error(exception))
            exception.message?.let { FirebaseCrashlytics.getInstance().log(it) }
        }
    }
}