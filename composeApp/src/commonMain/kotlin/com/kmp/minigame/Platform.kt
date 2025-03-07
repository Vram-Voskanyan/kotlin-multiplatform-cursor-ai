package com.kmp.minigame

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform