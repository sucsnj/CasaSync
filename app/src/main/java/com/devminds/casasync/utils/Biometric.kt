package com.devminds.casasync.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Biometric {

    // cria um pega o arquivo da lista
    fun sharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)
    }

    // cria uma lista de usuários que usaram a autenticação biométrica
    fun saveBiometricAuthUser(context: Context, userId: String) {
        // o arquivo.xml com a lista
        val sharedPref = sharedPreferences(context)

        // cria uma cópia da lista original para evitar problemas
        val originalSet = sharedPref.getStringSet("biometricUsers", emptySet()) ?: emptySet()
        val copySet = originalSet.toMutableSet() // a cópia é mutável

        // adiciona o novo usuário à lista
        copySet.add(userId)

        // salva a lista atualizada no arquivo.xml
        sharedPref.edit { putStringSet("biometricUsers", copySet) }
    }

    // pega a lista no arquivo.xml
    fun getBiometricAuthUsers(context: Context): Set<String> {
        val sharedPref = sharedPreferences(context)
        return sharedPref.getStringSet("biometricUsers", emptySet()) ?: emptySet()
    }

    // grava o ultimo usuário logado na lista
    fun lastLastLoggedUser(context: Context, userId: String) {
        val sharedPref = sharedPreferences(context)
        sharedPref.edit { putString("lastLoggedUser", userId) }
    }

    // pega o ultimo usuário logado na lista
    fun getLastLoggedUser(context: Context): String? {
        val sharedPref = sharedPreferences(context)
        return sharedPref.getString("lastLoggedUser", null)
    }
}