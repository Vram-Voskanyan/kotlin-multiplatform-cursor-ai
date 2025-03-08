package com.kmp.weatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmp.weatherapp.model.CurrentWeatherResponse
import com.kmp.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()
    
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    // Initialize ViewModel with data loading
    init {
        loadWeatherData()
    }
    
    fun loadWeatherData(lat: Double = WeatherRepository.LONDON.first, lon: Double = WeatherRepository.LONDON.second) {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            repository.getWeatherData(lat, lon)
                .onSuccess { weatherData ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            weatherData = weatherData,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error occurred"
                        )
                    }
                }
        }
    }
    
    fun setSelectedLocation(location: Pair<Double, Double>) {
        loadWeatherData(location.first, location.second)
    }
}

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: CurrentWeatherResponse? = null,
    val error: String? = null
) 