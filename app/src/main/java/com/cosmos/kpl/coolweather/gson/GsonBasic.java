package com.cosmos.kpl.coolweather.gson;


import com.google.gson.annotations.SerializedName;

public class GsonBasic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;

    }
}
