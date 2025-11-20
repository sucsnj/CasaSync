package com.devminds.casasync.utils

import android.util.Log
import com.devminds.casasync.parts.Dependent
import com.devminds.casasync.parts.User
import com.google.firebase.firestore.FirebaseFirestore

class Auth {

    fun authenticateWithFirestore(email: String, password: String, onResult: (User?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val hashedPassword = hashPassword(password)

        db.collection("users")
            .whereEqualTo("email", email)
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

    fun authenticateDepWithFirestore(email: String, password: String, onResult: (Dependent?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val hashedPassword = password
//        val hashedPassword = hashPassword(password) // @TODO corrigir

        db.collection("dependents")
            .whereEqualTo("email", email)
            .whereEqualTo("passcode", hashedPassword)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val dependent = result.documents[0].toObject(Dependent::class.java)?.apply {
                        id = result.documents[0].id
                    }
                    onResult(dependent)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                Log.e("Auth", "Erro ao autenticar com Firestore", it)
                onResult(null)
            }
    }

    // gera um hash da senha
    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
