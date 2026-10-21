package com.example.school_life.util;

import java.util.Calendar;

/** 周次与单双周判断（纯逻辑，方便单元测试） */
public class WeekUtils {

    public static final long DAY = 24L * 3600 * 1000;

    /** 某一时刻所在周的周一 00:00 */
    public static long mondayStart(long nowMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(nowMillis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int dow = c.get(Calendar.DAY_OF_WEEK); // 1=周日 ... 7=周六
        int diff = (dow == Calendar.SUNDAY) ? 6 : dow - Calendar.MONDAY;
        c.add(Calendar.DAY_OF_YEAR, -diff);
        return c.getTimeInMillis();
    }

    /** 当前是第几教学周（早于学期开始按第 1 周） */
    public static int currentWeek(long termStart, long now) {
        long diff = mondayStart(now) - termStart;
        int week = (int) (diff / (7 * DAY)) + 1;
        return Math.max(1, week);
    }

    /** 课程在某周是否显示：周次范围内 且 满足每周/单周/双周 */
    public static boolean visible(com.example.school_life.net.model.Course c, int week) {
        if (week < c.startWeek || week > c.endWeek) return false;
        if (c.weekType == 1) return week % 2 == 1;
        if (c.weekType == 2) return week % 2 == 0;
        return true;
    }

    private WeekUtils() {
    }
}
