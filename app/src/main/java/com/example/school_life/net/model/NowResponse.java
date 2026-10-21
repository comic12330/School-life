package com.example.school_life.net.model;

/** 和风天气：实时天气返回 */
public class NowResponse {
    public String code;
    public Now now;

    public static class Now {
        public String temp;
        public String text;
        public String windDir;
        public String windScale;
        public String humidity;
        public String feelsLike;
    }
}
