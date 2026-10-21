package com.example.school_life.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** 到点发上课提醒通知 */
public class ClassAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long courseId = intent.getLongExtra("courseId", -1);
        String name = intent.getStringExtra("name");
        String room = intent.getStringExtra("room");
        int start = intent.getIntExtra("startSection", 0);
        int end = intent.getIntExtra("endSection", 0);

        String text = name + " 第" + start + "-" + end + "节";
        if (room != null && !room.isEmpty()) text += " @" + room;
        Notifications.show(context, (int) courseId, "上课提醒", text);
    }
}
