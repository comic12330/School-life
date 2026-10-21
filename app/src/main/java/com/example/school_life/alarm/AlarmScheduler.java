package com.example.school_life.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.school_life.net.model.Course;
import com.example.school_life.util.AlarmTimeCalculator;
import com.example.school_life.util.SectionTimes;
import com.example.school_life.util.TermPrefs;

import java.util.List;

/** 依据课程列表设置上课提醒闹钟 */
public class AlarmScheduler {

    /** 每次课程变动后整体重排：先取消全部，再为开启提醒的课程设置下一次 */
    public static void scheduleAll(Context c, List<Course> courses) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        long termStart = TermPrefs.getTermStart(c);
        long now = System.currentTimeMillis();

        for (Course course : courses) {
            if (course.id == null) continue;
            PendingIntent pi = pending(c, course);
            am.cancel(pi);

            if (!Boolean.TRUE.equals(course.remind)) continue;
            long trigger = AlarmTimeCalculator.nextTrigger(now, termStart,
                    course.dayOfWeek, SectionTimes.startMinuteOfDay(course.startSection),
                    course.remindMinutes == null ? 30 : course.remindMinutes,
                    course.startWeek, course.endWeek, course.weekType);
            if (trigger < 0) continue;

            try {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
            } catch (SecurityException e) {
                // 未授予精确闹钟权限时降级为普通闹钟
                am.set(AlarmManager.RTC_WAKEUP, trigger, pi);
            }
        }
    }

    private static PendingIntent pending(Context c, Course course) {
        Intent i = new Intent(c, ClassAlarmReceiver.class);
        i.putExtra("courseId", course.id);
        i.putExtra("name", course.name == null ? "课程" : course.name);
        i.putExtra("room", course.room == null ? "" : course.room);
        i.putExtra("startSection", course.startSection);
        i.putExtra("endSection", course.endSection);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(c, course.id.intValue(), i, flags);
    }
}
