package com.devminds.casasync.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object NotificationUtils {

    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun openNotificationSettingsIfDisabled(context: Context) {
        if (!areNotificationsEnabled(context)) {
            val intent = run {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            }
            context.startActivity(intent)
        }
    }

    // cria o canal de notificação (inicializa o sistema de notificação)
    fun createNotificationChannel(context: Context) {
        val channelId = "task_channel"
        val channelName = "Notificação de Tarefas"
        val channelDescription = "Canal para notificações de tarefas"
        val channelImportance = NotificationManager.IMPORTANCE_HIGH // nível de importância da notificação

        // o canal de notificação em si
        val channel = android.app.NotificationChannel(channelId, channelName, channelImportance).apply {
            description = channelDescription
        }

        // obtém o serviço de notificação e cria o canal
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
