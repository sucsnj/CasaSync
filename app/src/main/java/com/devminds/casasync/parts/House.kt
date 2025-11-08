package com.devminds.casasync.parts

import androidx.annotation.Keep

@Keep
data class House(
    val id: String = "",
    var name: String = "",
    val ownerId: String = "",
    val dependents: MutableList<Dependent> = mutableListOf()
)
