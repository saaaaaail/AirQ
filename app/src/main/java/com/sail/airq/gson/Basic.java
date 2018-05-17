package com.sail.airq.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sail on 2018/5/4.
 */

public class Basic {

    @SerializedName("parent_city")
    public String parentcity;

    @SerializedName("location")
    public String city;

    @SerializedName("cid")
    public String cityId;


}
