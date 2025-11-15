package com.devminds.casasync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import com.devminds.casasync.parts.User
import com.google.firebase.firestore.SetOptions
import java.util.UUID

object FirestoreHelper {

    fun getDb(): FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getUserById(userId: String, onResult: (User?) -> Unit) {
        val userDoc = getDb().collection("users").document(userId)
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
        getDb().collection("users")
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
        getDb().collection("users")
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
        val userDoc = getDb().collection("users").document(userId)
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
                        val dependentsRef = houseDoc.collection("dependents")

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
                                    val tasksRef = depDoc.collection("tasks")

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
                                                "houseId" to task.houseId,
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

    fun syncUserToFirestoreRemoveHouse(user: User, houseId: String) {
        if (user.id.isBlank()) {
            Log.e("Firestore", "ID do usuário está nulo ou vazio. Abortando sincronização.")
            return
        }

        val houseRef = getDb().collection("users")
            .document(user.id)
            .collection("houses")
            .document(houseId)

        val dependentsRef = houseRef.collection("dependents")

        dependentsRef.get().addOnSuccessListener { depSnapshot ->
            val dependentDocs = depSnapshot.documents

            val deleteTasksAndDependents = dependentDocs.map { depDoc ->
                val tasksRef = depDoc.reference.collection("tasks")
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
                    Log.d("Firestore", "Casa $houseId removida com sucesso.")
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Erro ao remover casa $houseId", it)
                }
            }
        }.addOnFailureListener {
            Log.e("Firestore", "Erro ao acessar dependentes", it)
        }
    }

    fun syncUserToFirestoreRemoveDependent(user: User, houseId: String, dependentId: String) {
        if (user.id.isBlank()) {
            Log.e("Firestore", "ID do usuário está nulo ou vazio. Abortando sincronização.")
            return
        }

        val houseRef = getDb().collection("users")
            .document(user.id)
            .collection("houses")
            .document(houseId)

        val dependentRef = houseRef.collection("dependents").document(dependentId)

        dependentRef.collection("tasks").get().addOnSuccessListener { taskSnapshot ->
            val deleteTasks = taskSnapshot.documents.map { it.reference.delete() }

            Tasks.whenAllComplete(deleteTasks).addOnSuccessListener {
                dependentRef.delete()
                    .addOnSuccessListener {
                        Log.d("Firestore", "Dependente $dependentId e suas tarefas removidos com sucesso.")
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Erro ao remover dependente $dependentId", it)
                    }
            }
        }.addOnFailureListener {
            Log.e("Firestore", "Erro ao acessar dependente", it)
        }
    }

    fun syncUserToFirestoreRemoveTask(user: User, houseId: String, depId: String, taskId: String) {
        if (user.id.isBlank()) {
            Log.e("Firestore", "ID do usuário está nulo ou vazio. Abortando sincronização.")
            return
        }

        val taskRef = getDb().collection("users")
            .document(user.id)
            .collection("houses")
            .document(houseId)
            .collection("dependents")
            .document(depId)
            .collection("tasks")
            .document(taskId)

        taskRef.delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Tarefa $taskId removida com sucesso.")
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Erro ao remover tarefa $taskId", it)
                }
    }
}