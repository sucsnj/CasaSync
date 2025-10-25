package com.devminds.casasync.utils

import android.Manifest
import com.devminds.casasync.R
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

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

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM) // permissão do usuário
    // agenda a notificação
    fun sheduleNotification(context: Context, title: String, message: String, dueTimeMillis: Long) {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("title", title) // dados da intent
            putExtra("message", message)
        }

        // intent para notificação
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            title.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // usa o alarme para agendar e disparar a notificação
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, // RTC_WAKEUP é usado para agendar a notificação
            dueTimeMillis, // tempo em milissegundos
            pendingIntent // intent para notificação
        )
    }

    // cancela a notificação em caso de modificação ou remoção do agendamento anteior
    fun cancelSheduledNotification(context: Context, title: String) {
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            title.hashCode(), // deve ser o mesmo hashcode da intent
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        // manuseia o alarme e cancela a notificação
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
