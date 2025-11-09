package com.devminds.casasync.utils

import android.content.Context
import android.util.Log
import com.devminds.casasync.parts.User
import com.google.firebase.firestore.FirebaseFirestore

class Auth {

    fun authenticateWithFirestore(context: Context, login: String, password: String, onResult: (User?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val hashedPassword = JsonStorageManager.hashPassword(password)

        db.collection("users")
            .whereEqualTo("login", login)
            .whereEqualTo("password", hashedPassword)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val user = result.documents[0].toObject(User::class.java)?.apply {
                        id = result.documents[0].id
                    }
                    onResult(user)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                Log.e("Auth", "Erro ao autenticar com Firestore", it)
                onResult(null)
            }
    }
}