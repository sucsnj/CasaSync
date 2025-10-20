package com.devminds.casasync.views

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devminds.casasync.parts.House
import com.devminds.casasync.parts.User
import com.devminds.casasync.utils.JsonStorageManager

class UserViewModel : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    fun setUser(user: User) {
        _user.value = user
    }

    fun clearUser() {
        _user.value = null
    }

    fun deleteHouse(house: House, context: Context) {
        user.value?.let { user ->
            user.houses.remove(house)
            JsonStorageManager.saveUser(context, user)
        }
    }

    fun persistUser(context: Context, user: User?) {
        user?.let {
            JsonStorageManager.saveUser(context, it)
        }
    }

    fun persistUserPassword(context: Context, user: User?, password: String) {
        user?.let {
            it.password = password
            JsonStorageManager.saveUser(context, it)
        }
    }
}