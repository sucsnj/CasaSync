package com.devminds.casasync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.devminds.casasync.parts.User
import com.devminds.casasync.parts.House
import com.devminds.casasync.parts.Dependent
import com.devminds.casasync.parts.Task
import com.devminds.casasync.parts.UserIndexEntry
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.utils.Utils
import com.devminds.casasync.views.UserViewModel
import com.devminds.casasync.views.HouseViewModel
import com.devminds.casasync.views.DependentViewModel
import com.devminds.casasync.views.TaskViewModel
import android.content.Context

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

    fun syncUserToFirestore(user: User) {
        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("usuarios").document(user.id)

        // Salva dados básicos do usuário
        val userMap = mapOf(
            "name" to user.name,
            "login" to user.login,
            "password" to user.password
        )

        userDoc.set(userMap)
            .addOnSuccessListener { Log.d("Firestore", "Usuário sincronizado: ${user.id}") }
            .addOnFailureListener { Log.e("Firestore", "Erro ao salvar usuário", it) }

        // Casas
        user.houses.forEach { house ->
            val houseDoc = userDoc.collection("casas").document(house.id)
            val houseMap = mapOf(
                "name" to house.name,
                "ownerId" to house.ownerId
            )

            houseDoc.set(houseMap)
                .addOnSuccessListener { Log.d("Firestore", "Casa sincronizada: ${house.id}") }

            // Dependentes
            house.dependents.forEach { dep ->
                val depDoc = houseDoc.collection("dependentes").document(dep.id)
                val depMap = mapOf("name" to dep.name)

                depDoc.set(depMap)
                    .addOnSuccessListener { Log.d("Firestore", "Dependente sincronizado: ${dep.id}") }

                // Tarefas
                dep.tasks.forEach { task ->
                    val taskDoc = depDoc.collection("tarefas").document(task.id)
                    val taskMap = mapOf(
                        "name" to task.name,
                        "description" to task.description,
                        "previsionDate" to task.previsionDate,
                        "previsionHour" to task.previsionHour,
                        "startDate" to task.startDate,
                        "finishDate" to task.finishDate
                    )

                    taskDoc.set(taskMap)
                        .addOnSuccessListener { Log.d("Firestore", "Tarefa sincronizada: ${task.id}") }
                }
            }
        }
    }
}