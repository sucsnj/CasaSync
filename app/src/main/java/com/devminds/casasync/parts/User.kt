package com.devminds.casasync.parts

import androidx.annotation.Keep

// classe que representa um usuário (em memória)
@Keep
data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var login: String = "",
    var password: String = "",
    var photoUrl: String = "",
    val houses: MutableList<House> = mutableListOf()
)
