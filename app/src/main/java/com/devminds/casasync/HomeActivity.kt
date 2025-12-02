package com.devminds.casasync

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.devminds.casasync.fragments.DepFragment
import com.devminds.casasync.fragments.HomeFragment
import java.util.Locale

class HomeActivity : AppCompatActivity() {

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
        enableEdgeToEdge()

//        window.statusBarColor = getColor(R.color.white)

        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            val role = intent.getStringExtra("role")

            val fragment: Fragment = when (role) {
                "admin" -> HomeFragment()
                "dependent" -> DepFragment()
                else -> HomeFragment()
            }

            // define o tema do app com base na preferência salva
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val isDarkMode = prefs.getBoolean("dark_mode", false)

            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }

    }
}
