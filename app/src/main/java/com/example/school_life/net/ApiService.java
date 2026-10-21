package com.example.school_life.net;

import com.example.school_life.net.model.CardResult;
import com.example.school_life.net.model.Course;
import com.example.school_life.net.model.CampusCard;
import com.example.school_life.net.model.EduResult;
import com.example.school_life.net.model.LoginRequest;
import com.example.school_life.net.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** 校园业务接口（后端 8080） */
public interface ApiService {

    @POST("api/login")
    Call<User> login(@Body LoginRequest body);

    @GET("api/card/{studentNo}")
    Call<CardResult> card(@Path("studentNo") String studentNo);

    @POST("api/card/{studentNo}/lost")
    Call<CampusCard> setLost(@Path("studentNo") String studentNo, @Query("lost") boolean lost);

    @GET("api/edu/{studentNo}")
    Call<EduResult> edu(@Path("studentNo") String studentNo);

    @GET("api/courses/{studentNo}")
    Call<List<Course>> listCourses(@Path("studentNo") String studentNo);

    @POST("api/courses")
    Call<Course> addCourse(@Body Course course);

    @PUT("api/courses")
    Call<Course> updateCourse(@Body Course course);

    @DELETE("api/courses/{id}")
    Call<Void> deleteCourse(@Path("id") long id);
}
