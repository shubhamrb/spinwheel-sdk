# SpinWheel SDK

A lightweight, customizable spin-the-wheel view for Android. Use it to give users
rewards such as points, coupons or cashback.

- Weighted odds per slice
- Land on a result chosen by your server with `spinTo(id)`
- Callbacks for spin start and spin end
- Styling through XML attributes (text, border, spin duration)
- Kotlin, minSdk 21, no third-party dependencies

## Install

Add JitPack to `settings.gradle.kts` (or the root `build.gradle`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Then add the dependency in your app module:

```kotlin
implementation("com.github.shubhamrb:spinwheel-sdk:0.0.2")
```

## Usage

```kotlin
wheel.setSegments(listOf(
    WheelSegment("p10", "10 pts", Color.RED, weight = 40.0),
    WheelSegment("c20", "20% OFF", Color.GREEN, weight = 10.0),
))
wheel.onSpinEnd = { reward -> grant(reward.id) }
wheel.spin()
```

The package name is `com.shubh.spinwheel`:

```kotlin
import com.shubh.spinwheel.SpinWheelView
import com.shubh.spinwheel.WheelSegment
```
