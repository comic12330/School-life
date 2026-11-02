package com.example.school_life.util;

import java.util.Locale;

/** 12 节的上课时间 */
public class SectionTimes {

    /** 下标 1~12 对应第 1~12 节开始时间 */
    public static final String[] LABELS = {
            "", "08:00", "08:55", "10:00", "10:55", "14:00", "14:55",
            "16:00", "16:55", "19:00", "19:55", "20:50", "21:45"
    };

    /** 某节开始时间换算成当天的分钟数，如 08:00 -> 480 */
    public static int startMinuteOfDay(int section) {
        String[] hm = LABELS[section].split(":");
        return Integer.parseInt(hm[0]) * 60 + Integer.parseInt(hm[1]);
    }

    /** 某节下课时间：开始时间顺延 45 分钟，如第 1 节 08:45 */
    public static String endLabel(int section) {
        int end = startMinuteOfDay(section) + 45;
        return String.format(Locale.CHINA, "%02d:%02d", end / 60, end % 60);
    }

    private SectionTimes() {
    }
}
