package com.cosmos.kpl.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    //farseer daysaway info

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {

        public String max;

        public String min;

    }

    public class More {

        @SerializedName("txt_d")
        public String info;

    }
}
