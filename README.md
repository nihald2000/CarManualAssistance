# Car Manual AI - Android App Setup Guide

## Prerequisites
- Physical Android device (API 32+, Android 12.0+)
- ADB (Android Debug Bridge) installed
-  `.task` model file from HuggingFace

## Step 1: Download  Model
From the HuggingFace repository:
https://huggingface.co/Nihal2000/Car-Manual-gemma-3-270m-it/tree/main

Download the `.task` file (likely named something like `car_manual_model.task`)

## Step 2: Push Model to Device

use adb to push the model to your test device:

```bash
# Create directory on device
adb shell mkdir -p /data/local/tmp/llm/

# Push your .task file to device
adb push car_manual_model.task /data/local/tmp/llm/car_manual_model.task

# Verify the file is there
adb shell ls -lh /data/local/tmp/llm/
```

**Important:** The model is too large to be bundled in an APK, so you need to push it separately during development.

## Step 3: Create Android Project

1. Open Android Studio
2. Create new project: **Empty Views Activity**
3. Name: `CarManualAI`
4. Package: `com.example.carmanualai`
5. Language: **Java**
6. Minimum SDK: **API 32 (Android 12.0)**

## Step 4: Add Project Files

### File Structure
```
app/
├── src/main/
│   ├── java/com/example/carmanualai/
│   │   └── MainActivity.java
│   ├── res/layout/
│   │   └── activity_main.xml
│   └── AndroidManifest.xml
└── build.gradle
```

Copy the provided files to their respective locations.

## Step 5: Update Model Path (if needed)

In `MainActivity.java`, update the model path if you used a different location:

```java
private static final String MODEL_PATH = "/data/local/tmp/llm/car_manual_model.task";
```

## Step 6: Sync and Build

1. Click **File → Sync Project with Gradle Files**
2. Wait for dependencies to download (especially com.google.mediapipe:tasks-genai:0.10.27)
3. Connect your physical Android device
4. Enable USB Debugging on device
5. Click **Run** (Green play button)

## Step 7: Test the App

. Wait for model to load (first time may take 10-30 seconds)

## Configuration Options

You can adjust these parameters in `MainActivity.java`:

```java
LlmInference.LlmInferenceOptions.builder()
    .setModelPath(MODEL_PATH)
    .setMaxTokens(512)      // Maximum response length
    .setTopK(40)            // Diversity of responses
    .setTemperature(0.8f)   // Randomness (0.0-1.0)
    .setRandomSeed(0)       // For reproducibility
    .build();
```

## Troubleshooting

### Model Not Found
- Verify file path: `adb shell ls /data/local/tmp/llm/`
- Check file permissions: `adb shell chmod 644 /data/local/tmp/llm/*.task`

### Out of Memory
- Close other apps
- Reduce `maxTokens` to 256 or 128
- Ensure device has at least 2GB free RAM

### Slow Performance
- Use GPU backend (already configured)
- Close background apps
- Test on higher-end device

### App Crashes
- Check Logcat in Android Studio
- Verify `.task` file is valid MediaPipe format
- Ensure device meets minimum requirements

## For Production Deployment

For deployment, host the model on a server and download it at runtime since the model is too large for APK.

Example download flow:
1. App checks if model exists locally
2. If not, download from server to internal storage
3. Load model from internal storage
4. Cache for future use

## Key Features

✅ Pure Java implementation (no Kotlin)
✅ Simple, clean UI
✅ Background threading (non-blocking)
✅ Progress indicators
✅ Error handling
✅ On-device inference (no internet needed after model download)

## Resources

- [MediaPipe LLM Inference Documentation](https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android)
- [Google AI Edge Gallery](https://github.com/google-ai-edge/gallery)
- [MediaPipe Samples](https://github.com/google-ai-edge/mediapipe-samples)

## Model Information

Your Car Manual model is based on Gemma-3 270M parameters, fine-tuned for automotive questions. It should handle:
- Car maintenance queries
- Parts identification
- Troubleshooting guidance
- General automotive information

---

**Note:** This is a development setup. For production, implement proper model downloading, caching, and user permissions.
