package com.snackapp.coronavirus.network;


import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snackapp.coronavirus.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerTask {

    private static ServerTask serverTask = null;

    private Retrofit retrofit;
    private OkHttpClient httpClient;
    private ServerApi servicesCommon;
    private Gson gson;

    private ServerTask() {
        httpClient = provideOkHttpClient();
        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_DOMAIN)
                .addConverterFactory(GsonConverterFactory.create(provideGson()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(httpClient)
                .build();
    }

    private Gson provideGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .serializeNulls()
                    .create();
        }
        return gson;
    }

    private OkHttpClient provideOkHttpClient() {
        final OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

        builder.connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor).addNetworkInterceptor(new StethoInterceptor());
        }

//        HeaderInterceptor headerInterceptor = new HeaderInterceptor();
//        builder.addInterceptor(headerInterceptor);

        return builder.build();
    }

    public static ServerTask getInstance() {

        if (serverTask == null) {
            serverTask = new ServerTask();
        }

        return serverTask;
    }

    public static void clearOrResetServerTask() {
        serverTask = null;
    }

    public <S> S createService(Class<S> serviceClass) {

        return retrofit.create(serviceClass);
    }

    public <S> S createService(Class<S> serviceClass, String baseUrl) {

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
//                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(provideGson()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(httpClient)
                .build().create(serviceClass);
    }

    public ServerApi getServices() {
        if (servicesCommon == null) {
            servicesCommon = createService(ServerApi.class);
        }

        return servicesCommon;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

//    public static class HeaderInterceptor implements Interceptor {
//        @Override
//        public Response intercept(Chain chain) throws IOException {
//            Context context = MainApplication.getAppContext();
//            String uuid = DeviceUtils.getDeviceUUID(context);
//
//            Request original = chain.request();
//            Request request = original.newBuilder()
//                    .header(DEVICE_ID, uuid)
//                    .header(APP_TOKEN, "iiwGIFisnHLKZbQEUtAgx08CHaaYfIaDmgycZGWiw0bAz5L6VoAb9jG3YFN67uM6")
//                    .header(AUTHORIZE, "Bearer YMANn7yx9G5ChxF0qOkOwSTsBi81NKlSbweBnbbROqs3jSJo9T4mXaugWTvI8q3K")
//                    .addHeader("pageNumber", "1")
//                    .addHeader("pageSize", "100")
//                    .build();
//            return chain.proceed(request);
//        }
//
//    }
}