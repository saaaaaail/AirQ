package com.sail.airq;

/**
 * Created by sail on 2018/5/3.
 */

public class AppGlobal {
    public static final String CURRENT_INDEX = "currentIndex";

    public static double LONGITUDE = 0;//经度
    public static double LATITUDE = 0;//纬度
    public static String ADDRESS = "";//地址
    public static String CITY="";//城市
    public static String CHOOSECITY;//按照和风天气的格式存储，可直接查询
    public static String LOCALPARCITY;
    public static String LOCALCITY; //按照和风天气的格式存储，可直接查询

    public static String parentCity;
    public static String cityId;
    public static String city;
    public static String updateTime;
    public static String aqi;
    public static String aqiInfo ;
    public static String mainPoll ;
    public static String pubtime ;//数据发布时间
    public static String pm10;
    public static String pm25;
    public static String no2 ;
    public static String so2;
    public static String co ;
    public static String o3 ;

    public static String tmp;
    public static String atmp;
    public static String cond;
    public static String winddir;
    public static String windsc;
    public static String windspd;
    public static String hum;
    public static String pcpn;
    public static String pres;
    public static String vis;
    public static String cloud;
}
