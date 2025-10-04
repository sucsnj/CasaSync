package com.devminds.casasync.utils

import android.content.Context
import com.devminds.casasync.parts.User
import com.google.gson.Gson

object JsonStorageManager {

//    fun saveUser(context: Context, user: User, fileName: String = "user_data.json") {
//
//        val gson = Gson()
//        val user = userViewModel.user.value
//        val jsonString = gson.toJson(user)
//
//        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
//            it.write(jsonString.toByteArray())
//        }
//    }

    // revisar
     fun saveUser(context: Context, user: User, fileName: String = "user_data.json") {
         val gson = Gson()
         val jsonString = gson.toJson(user)
         context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
             it.write(jsonString.toByteArray())
         }
     }

    fun loadUser(context: Context, fileName: String = "user_data.json"): User? {

        return try {
            val gson = Gson()
            val json = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}