# opper-android-sdk

The goal of this project is to demonstrate how to communicate with Opper devices produced by Yuanxin Tech.

## Prerequisites

The following components must be installed in order to go through the Usage Instructions.

* [Gradle Build Tool](https://gradle.org/).
* Latest LTS release of the [Adoptium OpenJDK](https://adoptium.net/).
* [Git client](https://git-scm.com/downloads).
* [Android Studio](https://developer.android.com/studio/) version 3.1 or later.

## Getting started

1. Download this repository extract and copy the oppersdk aar library under /app/libs to your own
   android project.
2. Add required repositories to settings.gradle, looks like bellow:
   ```
   pluginManagement {
      repositories {
          google()
          mavenCentral()
          gradlePluginPortal()
          maven("https://www.jitpack.io")    // add this line
      }
    }
   dependencyResolutionManagement {
      repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
      repositories {
      google()
      mavenCentral()
      maven("https://jitpack.io")      // add this line
   }
   ```
3. Implements the dependencies below:
    ```
    implementation(files("libs/oppersdk-XXX.aar"))    // the oppersdk
    implementation("com.github.tbruyelle:rxpermissions:0.10.2")
    implementation("com.neovisionaries:nv-bluetooth:1.8")
    implementation("io.reactivex.rxjava2:rxjava:2.2.9")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    ```
4. Ready to roll

## Usage

1. Create an application class and do some initializing works, make sure to initialize ``OpperHelper`` under that application context, e.g.
   ``` 
   OpperHelper.init([application context], new OpperHelper.Weigh() {
            @Override
            public int unit() {
                return Settings.unit;
            }

            @Override
            public int decimalPlaces() {
                return Settings.decimals;
            }

            @Override
            public double tare() {
                return Settings.tare;
            }

            @Override
            public void onTareChange(double v) {
                Settings.tare = v;
            }

            @Override
            public int vibrateGrams() {
                return Settings.vibrateGrams;
            }
        });
   ```
2. Implements ``OpperHelper.OnBleListener, OpperHelper.OnFirmwareListener`` on your own Activity class then setup OpperHelper:
   ``` 
   OpperHelper.setOnBleListener(this);    // for the weighing logic
   OpperHelper.setOnFirmwareListener(this);   // for firmware update
   ```
3. Connect Opper device through BLE (Bluetooth Low Energy) technology. Something like bellow:
   ``` 
   OpperHelper.connect([replace with your own bluetooth MAC address])
   ```
4. For more usage, please refer to the demo.
