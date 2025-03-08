package com.kmp.weatherapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeatherResponse(
    val coord: Coordinates,
    val weather: List<Weather>,
    val base: String,
    val main: MainWeatherData,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Long,
    val sys: SystemData,
    val timezone: Int,
    val id: Long,
    val name: String,
    val cod: Int
)

@Serializable
data class Coordinates(
    val lon: Double,
    val lat: Double
)

@Serializable
data class MainWeatherData(
    val temp: Double,
    @SerialName("feels_like")
    val feelsLike: Double,
    @SerialName("temp_min")
    val tempMin: Double,
    @SerialName("temp_max")
    val tempMax: Double,
    val pressure: Int,
    val humidity: Int
)

@Serializable
data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double? = null
)

@Serializable
data class Clouds(
    val all: Int
)

@Serializable
data class SystemData(
    val type: Int? = null,
    val id: Int? = null,
    val country: String? = null,
    val sunrise: Long,
    val sunset: Long
)

@Serializable
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
) 