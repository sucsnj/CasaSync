package com.devminds.casasync.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devminds.casasync.parts.Dependent

class DependentViewModel : ViewModel() {
    private val _dependent = MutableLiveData<Dependent?>()
    val dependent: LiveData<Dependent?> get() = _dependent

    fun setDependent(dependent: Dependent) {
        _dependent.value = dependent
    }

    fun clearDependent() {
        _dependent.value = null
    }
}