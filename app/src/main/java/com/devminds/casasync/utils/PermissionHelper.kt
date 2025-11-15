package com.devminds.casasync.utils

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

object PermissionHelper {

    fun checkAndRequestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkAndRequestPostNotificationPermission(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    // gerencia o resultado da solicitação de permissão
    fun handlePermissionResult(
        context: Context,
        requestCode: Int,
        grantResults: IntArray
    ) {
        if (requestCode == 1001) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            val message = if (granted) {
                // chama outra permissão que só funciona com base na anterior
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkAndRequestExactAlarmPermission(context)
                }
                "Permissão de notificações concedida"
            } else {
                "Permissão de notificações negada"
            }
            DialogUtils.showMessage(context, message)
        }
    }
}
