package com.sail.airq.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sail on 2018/5/4.
 */

public class WeatherNow {

    @SerializedName("fl")//体感温度
    public String apparentTemperature;

    @SerializedName("tmp")//外界温度
    public String temperature;

    @SerializedName("cond_txt")//天气状况
    public String cond;

    @SerializedName("wind_dir")//风向
    public String windDir;

    @SerializedName("wind_sc")//风力
    public String windpower;

    @SerializedName("wind_spd")//风速
    public String windspeed;

    @SerializedName("hum")//湿度
    public String humidity;

    @SerializedName("pcpn")//降雨量
    public String aop;

    @SerializedName("pres")//大气压强
    public String pressure;

    @SerializedName("vis")//能见度
    public String visibility;

    public String cloud;//云量
}
