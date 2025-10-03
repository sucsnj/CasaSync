package com.devminds.casasync.parts

data class Dependent(
    val id: String,
    val name: String,
    val task: MutableList<Task> = mutableListOf()
)