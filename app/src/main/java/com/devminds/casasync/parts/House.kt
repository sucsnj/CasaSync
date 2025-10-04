package com.devminds.casasync.parts

data class House(
    val id: String,
    val name: String,
    val dependents: MutableList<Dependent> = mutableListOf()
)