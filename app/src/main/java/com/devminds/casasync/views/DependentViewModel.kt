package com.devminds.casasync.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devminds.casasync.parts.Dependent
import com.devminds.casasync.parts.Task

class DependentViewModel : ViewModel() {
    private val _dependent = MutableLiveData<Dependent?>()
    val dependent: LiveData<Dependent?> get() = _dependent

    fun setDependent(dependent: Dependent) {
        _dependent.value = dependent
    }

    fun updateTask(updatedTask: Task) {
        val current = _dependent.value ?: return
        val index = current.tasks.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            current.tasks[index] = updatedTask
            _dependent.value = current // dispara o observer
        }
    }
}