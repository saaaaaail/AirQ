package com.sail.airq.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by sail on 2018/5/4.
 */

public class Weather {

    public Basic basic;

    public String status;

    public Update update;

    public WeatherNow now;

    @SerializedName("lifestyle")
    public List<LifeStyle> lifeStyleList;
}
