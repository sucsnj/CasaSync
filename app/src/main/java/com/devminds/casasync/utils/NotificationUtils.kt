package com.devminds.casasync.utils

import android.app.NotificationManager
import android.content.Context
import android.app.NotificationChannel

object NotificationUtils {

    // ID para os canais de notificação
    const val TASK_CHANNEL_ID = "task_channel"
    const val ALERT_CHANNEL_ID = "alert_channel"
    const val MESSAGE_CHANNEL_ID = "message_channel"

    // cria o canal de notificação (inicializa o sistema de notificação)
    fun createNotificationChannel(context: Context) {

        // cada canal
        val channels = listOf(
            NotificationChannel(
                TASK_CHANNEL_ID,
                "Notificações de Tarefas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Canal para notificações relacionadas a tarefas" },
            NotificationChannel(
                ALERT_CHANNEL_ID,
                "Alertas Gerais",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Canal para alertas e avisos gerais" },
            NotificationChannel(
                MESSAGE_CHANNEL_ID,
                "Mensagens",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Canal para notificações de mensagens internas" })

//        val channelId = "task_channel"
//        val channelName = "Notificação de Tarefas"
//        val channelDescription = "Canal para notificações de tarefas"
//        val channelImportance =
//            NotificationManager.IMPORTANCE_HIGH // nível de importância da notificação

        // o canal de notificação em si
//        val channel =
//            android.app.NotificationChannel(channelId, channelName, channelImportance).apply {
//                description = channelDescription
//            }

        // obtém o serviço de notificação e cria o canal
        val manager = context.getSystemService(NotificationManager::class.java)
        channels.forEach { manager.createNotificationChannel(it) }
    }
}
