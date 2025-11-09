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

        db.collection("users").add(user)
            .addOnSuccessListener { Log.d("Firestore", "Adicionado: ${it.id}") }
            .addOnFailureListener { Log.e("Firestore", "Erro: ", it) }
    }

    fun readUsers() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    Log.d("Firestore", "${doc.id} => ${doc.data}")
                }
            }
            .addOnFailureListener { Log.e("Firestore", "Erro: ", it) }
    }

    // TODO modularizar
    fun syncUserToFirestore(user: User) {
        val db = FirebaseFirestore.getInstance()
        if (user.id.isBlank()) {
            Log.e("Firestore", "ID do usuário está nulo ou vazio. Abortando sincronização.")
            return
        }

        val userDoc = db.collection("users").document(user.id)

        // Salva dados básicos do usuário
        val userMap = mapOf(
            "name" to user.name,
            "login" to user.login,
            "password" to user.password
        )

        userDoc.set(userMap)
            .addOnSuccessListener { Log.d("Firestore", "Usuário sincronizado: ${user.id}") }
            .addOnFailureListener { Log.e("Firestore", "Erro ao salvar usuário", it) }

        // Sincroniza casas
        val casasRef = userDoc.collection("casas")
        casasRef.get().addOnSuccessListener { snapshot ->
            val firestoreHouseIds = snapshot.documents.map { it.id }
            val localHouseIds = user.houses.map { it.id }

            // Remove casas que não existem mais localmente
            firestoreHouseIds.filterNot { it in localHouseIds }.forEach { idToDelete ->
                casasRef.document(idToDelete).delete()
            }

            // Atualiza ou cria casas
            user.houses.forEach { house ->
                val houseDoc = casasRef.document(house.id)
                val houseMap = mapOf(
                    "name" to house.name,
                    "ownerId" to house.ownerId
                )
                houseDoc.set(houseMap)

                // Sincroniza dependentes
                val dependentsRef = houseDoc.collection("dependentes")
                dependentsRef.get().addOnSuccessListener { depSnapshot ->
                    val firestoreDepIds = depSnapshot.documents.map { it.id }
                    val localDepIds = house.dependents.map { it.id }

                    // Remove dependentes que não existem mais localmente
                    firestoreDepIds.filterNot { it in localDepIds }.forEach { idToDelete ->
                        dependentsRef.document(idToDelete).delete()
                    }

                    house.dependents.forEach { dep ->
                        val depDoc = dependentsRef.document(dep.id)
                        val depMap = mapOf("name" to dep.name)
                        depDoc.set(depMap)

                        // Sincroniza tarefas
                        val tasksRef = depDoc.collection("tarefas")
                        tasksRef.get().addOnSuccessListener { taskSnapshot ->
                            val firestoreTaskIds = taskSnapshot.documents.map { it.id }
                            val localTaskIds = dep.tasks.map { it.id }

                            // Remove tarefas que não existem mais localmente
                            firestoreTaskIds.filterNot { it in localTaskIds }.forEach { idToDelete ->
                                tasksRef.document(idToDelete).delete()
                            }

                            dep.tasks.forEach { task ->
                                val taskDoc = tasksRef.document(task.id)
                                val taskMap = mapOf(
                                    "name" to task.name,
                                    "description" to task.description,
                                    "previsionDate" to task.previsionDate,
                                    "previsionHour" to task.previsionHour,
                                    "startDate" to task.startDate,
                                    "finishDate" to task.finishDate
                                )
                                taskDoc.set(taskMap)
                            }
                        }
                    }
                }
            }
        }
    }
}