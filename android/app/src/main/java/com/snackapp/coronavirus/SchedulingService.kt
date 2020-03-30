package com.snackapp.coronavirus

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.snackapp.coronavirus.model.CoronaAttribute
import com.snackapp.coronavirus.model.Feature
import com.snackapp.coronavirus.model.Result
import com.snackapp.coronavirus.network.ServerTask
import rx.schedulers.Schedulers
import java.util.*

class SchedulingService : IntentService(SchedulingService::class.java.simpleName) {
    private var index = 0
    private val newResult: Result? = null
    private var count = 0

// dummy data notification start
//                        for (int i = 0; i < result.getFeatures().size(); i++) {
//                            if (i < 10)
//                                result.getFeatures().get(i).getAttributes().setConfirmed(result.getFeatures().get(i).getAttributes().getConfirmed() + count++);
//                        }
// dummy data notification end
//                    newResult = result;
//    public static void enqueueWork(Context context, Intent work) {
//        enqueueWork(context, SchedulingService.class, JOB_ID, work);
//    }
//
//    @Override
//    protected void onHandleWork(@NonNull Intent intent) {
//        index = intent.getIntExtra(NOTIFICATION_ID, 0);
//
//        SharedPreferences prefs = getSharedPreferences(MainActivity.PRE_CORONA, MODE_PRIVATE);
//        count = prefs.getInt(TEMP_COUNT, count);
//
//        getData();
//        AlarmUtils.create(getApplicationContext());
//    }

    private fun getData() {
        ServerTask.getInstance().services.coronaAttributesCity
                .subscribeOn(Schedulers.io())
                .subscribe({ result: Result ->
                    //                    newResult = result;
                    if (result.features.isNotEmpty()) { // dummy data notification start
//                        for (int i = 0; i < result.getFeatures().size(); i++) {
//                            if (i < 10)
//                                result.getFeatures().get(i).getAttributes().setConfirmed(result.getFeatures().get(i).getAttributes().getConfirmed() + count++);
//                        }
// dummy data notification end
                        pushNotification(result.features, getDataFromCache())
                    }
                }) { err: Throwable -> err.printStackTrace() }
    }

    private fun pushNotification(newData: List<Feature>, oldData: List<Feature>) {
        val notifications = getDataNotification(newData, oldData)
        if (notifications.isEmpty()) return
        //        String message = intent.getStringExtra(NOTIFY_CONTENT);
        for ((key, value) in notifications) {
            val notificationIntent = Intent(this, MainActivity::class.java)
            notificationIntent.putExtra(MainActivity.NOTIFICATION_CORONA, value)
            notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val requestID = System.currentTimeMillis().toInt()
            val contentIntent = PendingIntent
                    .getActivity(this, requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val builder = NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_app)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(key)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setSound(alarmSound)
                    .setPriority(6)
                    .setVibrate(longArrayOf(TIME_VIBRATE.toLong(), TIME_VIBRATE + 100.toLong(), TIME_VIBRATE + 200.toLong(), TIME_VIBRATE + 300.toLong(),
                            TIME_VIBRATE + 400.toLong()))
                    .setContentIntent(contentIntent)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance)
                builder.setChannelId(NOTIFICATION_CHANNEL_ID)
                notificationManager.createNotificationChannel(notificationChannel)
            }
            notificationManager.notify(index++, builder.build())
        }
        saveData(Result(newData))
    }

    private fun getDataFromCache(): List<Feature> {
        val prefs = getSharedPreferences(MainActivity.PRE_CORONA, Context.MODE_PRIVATE)
        val strData = prefs.getString(MainActivity.PRE_CORONA_DATA, "")
        val gson = Gson()
        val result = gson.fromJson(strData, Result::class.java)
        return result?.features ?: ArrayList()
    }

    private fun getDataNotification(newData: List<Feature>, oldData: List<Feature>): HashMap<String, CoronaAttribute> {
        val notifications = HashMap<String, CoronaAttribute>()
        if (oldData.isEmpty() || newData.isEmpty()) return notifications
        for (i in oldData.indices) {
            val (OBJECTID, _, Country_Region, _, _, Confirmed, rips, Recovered) = oldData[i].attributes
            for (j in newData.indices) {
                val coronaAttributeNew = newData[i].attributes
                if (coronaAttributeNew.OBJECTID == OBJECTID) {
                    val confirmedPlus = coronaAttributeNew.Confirmed - Confirmed
                    val recoveredPlus = coronaAttributeNew.Recovered - Recovered
                    val deathPlus = coronaAttributeNew.rips - rips
                    var locationName: String?
                    locationName = if (!TextUtils.isEmpty(coronaAttributeNew.Province_State)) {
                        coronaAttributeNew.Province_State
                    } else {
                        Country_Region
                    }
                    if (confirmedPlus > 0) {
                        notifications[getString(R.string.notification_confirmed, confirmedPlus, locationName)] = coronaAttributeNew
                    }
                    if (recoveredPlus > 0) {
                        notifications[getString(R.string.notification_confirmed, recoveredPlus, locationName)] = coronaAttributeNew
                    }
                    if (deathPlus > 0) {
                        notifications[getString(R.string.notification_rip, deathPlus, locationName)] = coronaAttributeNew
                    }
                }
            }
        }
        return notifications
    }

    private fun saveData(result: Result) {
        val gson = Gson()
        val prefs = getSharedPreferences(MainActivity.PRE_CORONA, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(MainActivity.PRE_CORONA_DATA, gson.toJson(result))
        editor.putInt(TEMP_COUNT, count)
        editor.apply()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        index = intent.getIntExtra(NOTIFICATION_ID, 0)
        val prefs = getSharedPreferences(MainActivity.PRE_CORONA, Context.MODE_PRIVATE)
        count = prefs.getInt(TEMP_COUNT, count)
        getData()
        AlarmUtils.create(applicationContext)
    }

    companion object {
        private const val TIME_VIBRATE = 500
        const val TEMP_COUNT = "temp_count"
        var NOTIFY_CONTENT = "notify_content"
        @JvmField
        var NOTIFICATION_ID = "notify_id"
        const val NOTIFICATION_CHANNEL_ID = "10001"
        const val JOB_ID = 1000
    }
}