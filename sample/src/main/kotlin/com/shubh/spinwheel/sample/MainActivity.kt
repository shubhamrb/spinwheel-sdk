package com.shubh.spinwheel.sample

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.shubh.spinwheel.SpinWheelView
import com.shubh.spinwheel.WheelSegment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wheel = SpinWheelView(this).apply {
            setSegments(
                listOf(
                    WheelSegment("p10", "10 pts", "#E53935".toColorInt(), weight = 40.0),
                    WheelSegment("p50", "50 pts", "#8E24AA".toColorInt(), weight = 20.0),
                    WheelSegment("none", "Try again", "#1E88E5".toColorInt(), weight = 25.0),
                    WheelSegment("c20", "20% OFF", "#43A047".toColorInt(), weight = 10.0),
                    WheelSegment("p100", "100 pts", "#FB8C00".toColorInt(), weight = 4.0),
                    WheelSegment("jackpot", "Jackpot", "#6D4C41".toColorInt(), weight = 1.0),
                )
            )
            onSpinEnd = { Toast.makeText(context, "You won: ${it.label}", Toast.LENGTH_LONG).show() }
        }
        val button = Button(this).apply {
            text = "SPIN"
            setOnClickListener { if (!wheel.isSpinning) wheel.spin() }
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(wheel, FrameLayout.LayoutParams(-1, -2))
            addView(button)
        })
    }
}
