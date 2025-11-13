package com.devminds.casasync.parts

data class Dependent(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var houseId: String = "",
    var tasks: MutableList<Task> = mutableListOf()
)