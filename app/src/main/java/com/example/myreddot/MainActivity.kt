package com.example.myreddot

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var ripple: ShapeRipple? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ripple = findViewById(R.id.ripple)
//        findViewById<DotWaveV2>(R.id.content).startRippleAnimation()
    }
}