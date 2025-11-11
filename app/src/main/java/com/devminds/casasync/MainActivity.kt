package com.devminds.casasync

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.core.net.toUri
import com.devminds.casasync.fragments.LoginFragment
import com.devminds.casasync.utils.DialogUtils
import com.devminds.casasync.utils.PermissionHelper
import com.devminds.casasync.utils.PermissionHelper.checkAndRequestExactAlarmPermission
import com.devminds.casasync.utils.Utils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("logged_user_id", null)

        if (!userId.isNullOrEmpty()) {
            // Usuário já está logado, vai direto para HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
            finish() // encerra MainActivity para não voltar com o botão de voltar
        } else {
            // Usuário não logado, segue com LoginFragment
            setContentView(R.layout.activity_main)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }

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
        PermissionHelper.handlePermissionResult(this, requestCode, permissions, grantResults)
    }

    private fun replaceFragment(fragment: Fragment, transitionType: TransitionType) {
        supportFragmentManager.beginTransaction()
            .setCustomTransition(transitionType)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
