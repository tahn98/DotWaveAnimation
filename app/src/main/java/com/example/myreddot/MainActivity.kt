package com.example.myreddot

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dotWave = findViewById<View>(R.id.content) as DotWave
        dotWave.startRippleAnimation()
    }
}