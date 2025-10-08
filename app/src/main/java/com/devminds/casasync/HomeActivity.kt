package com.devminds.casasync

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.devminds.casasync.R
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomTransition(TransitionType.INFLATE)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}