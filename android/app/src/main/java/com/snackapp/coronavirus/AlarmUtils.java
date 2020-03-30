package com.snackapp.coronavirus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class AlarmUtils {
        public static long TIME_MILI_DEFAULT = 60 * 60 * 1000; // 2 hour
//    public static long TIME_MILI_DEFAULT = 20 * 1000;

    public static void create(Context context) {
        Calendar calendar = Calendar.getInstance();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //Service
        Intent intent = new Intent(context, SchedulingService.class);
        intent.putExtra(SchedulingService.NOTIFICATION_ID, 0);

        // Broadcast receiver
//        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        cancelAlarmIfExists(context, 0, intent);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + TIME_MILI_DEFAULT, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + TIME_MILI_DEFAULT, pendingIntent);
            }
        }
    }

    public static void cancelAlarmIfExists(Context mContext, int requestCode, Intent intent) {
        try {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, 0);
            AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            if (am != null)
                am.cancel(pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
