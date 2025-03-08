package com.kmp.weatherapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmp.weatherapp.model.CurrentWeatherResponse
import com.kmp.weatherapp.repository.WeatherRepository
import com.kmp.weatherapp.viewmodel.WeatherViewModel
import kotlinx.datetime.*
import kotlinx.coroutines.delay

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Initial data loading with retry mechanism
    LaunchedEffect(Unit) {
        // Try loading data immediately 
        viewModel.loadWeatherData()
        
        // If data isn't loaded after a delay, try again (helps on iOS)
        delay(2000) // 2 second delay
        if (uiState.weatherData == null && uiState.error == null) {
            println("Initial data load didn't succeed, retrying...")
            viewModel.loadWeatherData()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather App") },
                actions = {
                    IconButton(onClick = { viewModel.loadWeatherData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    ErrorView(
                        errorMessage = uiState.error!!,
                        onRetry = { viewModel.loadWeatherData() }
                    )
                }
                uiState.weatherData != null -> {
                    WeatherContent(
                        weatherData = uiState.weatherData!!,
                        onLocationSelected = viewModel::setSelectedLocation
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherContent(
    weatherData: CurrentWeatherResponse,
    onLocationSelected: (Pair<Double, Double>) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Location selection
        LocationSelector(onLocationSelected)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Current weather
        CurrentWeatherCard(weatherData)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Additional Weather Details
        Text(
            text = "Weather Details",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AdditionalDetailsCard(weatherData)
    }
}

@Composable
fun CurrentWeatherCard(weatherData: CurrentWeatherResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location name
            Text(
                text = weatherData.name,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Current temperature
            Text(
                text = "${weatherData.main.temp.toInt()}°",
                style = MaterialTheme.typography.h3,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Weather condition
            Text(
                text = weatherData.weather.firstOrNull()?.main ?: "Unknown",
                style = MaterialTheme.typography.h6
            )
            
            Text(
                text = weatherData.weather.firstOrNull()?.description?.capitalize() ?: "",
                style = MaterialTheme.typography.body1
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Additional weather details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetail(label = "Feels Like", value = "${weatherData.main.feelsLike.toInt()}°")
                WeatherDetail(label = "Humidity", value = "${weatherData.main.humidity}%")
                WeatherDetail(label = "Pressure", value = "${weatherData.main.pressure} hPa")
            }
        }
    }
}

@Composable
fun AdditionalDetailsCard(weatherData: CurrentWeatherResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            DetailRow("Wind", "${weatherData.wind.speed} m/s, ${getWindDirection(weatherData.wind.deg)}")
            DetailRow("Visibility", "${weatherData.visibility / 1000} km")
            DetailRow("Cloud Cover", "${weatherData.clouds.all}%")
            
            // Sunrise and sunset times
            val sunrise = formatTime(weatherData.sys.sunrise, weatherData.timezone)
            val sunset = formatTime(weatherData.sys.sunset, weatherData.timezone)
            DetailRow("Sunrise", sunrise)
            DetailRow("Sunset", sunset)
            
            // Min and max temperatures
            DetailRow("Min Temp", "${weatherData.main.tempMin.toInt()}°")
            DetailRow("Max Temp", "${weatherData.main.tempMax.toInt()}°")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body1,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WeatherDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.caption)
        Text(text = value, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LocationSelector(onLocationSelected: (Pair<Double, Double>) -> Unit) {
    val locations = listOf(
        "London" to WeatherRepository.LONDON,
        "New York" to WeatherRepository.NEW_YORK,
        "Tokyo" to WeatherRepository.TOKYO,
        "Sydney" to WeatherRepository.SYDNEY
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        locations.forEach { (name, coordinates) ->
            Button(
                onClick = { onLocationSelected(coordinates) },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(name, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ErrorView(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.h5,
            color = Color.Red,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Show the error message in a card for better visibility
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            elevation = 4.dp,
            backgroundColor = Color(0xFFFFF0F0)
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.body1,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
        ) {
            Text("Retry", color = Color.White)
        }
    }
}

private fun formatTime(timestamp: Long, timezoneOffset: Int): String {
    // Convert timestamp to Instant
    val instant = Instant.fromEpochSeconds(timestamp)
    
    // Apply timezone offset - this is a simplification
    val offsetInstant = instant.plus(timezoneOffset, DateTimeUnit.SECOND)
    
    // Convert to local date time in UTC (we've already applied the offset)
    val dateTime = offsetInstant.toLocalDateTime(TimeZone.UTC)
    
    // Format as HH:MM
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}

private fun getWindDirection(degrees: Int): String {
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return directions[(degrees % 360 / 45) % 8]
}

private fun String.capitalize(): String {
    return if (this.isEmpty()) this else this.replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }
} 