package com.sail.airq.gson;

import com.google.gson.annotations.SerializedName;

import java.security.PublicKey;

/**
 * Created by sail on 2018/5/14.
 */

public class JvHeData {

    public  Citynow now;
    public JvHeAir lastTwoWeeks;
    public  LastMoniData lastMoniData;

    public class Citynow{
        public String city;
        public String AQI;
        public String quality;
        public String date;
    }

    public class LastMoniData{
        public String city;
        public String AQI;
        public String quality;
        public String date;

        @SerializedName("PM2.5Hour")
        public String PM25Hour;

        @SerializedName("PM2.5Day")
        public String PM25Day;

        public String PM10Hour;

        public String lat;

        public String lon;
    }
}
