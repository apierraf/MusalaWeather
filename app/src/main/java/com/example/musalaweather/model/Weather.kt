package com.example.musalaweather.model

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("name")
    var location: String? = null,
    var weather: List<WeatherX>? = null,
    @SerializedName("main")
    var dataX: DataX? = null,
    @SerializedName("wind")
    var wind: Wind? = null,
    @SerializedName("sys")
    var country: Country? = null,
)
