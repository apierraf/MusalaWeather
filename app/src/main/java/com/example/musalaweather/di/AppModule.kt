package com.example.musalaweather.di

import com.example.musalaweather.api.ApiService
import com.example.musalaweather.repository.WeatherRepository
import com.example.musalaweather.repository.WeatherRepositoryInterface
import com.example.musalaweather.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRetrofit() : ApiService {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun injectRealRepo(api: ApiService) = WeatherRepository(api) as WeatherRepositoryInterface
}