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
import com.google.firebase.firestore.SetOptions
import java.util.UUID

object FirestoreHelper {

    val db = FirebaseFirestore.getInstance()

    fun getUserById(userId: String, onResult: (User?) -> Unit) {
        val userDoc = db.collection("users").document(userId)
        userDoc.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = User(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        password = document.getString("password") ?: "",
                        houses = mutableListOf()
                    )
                    onResult(user)
                } else {
                    Log.w("FirestoreHelper", "Usuário não encontrado com ID: $userId")
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreHelper", "Erro ao buscar usuário por ID", exception)
                onResult(null)
            }
    }

    fun getUserByEmail(email: String, onResult: (Boolean) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("FirestoreHelper", "Email encontrado")
                val exists = !documents.isEmpty
                onResult(exists)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreHelper", "Email não encontrado", exception)
                onResult(false)
            }
    }

    fun createUser(name: String, email: String, password: String, onResult: (User?) -> Unit) {
        val newUser = hashMapOf(
            "name" to name,
            "email" to email,
            "password" to password
        )
        db.collection("users")
            .add(newUser)
            .addOnSuccessListener { documentReference ->
                val user = User(
                    id = documentReference.id,
                    name = name,
                    email = email,
                    password = password,
                    houses = mutableListOf()
                )
                onResult(user)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "Erro ao criar usuário", e)
                onResult(null)
            }
    }

    fun syncUserToFirestore(user: User) {
        val db = FirebaseFirestore.getInstance()

        if (user.id.isBlank()) {
            Log.e("Firestore", "ID do usuário está nulo ou vazio. Abortando sincronização.")
            return
        }

        val userDoc = db.collection("users").document(user.id)

        // Salva dados básicos do usuário com merge para não apagar campos existentes
        val userMap = mapOf(
            "id" to user.id,
            "name" to user.name,
            "login" to user.login,
            "password" to user.password
        )

        userDoc.set(userMap, SetOptions.merge())
            .addOnSuccessListener { Log.d("Firestore", "Usuário sincronizado: ${user.id}") }
            .addOnFailureListener { Log.e("Firestore", "Erro ao salvar usuário", it) }

        // Sincroniza casas apenas se houver casas locais
        if (user.houses.isNotEmpty()) {
            val casasRef = userDoc.collection("houses")

            casasRef.get().addOnSuccessListener { snapshot ->
                val firestoreHouseIds = snapshot.documents.map { it.id }
                val localHouseIds = user.houses.map { it.id }

                // Remove casas que não existem mais localmente
                firestoreHouseIds.filterNot { it in localHouseIds }.forEach { idToDelete ->
                    casasRef.document(idToDelete).delete()
                }

                // Atualiza ou cria casas
                user.houses.forEach { house ->
                    if (house.id.isBlank()) {
                        house.id = UUID.randomUUID().toString()
                    }
                    val houseDoc = casasRef.document(house.id)
                    val houseMap = mapOf(
                        "name" to house.name,
                        "ownerId" to house.ownerId
                    )
                    houseDoc.set(houseMap)

                    // Sincroniza dependentes apenas se houver
                    if (house.dependents.isNotEmpty()) {
                        val dependentsRef = houseDoc.collection("dependentes")

                        dependentsRef.get().addOnSuccessListener { depSnapshot ->
                            val firestoreDepIds = depSnapshot.documents.map { it.id }
                            val localDepIds = house.dependents.map { it.id }

                            firestoreDepIds.filterNot { it in localDepIds }.forEach { idToDelete ->
                                dependentsRef.document(idToDelete).delete()
                            }

                            house.dependents.forEach { dep ->
                                val depDoc = dependentsRef.document(dep.id)
                                val depMap = mapOf("name" to dep.name)
                                depDoc.set(depMap)

                                // Sincroniza tarefas apenas se houver
                                if (dep.tasks.isNotEmpty()) {
                                    val tasksRef = depDoc.collection("tarefas")

                                    tasksRef.get().addOnSuccessListener { taskSnapshot ->
                                        val firestoreTaskIds = taskSnapshot.documents.map { it.id }
                                        val localTaskIds = dep.tasks.map { it.id }

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
        }
    }
}