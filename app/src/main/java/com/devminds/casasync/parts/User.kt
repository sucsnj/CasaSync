package com.devminds.casasync.parts

import androidx.annotation.Keep

// classe que representa um usuário (em memória)
@Keep
data class User(
    var id: String = "",
    var name: String = "",
    var login: String = "",
    var password: String = "",
    val houses: MutableList<House> = mutableListOf()
)
