package com.example.musalaweather.api

import com.example.musalaweather.model.Weather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET
    suspend fun getWeather(@Url endPoint: String) : Response<Weather>

    @GET
    suspend fun getWeatherSearch(@Url endPoint: String) : Response<Weather>
}