package ie.setu.elaine

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import ie.setu.elaine.notifications.DailyReminderWorker
import ie.setu.elaine.notifications.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit



@RunWith(AndroidJUnit4::class)
class DailyReminderWorkerTest{

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    @Mock
    private lateinit var notificationHelper: NotificationHelper
    @Before
    fun setup(){
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()

        sharedPreferences = context.getSharedPreferences(
            DailyReminderWorker.PREF_NAME,
            Context.MODE_PRIVATE
        )
        sharedPreferencesEditor = sharedPreferences.edit()
    }

    //Checking for daily reminder
    @Test
    fun `test notification shown when app not opened`(){
        val timeAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(25)
        sharedPreferencesEditor.putLong(DailyReminderWorker.LAST_OPENED_KEY, timeAgo).commit()

        val worker = TestWorker(context, notificationHelper)
        val result = worker.doTestWork()

        verify(notificationHelper).showDailyReminder()
        assertEquals(ListenableWorker.Result.success(), result)
    }


}

private class TestWorker(
   private val context: Context,
   private val helper: NotificationHelper
) {
    fun doTestWork() : ListenableWorker.Result {
        val sharedPrefs = context.getSharedPreferences(DailyReminderWorker.PREF_NAME, Context.MODE_PRIVATE)
        val lastOpenedTime = sharedPrefs.getLong(DailyReminderWorker.LAST_OPENED_KEY, 0)
        val currentTime = System.currentTimeMillis()

        if (lastOpenedTime == 0L || currentTime - lastOpenedTime > TimeUnit.DAYS.toMillis(1)) {
            helper.showDailyReminder()
        }

        return ListenableWorker.Result.success()
    }



}

