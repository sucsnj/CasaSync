package com.devminds.casasync.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devminds.casasync.parts.House

class HouseViewModel : ViewModel() {
    private val _house = MutableLiveData<House?>()
    val house: LiveData<House?> get() = _house

    fun setHouse(house: House) {
        _house.value = house
    }
}
