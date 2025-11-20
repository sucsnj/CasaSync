package com.devminds.casasync

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.devminds.casasync.fragments.DependentFragment
import com.devminds.casasync.fragments.HomeFragment

class HomeActivity : AppCompatActivity() {

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
            val userId = intent.getStringExtra("userId")
            val dependentId = intent.getStringExtra("dependentId")

            val fragment: Fragment = when {
                userId != null -> HomeFragment()
                dependentId != null -> DependentFragment()
                else -> HomeFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }

    }

    @Suppress("unused") // TODO
    private fun replaceFragment(fragment: Fragment, transitionType: TransitionType) {
        supportFragmentManager.beginTransaction()
            .setCustomTransition(transitionType)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
