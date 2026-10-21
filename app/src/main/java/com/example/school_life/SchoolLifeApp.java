package com.example.school_life;

import android.app.Application;

import com.example.school_life.alarm.Notifications;

public class SchoolLifeApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Notifications.createChannel(this);
    }
}
