package com.example.school_life.util;

/** 计算下一次上课提醒的触发时刻（纯逻辑，方便单元测试） */
public class AlarmTimeCalculator {

    /**
     * @param now                 当前时间戳 ms
     * @param termStart           学期起始周周一 00:00
     * @param dayOfWeek           1=周一 ... 7=周日
     * @param sectionMinuteOfDay  该节开始时间在一天中的分钟数
     * @param remindMinutes       提前分钟数
     * @param startWeek/endWeek   课程起止教学周
     * @param weekType            0=每周 1=单周 2=双周
     * @return 下一次触发时刻 ms；找不到返回 -1
     */
    public static long nextTrigger(long now, long termStart, int dayOfWeek, int sectionMinuteOfDay,
                                   int remindMinutes, int startWeek, int endWeek, int weekType) {
        int currentWeek = (int) ((WeekUtils.mondayStart(now) - termStart) / (7 * WeekUtils.DAY)) + 1;
        if (currentWeek < 1) currentWeek = 1;
        for (int w = currentWeek; w <= endWeek; w++) {
            if (w < startWeek) continue;
            if (weekType == 1 && w % 2 == 0) continue;
            if (weekType == 2 && w % 2 == 1) continue;
            long candidate = termStart + (w - 1) * 7 * WeekUtils.DAY
                    + (dayOfWeek - 1) * WeekUtils.DAY
                    + (sectionMinuteOfDay - remindMinutes) * 60_000L;
            if (candidate > now) return candidate;
        }
        return -1;
    }

    private AlarmTimeCalculator() {
    }
}
