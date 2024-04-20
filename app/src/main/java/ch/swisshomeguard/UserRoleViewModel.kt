package ch.swisshomeguard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserRoleViewModel : ViewModel() {

    private val _isBasicUser = MutableLiveData<Boolean>()
    val isBasicUser: LiveData<Boolean> = _isBasicUser

    // Set only if the value is different from the previous one
    // to prevent the bottom navigation bar from refreshing when the tab items haven't changed
    fun setBasicUser(basicUser: Boolean) {
        if (basicUser != _isBasicUser.value) {
            _isBasicUser.value = basicUser
        }
    }
}