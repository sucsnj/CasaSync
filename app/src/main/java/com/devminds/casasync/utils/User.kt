package com.devminds.casasync.utils

// classe que representa um usuário (em memória)
data class User(
    val name: String,
    var login: String,
    var password: String,
    val houses: MutableList<House> = mutableListOf()
) {
    companion object {
        val users = mutableListOf(
            User("admin", "admin", "admin")
        )
    }
}
