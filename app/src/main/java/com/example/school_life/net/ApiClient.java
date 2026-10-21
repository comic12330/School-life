package com.example.school_life.net;

import com.example.school_life.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Retrofit 客户端：校园接口 + 天气接口 */
public class ApiClient {

    private static Retrofit main;
    private static Retrofit weather;

    public static ApiService api() {
        if (main == null) {
            main = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return main.create(ApiService.class);
    }

    public static WeatherApi weather() {
        if (weather == null) {
            weather = new Retrofit.Builder()
                    .baseUrl("http://localhost/") // 实际请求地址由 @Url 提供
                    .client(client())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return weather.create(WeatherApi.class);
    }

    private static OkHttpClient client() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(log)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }
}
