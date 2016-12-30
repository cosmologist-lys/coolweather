package com.cosmos.kpl.coolweather.gson;


import com.google.gson.annotations.SerializedName;

public class Now {

    //now info

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;

    }

}
