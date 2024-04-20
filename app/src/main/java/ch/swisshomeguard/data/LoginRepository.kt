package ch.swisshomeguard.data

import androidx.lifecycle.LiveData
import ch.swisshomeguard.utils.ViewModelEvent

interface LoginRepository {
    val loginResult: LiveData<ViewModelEvent<Result<String>>>
    val forgotPasswordResult: LiveData<ViewModelEvent<Result<String>>>

    suspend fun login(email: String, password: String, language: String)
    suspend fun forgotPassword(email: String)
}