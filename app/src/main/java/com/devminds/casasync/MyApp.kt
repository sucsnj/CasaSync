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

        // força o tema escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // inicializa o Firebase
        FirebaseApp.initializeApp(this)

        // Cria o canal de notificação ao iniciar o app
        NotificationUtils.createNotificationChannel(this)
    }
}
