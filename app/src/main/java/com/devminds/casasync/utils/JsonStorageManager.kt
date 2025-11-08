package com.devminds.casasync.utils

import android.content.Context
import android.util.Log
import com.devminds.casasync.parts.User
import com.devminds.casasync.parts.UserIndexEntry
import com.google.gson.Gson

object JsonStorageManager {

    private const val INDEX_FILE = "users_index.json"
    fun saveUser(context: Context, user: User) {
        try {
            val gson = Gson()
            val jsonString = gson.toJson(user)
            val fileName = "user_${user.id}.json"

            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
            }

            updateUserIndex(context, user)

            Log.d("JsonStorageManager", "Usuário ${user.id} salvo com sucesso.")
        } catch (e: Exception) {
            Log.e("JsonStorageManager", "Erro ao salvar usuário ${user.id}", e)
        }
    }


    fun loadUser(context: Context, userId: String): User? {
        return try {
            val gson = Gson()
            val fileName = "user_${userId}.json"
            val json = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            gson.fromJson(json, User::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun getUserIndex(context: Context): List<UserIndexEntry> {
        return try {
            val json = context.openFileInput(INDEX_FILE).bufferedReader().use { it.readText() }
            Gson().fromJson(json, Array<UserIndexEntry>::class.java).toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun updateUserIndex(context: Context, user: User) {
        val index = getUserIndex(context).toMutableList()
        val existing = index.find { it.id == user.id }
        if (existing != null) {
            index.remove(existing)
        }
        index.add(UserIndexEntry(user.id, user.name, user.login, user.password))
        val json = Gson().toJson(index)
        context.openFileOutput(INDEX_FILE, Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    fun authenticateUser(context: Context, login: String, password: String): User? {
        val index = getUserIndex(context)
        val match = index.find { it.login == login && it.password == password }
        return match?.let {
            loadUser(context, it.id)
        }
    }

    fun recoveryUser(context: Context, login: String): User? {
        val index = getUserIndex(context)
        val match = index.find { it.login == login }
        return match?.let {
            loadUser(context, it.id)
        }
    }

    // retorna o Id do usuário que acabou de logar
    fun getUserById(context: Context, userId: String): User? {
        val index = getUserIndex(context)
        val match = index.find { it.id == userId }
        return match?.let {
            loadUser(context, it.id)
        }
    }
}