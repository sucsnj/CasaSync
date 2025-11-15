package com.devminds.casasync.utils

import android.app.NotificationManager
import android.content.Context

object NotificationUtils {

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
