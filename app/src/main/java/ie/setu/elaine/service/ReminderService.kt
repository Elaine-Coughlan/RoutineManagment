package ie.setu.elaine.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import ie.setu.elaine.R
import ie.setu.elaine.model.Reminder
import ie.setu.elaine.model.NotificationType
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

class ReminderService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val REMINDER_CHANNEL_ID = "routine_reminders"
        private const val URGENT_CHANNEL_ID = "urgent_reminders"
        private const val SILENT_CHANNEL_ID = "silent_reminders"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Standard Reminders Channel
            val standardChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Routine Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            // Urgent Reminders Channel
            val urgentChannel = NotificationChannel(
                URGENT_CHANNEL_ID,
                "Urgent Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                enableLights(true)
            }

            // Silent Reminders Channel
            val silentChannel = NotificationChannel(
                SILENT_CHANNEL_ID,
                "Silent Reminders",
                NotificationManager.IMPORTANCE_LOW
            )

            notificationManager.createNotificationChannels(
                listOf(standardChannel, urgentChannel, silentChannel)
            )
        }
    }

    fun scheduleReminder(reminder: Reminder) {
        // Use AlarmManager to schedule periodic reminders
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        reminder.repeatDays.forEach { day ->
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("REMINDER_ID", reminder.id)
                putExtra("REMINDER_TITLE", reminder.title)
                putExtra("REMINDER_DESCRIPTION", reminder.description)
                putExtra("NOTIFICATION_TYPE", reminder.notificationType.ordinal)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Calculate next trigger time
            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, day.value)
                set(Calendar.HOUR_OF_DAY, reminder.time.hour)
                set(Calendar.MINUTE, reminder.time.minute)
                set(Calendar.SECOND, 0)
            }

            // Set repeating alarm
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        }
    }

    fun cancelReminder(reminderId: String) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun showNotification(reminder: Reminder) {
        val channelId = when (reminder.notificationType) {
            NotificationType.URGENT -> URGENT_CHANNEL_ID
            NotificationType.SILENT -> SILENT_CHANNEL_ID
            else -> REMINDER_CHANNEL_ID
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(reminder.title)
            .setContentText(reminder.description)
            .setSmallIcon(R.drawable.ic_notification)//TODO create icon
            .setPriority(when (reminder.notificationType) {
                NotificationType.URGENT -> NotificationCompat.PRIORITY_HIGH
                NotificationType.SILENT -> NotificationCompat.PRIORITY_LOW
                else -> NotificationCompat.PRIORITY_DEFAULT
            })
            .setAutoCancel(true)

        notificationManager.notify(reminder.id.hashCode(), builder.build())
    }
}