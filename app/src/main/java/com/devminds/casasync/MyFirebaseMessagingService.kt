package com.devminds.casasync

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devminds.casasync.fragments.TaskFragment
import com.devminds.casasync.utils.NotificationUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.Manifest
import android.content.pm.PackageManager

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // para notificações com payload simples
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }

        // para notificações com payload de data
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Payload de dados: ${remoteMessage.data}")
            val title = remoteMessage.data["title"] ?: "Nova atualização"
            val body = remoteMessage.data["body"] ?: "Você recebeu uma nova tarefa"
            showNotification(title, body)
        }
    }

    // token para envio de notificações
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Novo token FCM: $token")
    }

    private fun showNotification(title: String?, message: String?) {
        // abrir a main activity ao clicar na notificação
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NotificationUtils.TASK_CHANNEL_ID)
            .setContentTitle(title ?: "CasaSync")
            .setContentText(message ?: "Você recebeu uma nova tarefa")
            .setSmallIcon(R.drawable.casasync)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        showNotificationSafe(System.currentTimeMillis().toInt(), notification)
    }

    private fun showNotificationSafe(notificationId: Int, notification: android.app.Notification) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        } else {
            Log.w("FCM", "Permissão POST_NOTIFICATIONS não concedida")
        }
    }
}
