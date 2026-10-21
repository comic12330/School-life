package com.example.school_life.net.model;

import java.util.List;

/** 和风天气：7 天预报返回 */
public class DailyResponse {
    public String code;
    public List<Daily> daily;

    public static class Daily {
        public String fxDate;
        public String tempMax;
        public String tempMin;
        public String textDay;
        public String textNight;
    }
}
