package com.example.casasync

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

// classe de configuração inicial do app
// essa classe carrega antes da aplicação em si, a main activity
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // força o modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}