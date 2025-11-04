package com.devminds.casasync.parts

import androidx.annotation.Keep

// classe que representa um usuário (em memória)
@Keep
data class User(
    var id: String = "",
    var name: String = "",
    var login: String = "",
    var password: String = "", // Mesmo que não use, precisa estar aqui para o mapeamento
    val houses: MutableList<House> = mutableListOf()
)
