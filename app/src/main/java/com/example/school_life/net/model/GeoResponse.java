package com.example.school_life.net.model;

import java.util.List;

/** 和风天气：城市搜索返回 */
public class GeoResponse {
    public String code;
    public List<GeoLocation> location;

    public static class GeoLocation {
        public String id;
        public String name;
        public String adm1; // 省
        public String adm2; // 市
    }
}
