package com.devminds.casasync.views

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EasterEggViewModel : ViewModel() {
    val easterEggActive = MutableLiveData<Boolean>()
}
