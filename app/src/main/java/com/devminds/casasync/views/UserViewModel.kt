package com.devminds.casasync.views

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devminds.casasync.parts.User
import com.devminds.casasync.FirestoreHelper
import com.devminds.casasync.R
import com.devminds.casasync.fragments.HomeFragment
import com.devminds.casasync.parts.Task
import com.google.firebase.firestore.FirebaseFirestore

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

    fun updateTask(houseId: String, dependentId: String, updatedTask: Task) {
        val currentUser = _user.value ?: return
        FirestoreHelper.persistTaskForUser(currentUser, houseId, dependentId, updatedTask)
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
