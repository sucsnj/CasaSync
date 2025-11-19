package com.devminds.casasync

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        @Suppress("DEPRECATION") // @(TODO migrar para Compose)
        window.statusBarColor = ContextCompat.getColor(this, R.color.notch)
//        window.apply {
//            statusBarColor = Color.TRANSPARENT
//            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//        }

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
