package com.devminds.casasync.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devminds.casasync.parts.Task
import java.time.LocalDate
import java.time.LocalTime

class TaskViewModel : ViewModel() {
    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> get() = _task

    fun setTask(task: Task) {
        _task.value = task
    }

    fun clearTask() {
        _task.value = null
    }
}