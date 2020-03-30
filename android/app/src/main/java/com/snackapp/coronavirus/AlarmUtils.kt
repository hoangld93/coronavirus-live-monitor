package com.snackapp.coronavirus

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.snackapp.coronavirus.SchedulingService
import java.util.*

object AlarmUtils {
    var TIME_MILI_DEFAULT = 60 * 60 * 1000 // 2 hour
            .toLong()

    //    public static long TIME_MILI_DEFAULT = 20 * 1000;
    @JvmStatic
    fun create(context: Context) {
        val calendar = Calendar.getInstance()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        //Service
        val intent = Intent(context, SchedulingService::class.java)
        intent.putExtra(SchedulingService.NOTIFICATION_ID, 0)
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        cancelAlarmIfExists(context, 0, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + TIME_MILI_DEFAULT, pendingIntent)
        } else {
            alarmManager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis + TIME_MILI_DEFAULT] = pendingIntent
        }
    }

    private fun cancelAlarmIfExists(mContext: Context, requestCode: Int, intent: Intent?) {
        try {
            val pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, 0)
            val am = mContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            am?.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}