package com.kmp.weatherapp

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.kmp.weatherapp.ui.WeatherScreen
import com.kmp.weatherapp.viewmodel.WeatherViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Create and use a ViewModel instance
        val weatherViewModel = WeatherViewModel()
        WeatherScreen(viewModel = weatherViewModel)
    }
}