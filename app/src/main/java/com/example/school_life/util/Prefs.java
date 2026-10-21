package com.example.school_life.util;

import android.content.Context;
import android.content.SharedPreferences;

/** 登录态 + 天气城市的本地存储 */
public class Prefs {

    private static final String SP = "school_life";

    public static String getStudentNo(Context c) {
        return sp(c).getString("studentNo", null);
    }

    public static void setStudentNo(Context c, String no) {
        sp(c).edit().putString("studentNo", no).apply();
    }

    public static void clearLogin(Context c) {
        sp(c).edit().remove("studentNo").apply();
    }

    public static String getCity(Context c) {
        return sp(c).getString("city", "北京");
    }

    public static void setCity(Context c, String city) {
        sp(c).edit().putString("city", city).apply();
    }

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(SP, Context.MODE_PRIVATE);
    }
}
