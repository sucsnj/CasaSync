package com.devminds.casasync.parts

data class Task(
    val id: String,
    var name: String,
    val description: String,
    val date: String,
    var finishDate: String?,
    var hourFinish: String?,
    var startDate: String?
)