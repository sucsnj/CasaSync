package com.devminds.casasync.parts

data class Task(
    val id: String,
    var name: String,
    val description: String,
    var previsionDate: String?,
    var previsionHour: String?,
    var startDate: String?,
    var finishDate: String?
)