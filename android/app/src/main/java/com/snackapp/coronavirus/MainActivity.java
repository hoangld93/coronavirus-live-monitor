package com.snackapp.coronavirus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import com.google.gson.Gson;
import com.snackapp.coronavirus.model.Feature;
import com.snackapp.coronavirus.model.Result;
import com.snackapp.coronavirus.network.ServerTask;
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.schedulers.Schedulers;


public class MainActivity extends ReactActivity {

    public static String PRE_CORONA = "pre_corona";
    public static String PRE_CORONA_DATA = "pre_corona_data";
    public static String NOTIFICATION_CORONA = "notification_corona";

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "CoronaReact";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlarmUtils.create(getApplicationContext());
        getData();
        getDataFromCache();

//        String config = ((MainApplication) getApplication()).getRemoteConfig().getString("data");
//        Toast.makeText(this, config, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this, getMainComponentName()) {
            @Override
            protected ReactRootView createRootView() {
                RNGestureHandlerEnabledRootView mRootView = new RNGestureHandlerEnabledRootView(MainActivity.this);
                return mRootView;
            }

            @Nullable
            @Override
            protected Bundle getLaunchOptions() {
                Bundle initialProperties = new Bundle();
                String dataListRemote = MainApplication.getRemoteConfig().getString("data");
                initialProperties.putString("dataListRemote", dataListRemote);

                Boolean isUseMyServer = MainApplication.getRemoteConfig().getBoolean("is_use_my_server");
                initialProperties.putBoolean("isUseMyServer", isUseMyServer);

                return initialProperties;
            }
        };
    }

    private void getData() {
        ServerTask.getInstance().getServices().getCoronaAttributesCity()
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                    if (!result.getFeatures().isEmpty()) {
                        saveData(result);
                    }
                }, err -> {
                    err.printStackTrace();
                });
    }

    private void saveData(Result result) {
        Gson gson = new Gson();
        SharedPreferences prefs = getSharedPreferences(MainActivity.PRE_CORONA, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MainActivity.PRE_CORONA_DATA, gson.toJson(result));
        editor.apply();
    }

    private List<Feature> getDataFromCache() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PRE_CORONA, MODE_PRIVATE);
        String strData = prefs.getString(MainActivity.PRE_CORONA_DATA, "");
        Gson gson = new Gson();
        Result result = gson.fromJson(strData, Result.class);

        if (result != null)
            return result.getFeatures();

        return new ArrayList<>();
    }

}
