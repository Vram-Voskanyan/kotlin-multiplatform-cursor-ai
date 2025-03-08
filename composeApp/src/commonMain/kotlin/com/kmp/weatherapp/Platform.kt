package com.kmp.weatherapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform