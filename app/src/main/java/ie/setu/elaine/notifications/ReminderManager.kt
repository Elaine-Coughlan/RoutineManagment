package ie.setu.elaine.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import java.util.Calendar

class ReminderManager(private val context: Context) {

    companion object {
        const val DAILY_REMINDER_WORK = "daily_reminder_work"
        const val PREF_NAME = "app_prefs"
        const val LAST_OPENED_KEY = "last_app_open_time"
        const val CONSECUTIVE_DAYS_KEY = "consecutive_days"
        const val LAST_OPENED_DAY_KEY = "last_opened_day"
    }

    fun scheduleDailyReminder() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    fun recordAppOpened() {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()

        val calendar = Calendar.getInstance()
        val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)

        val lastOpenedDay = sharedPrefs.getInt(LAST_OPENED_DAY_KEY, -1)
        val lastOpenedYear = sharedPrefs.getInt("last_opened_year", -1)

        with(sharedPrefs.edit()) {
            putLong(LAST_OPENED_KEY, currentTime)

            if (lastOpenedDay != -1 && lastOpenedYear != -1) {
                val isConsecutiveDay = when {
                    todayYear == lastOpenedYear && todayDayOfYear == lastOpenedDay + 1 -> true

                    todayYear == lastOpenedYear + 1 &&
                            isLastDayOfYear(lastOpenedDay, lastOpenedYear) &&
                            todayDayOfYear == 1 -> true
                    todayYear == lastOpenedYear && todayDayOfYear == lastOpenedDay -> false
                    else -> false
                }

                if (isConsecutiveDay) {
                    val currentStreak = sharedPrefs.getInt(CONSECUTIVE_DAYS_KEY, 0)
                    putInt(CONSECUTIVE_DAYS_KEY, currentStreak + 1)
                } else if (todayYear != lastOpenedYear || todayDayOfYear != lastOpenedDay) {
                    putInt(CONSECUTIVE_DAYS_KEY, 1)
                }
            } else {
                putInt(CONSECUTIVE_DAYS_KEY, 1)
            }

            putInt(LAST_OPENED_DAY_KEY, todayDayOfYear)
            putInt("last_opened_year", todayYear)

            apply()
        }
    }

    fun getConsecutiveDays(): Int {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getInt(CONSECUTIVE_DAYS_KEY, 0)
    }

    private fun isLastDayOfYear(dayOfYear: Int, year: Int): Boolean {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, Calendar.DECEMBER)
        calendar.set(Calendar.DAY_OF_MONTH, 31)

        return dayOfYear == calendar.get(Calendar.DAY_OF_YEAR)
    }
}