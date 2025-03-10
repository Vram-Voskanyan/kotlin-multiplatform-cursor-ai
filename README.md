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

### Using android_runner.sh

For efficient testing and debugging on Android devices, the project includes an `android_runner.sh` shell script that:

1. **Dynamically extracts app information** from your project files:
   - Package name from `build.gradle.kts`
   - Main activity name from `AndroidManifest.xml`

2. **Builds the APK** automatically using Gradle

3. **Handles device connection**:
   - Uses connected device if available
   - Launches an emulator if no device is connected
   - Waits for the emulator to boot completely

4. **Installs and launches** the app on the device

To use the script:
```bash
# Make it executable (first time only)
chmod +x android_runner.sh

# Run it
./android_runner.sh
```

The script outputs detailed information about each step, making it easy to verify the app configuration:
```
Extracting package name from composeApp/build.gradle.kts...
Using package: com.kmp.weatherapp
Extracting launcher activity from composeApp/src/androidMain/AndroidManifest.xml...
Using activity: com.kmp.weatherapp.MainActivity
...
```

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