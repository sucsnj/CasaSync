package com.devminds.casasync.utils

import android.Manifest
import com.devminds.casasync.R
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri

class TaskAlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS) // permissão do usuário
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") // dados da intent
        val message = intent.getStringExtra("message") // dados da intent

        // notificação
        val notification = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(R.drawable.casasync) // ícone da notificação TODO: mudar
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // desaparece ao clicar
            .build()

        // gerenciador de notificações
        val manager = NotificationManagerCompat.from(context)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // agenda a notificação
    fun scheduleNotification(context: Context, title: String, message: String, dueTimeMillis: Long) {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("title", title) // dados da intent
            putExtra("message", message)
        }

        val requestCode = (title + message + dueTimeMillis).hashCode()

        // intent para notificação
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode, // id para identificar a notificação
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // usa o alarme para agendar e disparar a notificação
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // verifica a versão do Android para determinar como agendar a notificação
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // android 12 ou superior
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dueTimeMillis,
                    pendingIntent
                )
            } else { // se não estiver permitido, pede permissão
                // redirecionar o usuário para conceder permissão
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } else {
            // android 11 ou inferior: sem necessidade de permissão
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                dueTimeMillis,
                pendingIntent
            )
        }
    }

    // cancela a notificação em caso de modificação ou remoção do agendamento anteior
    fun cancelScheduleNotification(context: Context, title: String) {
        val intent = Intent(context, TaskAlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            title.hashCode(), // deve ser o mesmo hashcode da intent
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // manuseia o alarme e cancela a notificação
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
