package ch.swisshomeguard.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.swisshomeguard.utils.ViewModelEvent

class DummyLoginRepository : LoginRepository {
    private val _loginResult = MutableLiveData<ViewModelEvent<Result<String>>>()
    override val loginResult: LiveData<ViewModelEvent<Result<String>>> = _loginResult
    override val forgotPasswordResult: LiveData<ViewModelEvent<Result<String>>>
        get() = TODO("Not yet implemented")

    override suspend fun login(email: String, password: String, language: String) {
        _loginResult.value = ViewModelEvent(Result.Success("Token"))
    }

    override suspend fun forgotPassword(email: String) {
        TODO("Not yet implemented")
    }
}