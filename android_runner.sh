#!/bin/bash

PROJECT_DIR=$(pwd)
APK_PATH="$PROJECT_DIR/composeApp/build/outputs/apk/debug/composeApp-debug.apk"
GRADLE_FILE="composeApp/build.gradle.kts"
MANIFEST_FILE="composeApp/src/androidMain/AndroidManifest.xml"
EMULATOR_PATH="$HOME/Library/Android/sdk/emulator"

# Dynamically extract package name from build.gradle.kts
echo "Extracting package name from $GRADLE_FILE..."
PACKAGE_NAME=$(grep -E "namespace\s*=" "$GRADLE_FILE" | sed -E 's/.*"([^"]+)".*/\1/')
echo "Using package: $PACKAGE_NAME"

echo "Extracting launcher activity from $MANIFEST_FILE..."

# Check if manifest file exists
if [ ! -f "$MANIFEST_FILE" ]; then
    echo "Manifest file not found at: $MANIFEST_FILE"
    echo "Using MainActivity as default"
    ACTIVITY_NAME="MainActivity"
else
    echo "Analyzing manifest structure..."
    
    # Using a temporary file to store numbered lines
    TEMP_FILE=$(mktemp)
    cat -n "$MANIFEST_FILE" > "$TEMP_FILE"
    
    # Find the line number of LAUNCHER category
    LAUNCHER_LINE=$(grep -n "android.intent.category.LAUNCHER" "$TEMP_FILE" | cut -d':' -f1)
    
    if [ -n "$LAUNCHER_LINE" ]; then
        echo "Found LAUNCHER category at line $LAUNCHER_LINE"
        
        # Find closest previous activity declaration by searching backward
        ACTIVITY_LINE=$(head -n "$LAUNCHER_LINE" "$TEMP_FILE" | grep -n '<activity' | tail -1 | cut -d':' -f1)
        
        if [ -n "$ACTIVITY_LINE" ]; then
            echo "Found activity declaration at line $ACTIVITY_LINE"
            
            # Extract the android:name attribute from this activity
            ACTIVITY_BLOCK=$(sed -n "${ACTIVITY_LINE},+10p" "$TEMP_FILE")
            ACTIVITY_NAME=$(echo "$ACTIVITY_BLOCK" | grep 'android:name=' | sed -E 's/.*android:name="([^"]+)".*/\1/')
            
            echo "Extracted from manifest: $ACTIVITY_NAME"
        fi
    fi
    
    # Clean up temp file
    rm "$TEMP_FILE"
    
    # Final check with fallback
    if [ -z "$ACTIVITY_NAME" ]; then
        echo "Failed to extract activity name from manifest, using MainActivity as fallback"
        ACTIVITY_NAME="MainActivity"
    fi
fi

echo "Raw activity name: $ACTIVITY_NAME"

# Handle relative vs absolute activity name
if [[ "$ACTIVITY_NAME" == "."* ]]; then
    MAIN_ACTIVITY="${PACKAGE_NAME}${ACTIVITY_NAME}"
elif [[ "$ACTIVITY_NAME" != *"."* ]]; then
    MAIN_ACTIVITY="${PACKAGE_NAME}.${ACTIVITY_NAME}"
else
    MAIN_ACTIVITY="$ACTIVITY_NAME"
fi

echo "Using activity: $MAIN_ACTIVITY"

# Step 1: Build the APK
echo "Building APK..."
./gradlew composeApp:assembleDebug

# Check if build was successful
if [ ! -f "$APK_PATH" ]; then
    echo "Build failed or APK not found!"
    exit 1
fi
echo "APK built successfully: $APK_PATH"

# Step 2: Check for connected devices
DEVICE=$(adb devices | grep -v "List" | grep "device" | head -1 | awk '{print $1}')
if [ -z "$DEVICE" ]; then
    echo "No active devices found. Checking for available emulators..."

    # Check if emulator path exists
    if [ ! -d "$EMULATOR_PATH" ]; then
        echo "Emulator path doesn't exist: $EMULATOR_PATH"
        echo "Please update the EMULATOR_PATH variable in the script."
        exit 1
    fi

    # List available emulators
    AVD_LIST=$("$EMULATOR_PATH/emulator" -list-avds)
    
    if [ -z "$AVD_LIST" ]; then
        echo "No available emulators found. Please create an AVD in Android Studio."
        exit 1
    fi

    # Start the first emulator found
    AVD_NAME=$(echo "$AVD_LIST" | head -n 1)
    echo "Starting emulator: $AVD_NAME..."
    "$EMULATOR_PATH/emulator" -avd "$AVD_NAME" -no-snapshot-load &
    EMULATOR_PID=$!
    
    echo "Emulator starting with PID: $EMULATOR_PID"

    # Improved wait-for-device with timeout and boot completion check
    echo "Waiting for emulator to boot (this may take several minutes)..."
    
    # First wait for device to appear
    adb wait-for-device
    
    # Set a timeout (120 seconds = 2 minutes)
    TIMEOUT=120
    INTERVAL=5
    ELAPSED=0
    
    # Then poll until boot is complete or timeout
    while [ $ELAPSED -lt $TIMEOUT ]; do
        echo "Checking if boot completed... ($ELAPSED seconds elapsed)"
        
        # Check if the device is fully booted
        BOOT_COMPLETE=$(adb shell getprop sys.boot_completed 2>/dev/null)
        
        if [ "$BOOT_COMPLETE" == "1" ]; then
            echo "Emulator boot completed!"
            break
        fi
        
        sleep $INTERVAL
        ELAPSED=$((ELAPSED + INTERVAL))
    done
    
    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo "Timeout waiting for emulator to boot completely."
        echo "Trying to proceed anyway..."
    fi
    
    # Get the device ID after boot
    sleep 5  # Give a bit more time for device to settle
    DEVICE=$(adb devices | grep -v "List" | grep "device" | head -1 | awk '{print $1}')
fi

# Final check to ensure a device is available
if [ -z "$DEVICE" ]; then
    echo "No device detected after trying to start an emulator. Exiting."
    exit 1
fi
echo "Device detected: $DEVICE"

 # Dynamically find the QEMU process name, needed for macOS to bring the emulator window to front
    if [[ "$(uname)" == "Darwin" ]]; then  # macOS
        echo "Detecting emulator process on macOS..."
        # Find any qemu-system process related to the emulator
        QEMU_PROCESS=$(ps -ef | grep -E "qemu-system-[a-z0-9]+" | grep -v grep | head -1 | awk '{print $8}')
        
        if [ -n "$QEMU_PROCESS" ]; then
            # Extract just the process name from the path
            QEMU_PROCESS_NAME=$(basename "$QEMU_PROCESS")
            echo "Found emulator process: $QEMU_PROCESS_NAME"
            
            # Bring the emulator window to front
            osascript -e "tell application \"System Events\" to tell process \"$QEMU_PROCESS_NAME\" to set frontmost to true"
        else
            echo "Could not detect emulator process, skipping window focus"
        fi
    elif [[ "$(uname)" == "Linux" ]]; then  # Linux
        echo "Running on Linux, skipping window management"
    elif [[ "$(uname)" == "MINGW"* || "$(uname)" == "MSYS"* ]]; then  # Windows
        echo "Running on Windows, skipping window management"
    fi


# Step 3: Install the APK
echo "Installing APK on device..."
adb -s $DEVICE install -r "$APK_PATH"

# Step 4: Launch the App
echo "Launching the app..."
adb -s $DEVICE shell am start -n "$PACKAGE_NAME/$MAIN_ACTIVITY"

echo "Done!"