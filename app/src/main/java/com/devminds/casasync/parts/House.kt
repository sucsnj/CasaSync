package com.devminds.casasync.parts

data class House(
    val id: String,
    val name: String,
    val ownerId: String,
    val dependents: MutableList<Dependent> = mutableListOf()
)