package com.devminds.casasync.views

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devminds.casasync.parts.House
import com.devminds.casasync.parts.User
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.FirestoreHelper
import com.google.gson.Gson
import com.devminds.casasync.utils.DialogUtils

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

            // atualiza o firestore
            FirestoreHelper.syncUserToFirestore(it)
        }
    }

    fun persistAndSyncUser(context: Context) {
        val user = user.value ?: return
        FirestoreHelper.syncUserToFirestore(user)
    }

    fun deleteHouse(context: Context, houseId: String) {
        val user = user.value ?: return
        FirestoreHelper.syncUserToFirestoreRemoveHouse(context, user, houseId) 
    }

    fun deleteDependent(context: Context, houseId: String) {
        val user = user.value ?: return
        FirestoreHelper.syncUserToFirestoreRemoveDependent(context, user, houseId)
    }

    fun deleteTask(context: Context, taskId: String) {
        val user = user.value ?: return
        FirestoreHelper.syncUserToFirestoreRemoveTask(context, user, taskId)
    }

    fun persistUserPassword(context: Context, user: User?, password: String) {
        user?.let {
            it.password = password
            JsonStorageManager.saveUser(context, it)
        }
    }
}