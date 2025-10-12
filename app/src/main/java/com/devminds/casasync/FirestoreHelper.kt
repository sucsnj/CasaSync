package com.devminds.casasync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreHelper {

    fun writeUser() {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf("nome" to "Carlos", "cidade" to "Jaboatão")

        db.collection("usuarios").add(user)
            .addOnSuccessListener { Log.d("Firestore", "Adicionado: ${it.id}") }
            .addOnFailureListener { Log.e("Firestore", "Erro: ", it) }
    }

    fun readUsers() {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    Log.d("Firestore", "${doc.id} => ${doc.data}")
                }
            }
            .addOnFailureListener { Log.e("Firestore", "Erro: ", it) }
    }
}