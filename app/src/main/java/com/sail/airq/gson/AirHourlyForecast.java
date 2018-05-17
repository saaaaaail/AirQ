package com.sail.airq.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sail on 2018/5/4.
 */

public class AirHourlyForecast {

    @SerializedName("time")
    public String htime;

    public String aqi;

    public String main;

    public String qlty;

    public String pm10;

    public String pm25;

    public String no2;

    public String so2;

    public String co;

    public String o3;
}
