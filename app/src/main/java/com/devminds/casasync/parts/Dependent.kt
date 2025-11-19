package com.devminds.casasync.parts

import androidx.annotation.Keep

@Keep
data class Dependent(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var active: Boolean = false,
    var houseId: String = "",
    var photo: String = "",
    var passcode: String = "",
    var tasks: MutableList<Task> = mutableListOf()
)
