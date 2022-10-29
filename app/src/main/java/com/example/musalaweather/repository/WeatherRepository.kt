package com.example.musalaweather.repository

import com.example.musalaweather.api.ApiService
import com.example.musalaweather.model.Weather
import retrofit2.Response
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val apiService: ApiService) : WeatherRepositoryInterface {
    override suspend fun getWeather(endPoint: String): Response<Weather> {
        return apiService.getWeather(endPoint)
    }

    override suspend fun getWeatherSearch(endPoint: String): Response<Weather> {
        return apiService.getWeatherSearch(endPoint)
    }

}