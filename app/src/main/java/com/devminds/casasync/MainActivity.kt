package com.devminds.casasync

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    // private lateinit var drawerLayout: DrawerLayout
    // private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // pedido de permissão para notificações
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001 // codigo qualquer
            )
        }

        // menu lateral
//        drawerLayout = findViewById(R.id.drawer_layout)
//        navView = findViewById(R.id.nav_view)
//
//        navView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                // R.id.nav_home -> replaceFragment(HomeFragment())
//                // R.id.nav_settings_app -> replaceFragment(SettingsAppFragment())
//                // R.id.nav_settings_user -> replaceFragment(SettingsUserFragment())
//                R.id.nav_about -> replaceFragment(AboutFragment())
//            }
//            drawerLayout.closeDrawer(GravityCompat.START)
//            true
//        }
    }

    private fun replaceFragment(fragment: Fragment, transitionType: TransitionType) {
        supportFragmentManager.beginTransaction()
            .setCustomTransition(transitionType)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}