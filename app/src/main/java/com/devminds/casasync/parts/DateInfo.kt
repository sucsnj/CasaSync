package com.devminds.casasync.parts

data class DateInfo(
    val tomorrow: String,
    val today: String,
    val yesterday: String,
    val hour: Int,
    val minute: Int,
    val hourMinute: String,
    val fullDate: String,
    val dayPlus: String,
    val dayMinus: String
)
