package com.devminds.casasync.parts

data class Dependent(
    var id: String = "",
    var name: String = "",
    var tasks: MutableList<Task> = mutableListOf()
)