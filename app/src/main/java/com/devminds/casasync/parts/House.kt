package com.devminds.casasync.parts

data class House(
    val id: String,
    var name: String,
    val ownerId: String,
    val dependents: MutableList<Dependent> = mutableListOf()
)