package com.devminds.casasync.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devminds.casasync.FirestoreHelper
import com.devminds.casasync.parts.Dependent
import com.devminds.casasync.parts.Task

class DependentViewModel : ViewModel() {
    private val _dependent = MutableLiveData<Dependent?>()
    val dependent: LiveData<Dependent?> get() = _dependent

    private val _tasksLiveData = MutableLiveData<List<Task>>()
    @Suppress("unused")
    val tasksLiveData: LiveData<List<Task>> get() = _tasksLiveData

    fun setDependent(dependent: Dependent) {
        _dependent.value = dependent
    }

    fun persistAndSyncDependent() {
        val dependent = dependent.value ?: return
        FirestoreHelper.syncDependentToFirestore(dependent)
    }

    fun updateTask(updatedTask: Task) {
        val current = _dependent.value ?: return
        val index = current.tasks.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            current.tasks[index] = updatedTask
            _dependent.value = current // dispara o observer

            // sincroniza no Firestore (dependents + users)
            FirestoreHelper.persistTaskForDependent(current, updatedTask)
        }
    }

    fun deleteTask(taskId: String) {
        val dependent = dependent.value ?: return

        dependent.tasks.removeAll { it.id == taskId } // remove da lista local
        setDependent(dependent) // atualiza o LiveData

        FirestoreHelper.syncUserToFirestoreRemoveTaskDep(dependent, taskId)
    }
}
