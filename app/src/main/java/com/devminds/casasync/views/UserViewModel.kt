package com.devminds.casasync.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devminds.casasync.parts.User
import com.devminds.casasync.FirestoreHelper

class UserViewModel : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    fun setUser(user: User) {
        _user.value = user
    }

    fun persistAndSyncUser() {
        val user = user.value ?: return
        FirestoreHelper.syncUserToFirestore(user)
    }

    fun deleteHouse(houseId: String) {
        val user = user.value ?: return
        FirestoreHelper.syncUserToFirestoreRemoveHouse(user, houseId)
    }

    fun deleteDependent(houseId: String, dependentId: String) {
        val user = user.value ?: return
        FirestoreHelper.syncUserToFirestoreRemoveDependent(user, houseId, dependentId)
    }

    fun deleteTask(houseId: String, depId: String, taskId: String) {
        val user = user.value ?: return
        FirestoreHelper.syncUserToFirestoreRemoveTask(user, houseId, depId, taskId)
    }
}
