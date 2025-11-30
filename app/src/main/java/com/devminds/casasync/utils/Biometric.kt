package com.devminds.casasync.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.biometric.BiometricPrompt

class Biometric : BiometricPrompt.AuthenticationCallback(){

    // cria ou acessa o arquivo xml da lista da biometria
    fun sharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)
    }

    // adiciona o usuário a lista da biometria
    fun saveBiometricAuthUser(context: Context, id: String, role: String) {
        // o arquivo.xml com a lista
        val sharedPref = sharedPreferences(context)

        // cria uma cópia da lista original para evitar problemas
        val originalSet = sharedPref.getStringSet("biometricUsers", emptySet()) ?: emptySet()
        val copySet = originalSet.toMutableSet() // a cópia é mutável

        // adiciona o novo usuário à lista
        copySet.add("$id:$role")

        // salva a lista atualizada no arquivo.xml
        sharedPref.edit { putStringSet("biometricUsers", copySet) }
    }

    // retorna a lista no arquivo.xml com os usuários da biometria
    fun getBiometricAuthUsers(context: Context): Set<String> {
        val sharedPref = sharedPreferences(context)
        return sharedPref.getStringSet("biometricUsers", emptySet()) ?: emptySet()
    }

    // grava o ultimo usuário logado na lista
    fun lastLoggedUser(context: Context, id: String, role: String) {
        val sharedPref = sharedPreferences(context)
        sharedPref.edit {
            putString("lastLoggedUser", id)
            putString("lastLoggedUserRole", role)
        }
    }

    // pega o ultimo usuário logado na lista
    fun getLastLoggedUser(context: Context): Pair<String?, String?> {
        val sharedPref = sharedPreferences(context)
        // retorna o lastLoggedUser e lastLoggedUserRole
        val id = sharedPref.getString("lastLoggedUser", null)
        val role = sharedPref.getString("lastLoggedUserRole", null)
        return id to role
    }

    // utilitário: dado um id, retorna o role da lista
    @Suppress("unused") // não é utilizada no momento
    fun getRoleForUser(context: Context, id: String): String? {
        val users = getBiometricAuthUsers(context)
        val match = users.find { it.startsWith("$id:") }
        return match?.split(":")?.getOrNull(1)
    }
}
