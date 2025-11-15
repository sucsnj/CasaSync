package com.devminds.casasync.parts

import androidx.annotation.Keep

@Keep
data class Task(
    var id: String = "",
    var houseId: String = "",
    var dependentId: String = "",
    var name: String = "",
    var description: String = "",
    var previsionDate: String? = null,
    var previsionHour: String? = null,
    var startDate: String? = null,
    var finishDate: String? = null
)
