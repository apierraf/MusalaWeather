package com.example.musalaweather.repository

import com.example.musalaweather.model.Weather
import retrofit2.Response

interface WeatherRepositoryInterface {
    suspend fun getWeather(endPoint : String) : Response<Weather>
    suspend fun getWeatherSearch(endPoint : String) : Response<Weather>
}