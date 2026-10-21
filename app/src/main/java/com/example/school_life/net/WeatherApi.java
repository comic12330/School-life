package com.example.school_life.net;

import com.example.school_life.net.model.DailyResponse;
import com.example.school_life.net.model.GeoResponse;
import com.example.school_life.net.model.NowResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/** 和风天气接口（App 直连，不经后端） */
public interface WeatherApi {

    @GET
    Call<GeoResponse> geoLookup(@Url String url, @Query("location") String location, @Query("key") String key);

    @GET
    Call<NowResponse> now(@Url String url, @Query("location") String locationId, @Query("key") String key);

    @GET
    Call<DailyResponse> daily(@Url String url, @Query("location") String locationId, @Query("key") String key);
}
