package com.kmp.weatherapp.repository

import com.kmp.weatherapp.model.CurrentWeatherResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class WeatherRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    // Simple hardcoded API key approach
    private val apiKey = "YOUR_KEY_HERE" // This is a sample API key - replace with your own for production use
    // Using current weather endpoint instead of onecall for simpler integration
    private val baseUrl = "https://api.openweathermap.org/data/2.5/weather"

    suspend fun getWeatherData(lat: Double, lon: Double, units: String = "metric"): Result<CurrentWeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$baseUrl?lat=$lat&lon=$lon&units=$units&appid=$apiKey"
                println("Making API request to: $url")
                
                // Try up to 3 times with exponential backoff for better iOS reliability
                var lastException: Exception? = null
                for (attempt in 1..3) {
                    try {
                        val response: CurrentWeatherResponse = client.get(url).body()
                        return@withContext Result.success(response)
                    } catch (e: Exception) {
                        println("API Error on attempt $attempt: ${e.message}")
                        lastException = e
                        if (attempt < 3) {
                            val delayTime = 1000L * attempt // 1s, 2s, 3s
                            delay(delayTime)
                        }
                    }
                }
                
                // If we get here, all attempts failed
                lastException?.printStackTrace()
                Result.failure(lastException ?: Exception("Unknown error occurred"))
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
    
    companion object {
        // Common locations for quick testing
        val LONDON = Pair(51.5074, -0.1278)
        val NEW_YORK = Pair(40.7128, -74.0060)
        val TOKYO = Pair(35.6762, 139.6503)
        val SYDNEY = Pair(-33.8688, 151.2093)
    }
} 