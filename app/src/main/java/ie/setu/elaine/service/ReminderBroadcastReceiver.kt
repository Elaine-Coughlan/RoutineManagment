package ie.setu.elaine.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ie.setu.elaine.model.Reminder
import ie.setu.elaine.model.NotificationType

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("REMINDER_ID") ?: return
        val title = intent.getStringExtra("REMINDER_TITLE") ?: return
        val description = intent.getStringExtra("REMINDER_DESCRIPTION") ?: ""
        val notificationTypeOrdinal = intent.getIntExtra("NOTIFICATION_TYPE", 0)

        val reminder = Reminder(
            id = reminderId,
            title = title,
            description = description,
            time = java.time.LocalTime.now(),
            notificationType = NotificationType.values()[notificationTypeOrdinal]
        )

        val reminderService = ReminderService(context)
        reminderService.showNotification(reminder)
    }
}