package com.example.school_life;

/** 常量集中写死，作业演示更直观 */
public final class Constants {

    /** 模拟器访问电脑本机后端用 10.0.2.2；真机改成电脑局域网 IP */
    public static final String BASE_URL = "http://10.0.2.2:8080/";

    /** 和风天气：在 https://dev.qweather.com 申请免费 Key 后填到这里 */
    public static final String QWEATHER_KEY = "cd6f4407b4ab4929aad74fce7bf86ec2";

    public static final String WEATHER_GEO_URL = "https://mv4wcwrd62.re.qweatherapi.com/geo/v2/city/lookup";
    public static final String WEATHER_NOW_URL = "https://mv4wcwrd62.re.qweatherapi.com/v7/weather/now";
    public static final String WEATHER_7D_URL = "https://mv4wcwrd62.re.qweatherapi.com/v7/weather/7d";

    private Constants() {
    }
}
