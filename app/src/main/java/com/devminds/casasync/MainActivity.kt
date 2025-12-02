package com.devminds.casasync

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.devminds.casasync.utils.PermissionHelper
import com.devminds.casasync.utils.Utils
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(newBase)
        val lang = prefs.getString("app_language", "")

        val locale = if (lang.isNullOrEmpty()) Locale.getDefault() else Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Utils.isLogged(this)) {
            finish()
            return
        }

        // força o tema escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // pedido de permissão para notificações
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionHelper.checkAndRequestPostNotificationPermission(this)
        }
    }

    // verifica se a permissão de notificação foi dada
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.handlePermissionResult(this, requestCode, grantResults)
    }
}
