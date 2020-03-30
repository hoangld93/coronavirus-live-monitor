package com.snackapp.coronavirus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.snackapp.coronavirus.model.CoronaAttribute;
import com.snackapp.coronavirus.model.Feature;
import com.snackapp.coronavirus.model.Result;
import com.snackapp.coronavirus.network.ServerTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static com.snackapp.coronavirus.SchedulingService.TEMP_COUNT;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    private static final int TIME_VIBRATE = 500;
    private Context context;
    private int count = 0;
    private int index = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        getData();
        AlarmUtils.create(context);
    }

    private void getData() {
        ServerTask.getInstance().getServices().getCoronaAttributesCity()
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
//                    newResult = result;
                    if (!result.getFeatures().isEmpty()) {
                        for (int i = 0; i < result.getFeatures().size(); i++) {
                            if (i < 10)
                                result.getFeatures().get(i).getAttributes().setConfirmed(result.getFeatures().get(i).getAttributes().getConfirmed() + count++);
                        }
                        pushNotification(result.getFeatures(), getDataFromCache());
                    }
                }, err -> {
                    err.printStackTrace();
                });
    }

    private List<Feature> getDataFromCache() {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PRE_CORONA, MODE_PRIVATE);
        String strData = prefs.getString(MainActivity.PRE_CORONA_DATA, "");
        Gson gson = new Gson();
        Result result = gson.fromJson(strData, Result.class);

        if (result != null)
            return result.getFeatures();

        return new ArrayList<>();
    }


    private void pushNotification(List<Feature> newData, List<Feature> oldData) {
        HashMap<String, CoronaAttribute> notifications = getDataNotification(newData, oldData);

        if (notifications.isEmpty()) return;

//        String message = intent.getStringExtra(NOTIFY_CONTENT);

        for (Map.Entry notification : notifications.entrySet()) {
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.putExtra(MainActivity.NOTIFICATION_CORONA, (CoronaAttribute) notification.getValue());
            notificationIntent
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            int requestID = (int) System.currentTimeMillis();
            PendingIntent contentIntent = PendingIntent
                    .getActivity(context, requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_app)
                            .setContentTitle(context.getString(R.string.app_name))
                            .setContentText(notification.getKey().toString())
                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setAutoCancel(true)
                            .setSound(alarmSound)
                            .setPriority(6)
                            .setVibrate(new long[]{TIME_VIBRATE, TIME_VIBRATE + 100, TIME_VIBRATE + 200, TIME_VIBRATE + 300,
                                    TIME_VIBRATE + 400})
                            .setContentIntent(contentIntent);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null)
                notificationManager.notify(index++, builder.build());
        }

        saveData(new Result(newData));
    }

    private void saveData(Result result) {
        Gson gson = new Gson();
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PRE_CORONA, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MainActivity.PRE_CORONA_DATA, gson.toJson(result));
        editor.putInt(TEMP_COUNT, count);
        editor.apply();
    }

    private HashMap<String, CoronaAttribute> getDataNotification(List<Feature> newData, List<Feature> oldData) {
        HashMap<String, CoronaAttribute> notifications = new HashMap<>();

        if (oldData.isEmpty() || newData.isEmpty())
            return notifications;

        for (int i = 0; i < oldData.size(); i++) {
            CoronaAttribute coronaAttributeOld = oldData.get(i).getAttributes();
            for (int j = 0; j < newData.size(); j++) {
                CoronaAttribute coronaAttributeNew = newData.get(i).getAttributes();
                if (coronaAttributeNew.getOBJECTID() == coronaAttributeOld.getOBJECTID()) {
                    int confirmedPlus = coronaAttributeNew.getConfirmed() - coronaAttributeOld.getConfirmed();
                    int recoveredPlus = coronaAttributeNew.getRecovered() - coronaAttributeOld.getRecovered();
                    int deathPlus = coronaAttributeNew.getRips() - coronaAttributeOld.getRips();

                    String locationName;
                    if (!TextUtils.isEmpty(coronaAttributeNew.getProvince_State())) {
                        locationName = coronaAttributeNew.getProvince_State();
                    } else {
                        locationName = coronaAttributeOld.getCountry_Region();
                    }

                    if (confirmedPlus > 0) {
                        notifications.put(context.getString(R.string.notification_confirmed, confirmedPlus, locationName), coronaAttributeNew);
                    }

                    if (recoveredPlus > 0) {
                        notifications.put(context.getString(R.string.notification_confirmed, recoveredPlus, locationName), coronaAttributeNew);
                    }

                    if (deathPlus > 0) {
                        notifications.put(context.getString(R.string.notification_rip, deathPlus, locationName), coronaAttributeNew);
                    }
                }
            }
        }

        return notifications;
    }

}