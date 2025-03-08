# KMM Weather App

A Kotlin Multiplatform Mobile (KMM) app that fetches weather data from OpenWeatherMap's API and displays it using Compose Multiplatform.

## Features

- 🌤️ Real-time weather data using OpenWeatherMap API
- 📱 Cross-platform UI with Compose Multiplatform (Android & iOS)
- 🌎 Weather for multiple locations around the world
- 🔄 Easy location switching
- 📊 Detailed weather information including temperature, wind, humidity, and more

## Setup

### Prerequisites

- Android Studio Arctic Fox or higher
- Xcode 13 or higher (for iOS development)
- OpenWeatherMap API key (get one at [https://openweathermap.org/api](https://openweathermap.org/api))

### API Key

The app includes a sample API key for testing. If you want to use your own:

1. Open `composeApp/src/commonMain/kotlin/com/kmp/weatherapp/repository/WeatherRepository.kt`
2. Replace the existing API key with your own:

```kotlin
private val apiKey = "YOUR_API_KEY" // Replace with your actual API key
```

**Note:** This app uses the OpenWeatherMap Current Weather API 2.5 endpoint, which is accessible with a standard free API key.

## Running the App

### Android

1. Open the project in Android Studio
2. Select the Android app configuration
3. Click Run

### iOS

1. Open the project in Android Studio
2. Generate the Xcode project: `./gradlew generateIosXcodeProject`
3. Open `iosApp/iosApp.xcodeproj` in Xcode
4. Run the app

### Desktop

1. Run `./gradlew composeApp:run` to start the desktop version

## Architecture

The app follows an MVVM architecture:

- **Model**: Data classes for weather information
- **View**: Compose UI components
- **ViewModel**: Manages UI state and data loading

## Technologies Used

- Kotlin Multiplatform Mobile
- Compose Multiplatform
- Ktor for networking
- Kotlinx.Serialization for JSON parsing
- Kotlinx.Coroutines for asynchronous programming

## License

This project is licensed under the MIT License - see the LICENSE file for details.

This is a Kotlin Multiplatform project targeting Android, iOS, Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that's common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple's CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you're sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…