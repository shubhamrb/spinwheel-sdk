package com.shubh.spinwheel

import androidx.annotation.ColorInt

/**
 * One slice of the wheel.
 *
 * @param id      Your own identifier for the reward (coupon code, points id, ...). Returned in the result.
 * @param label   Text drawn on the slice.
 * @param color   Slice background color.
 * @param weight  Relative probability of winning. Slices are drawn equal size; only odds change.
 *                Use 0 for a slice that can never be won randomly.
 */
data class WheelSegment(
    val id: String,
    val label: String,
    @ColorInt val color: Int,
    val weight: Double = 1.0,
) {
    init { require(weight >= 0.0) { "weight must be >= 0" } }
}
