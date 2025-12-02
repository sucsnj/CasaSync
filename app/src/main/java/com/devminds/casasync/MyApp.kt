package com.devminds.casasync

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.devminds.casasync.utils.NotificationUtils
import com.google.firebase.FirebaseApp
import androidx.preference.PreferenceManager

// classe de configuração inicial do app
// essa classe carrega antes da aplicação em si, a main activity
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // define o tema do app com base na preferência salva
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // inicializa o Firebase
        FirebaseApp.initializeApp(this)

        // Cria o canal de notificação ao iniciar o app
        NotificationUtils.createNotificationChannel(this)
    }
}
