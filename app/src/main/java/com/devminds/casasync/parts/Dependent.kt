package com.devminds.casasync.parts

data class Dependent(
    val id: String,
    var name: String,
    val tasks: MutableList<Task> = mutableListOf()
)