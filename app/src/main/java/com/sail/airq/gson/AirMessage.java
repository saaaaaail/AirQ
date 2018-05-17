package com.sail.airq.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by sail on 2018/5/4.
 */

public class AirMessage {

    public String status;

    public Update update;

    public Basic basic;

    @SerializedName("air_now_city")
    public AirNowCity airnow;

    @SerializedName("air_forecast")
    public List<AirForecast> forecastList;

    @SerializedName("air_hourly")
    public List<AirHourlyForecast> hourlyForecastList;
}
