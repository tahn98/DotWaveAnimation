package com.example.myreddot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.myreddot.shapeRipple.ShapeRipple


class MainActivity : AppCompatActivity() {
    private lateinit var ripple: ShapeRipple
    private lateinit var btTest: AppCompatButton
    private var toogle: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ripple = findViewById(R.id.ripple)
        btTest = findViewById(R.id.bt_test)

        btTest.setOnClickListener {
            toogle = if (toogle) {
                ripple.stop()
                !toogle
            } else {
                ripple.restartRipple()
                !toogle
            }
        }
    }
}