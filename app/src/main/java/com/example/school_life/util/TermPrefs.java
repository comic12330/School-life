package com.example.school_life.util;

import android.content.Context;
import android.content.SharedPreferences;

/** 学期起始周（默认取首次运行时的本周一 00:00） */
public class TermPrefs {

    private static final String SP = "term";

    public static long getTermStart(Context c) {
        SharedPreferences sp = c.getSharedPreferences(SP, Context.MODE_PRIVATE);
        long v = sp.getLong("termStart", 0);
        if (v == 0) {
            v = WeekUtils.mondayStart(System.currentTimeMillis());
            sp.edit().putLong("termStart", v).apply();
        }
        return v;
    }
}
