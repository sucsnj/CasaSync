package com.devminds.casasync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.devminds.casasync.parts.User
import com.devminds.casasync.parts.House
import com.devminds.casasync.parts.Dependent
// import com.devminds.casasync.parts.Task as TaskPart
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
import com.devminds.casasync.utils.DialogUtils

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

    fun getUserByEmail(email: String, onResult: (User?) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val user = doc.toObject(User::class.java)
                    onResult(user)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreHelper", "Erro ao buscar usuário", exception)
                onResult(null)
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

    fun updateUserPassword(user: User?, password: String) {
        val userId = user?.id.toString()
        val userDoc = db.collection("users").document(userId)
        val userMap = mapOf(
            "password" to password
        )
        userDoc.update(userMap)
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
                        "id" to house.id,
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
                                val depMap = mapOf(
                                    "id" to dep.id,
                                    "name" to dep.name,
                                    "houseId" to dep.houseId,
                                    "email" to dep.email
                                )
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
                                                "id" to task.id,
                                                "dependentId" to task.dependentId,
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

    fun syncUserToFirestoreRemoveHouse(context: Context, user: User, houseId: String) {
        if (user.id.isBlank()) {
            Log.e("Firestore", "ID do usuário está nulo ou vazio. Abortando sincronização.")
            return
        }

        val houseRef = db.collection("users")
            .document(user.id)
            .collection("houses")
            .document(houseId)

        val dependentsRef = houseRef.collection("dependentes")

        dependentsRef.get().addOnSuccessListener { depSnapshot ->
            val dependentDocs = depSnapshot.documents

            val deleteTasksAndDependents = dependentDocs.map { depDoc ->
                val tasksRef = depDoc.reference.collection("tarefas")
                tasksRef.get().continueWithTask { taskSnapshot ->
                    val deleteTasks = taskSnapshot.result?.documents?.map { it.reference.delete() } ?: emptyList()
                    Tasks.whenAllComplete(deleteTasks).continueWithTask {
                        depDoc.reference.delete()
                    }
                }
            }

        Tasks.whenAllComplete(deleteTasksAndDependents).addOnSuccessListener {
            houseRef.delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Casa ${houseId} removida com sucesso.")
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Erro ao remover casa ${houseId}", it)
                }
            }
        }.addOnFailureListener {
            Log.e("Firestore", "Erro ao acessar dependentes", it)
        }
    }

    fun syncUserToFirestoreRemoveDependent(context: Context, user: User, houseId: String) {
        if (user.id.isBlank()) {
            Log.e("Firestore", "ID do usuário está nulo ou vazio. Abortando sincronização.")
            return
        }

        val houseRef = db.collection("users")
            .document(user.id)
            .collection("houses")
            .document(houseId)

        val dependentsRef = houseRef.collection("dependentes")

        dependentsRef.get().addOnSuccessListener { depSnapshot ->
            val dependentDocs = depSnapshot.documents

            val deleteTasksAndDependents = dependentDocs.map { depDoc ->
                val tasksRef = depDoc.reference.collection("tarefas")
                tasksRef.get().continueWithTask { taskSnapshot ->
                    val deleteTasks = taskSnapshot.result?.documents?.map { it.reference.delete() } ?: emptyList()
                    Tasks.whenAllComplete(deleteTasks).continueWithTask {
                        depDoc.reference.delete()
                    }
                }
            }
        }.addOnFailureListener {
            Log.e("Firestore", "Erro ao acessar dependentes", it)
        }
    }

    fun syncUserToFirestoreRemoveTask(context: Context, user: User, taskId: String) {
        if (user.id.isBlank()) {
            Log.e("Firestore", "ID do usuário está nulo ou vazio. Abortando sincronização.")
            return
        }

        val taskRef = db.collection("tasks").document(taskId)

        taskRef.delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Casa ${taskId} removida com sucesso.")
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Erro ao remover casa ${taskId}", it)
                }
    }
}