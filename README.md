# SpinWheel SDK

A lightweight, customizable spin-the-wheel view for Android. Use it to give users
rewards such as points, coupons or cashback.

- Weighted odds per slice
- Land on a result chosen by your server with `spinTo(id)`
- Callbacks for spin start and spin end
- Styling through XML attributes (text, border, spin duration)
- Kotlin, minSdk 21, no third-party dependencies

## Install

implementation("com.shubh.spinwheel:spinwheel:*.*.*")

## Usage

wheel.setSegments(listOf(
    WheelSegment("p10", "10 pts", Color.RED, weight = 40.0),
    WheelSegment("c20", "20% OFF", Color.GREEN, weight = 10.0),
))
wheel.onSpinEnd = { reward -> grant(reward.id) }
wheel.spin()
