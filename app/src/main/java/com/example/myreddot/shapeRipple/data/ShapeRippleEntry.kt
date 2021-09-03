package com.example.myreddot.shapeRipple.data

import android.graphics.Color
import com.example.myreddot.shapeRipple.data.CircleShape

class ShapeRippleEntry(
    var circleShape: CircleShape?
) {
    var isRender = false
    var radiusSize = 0f
    var multiplierValue = 0f
    var rippleIndex = 0
    var x = 0
    var y = 0
    var originalColorValue = 0
        set(value){
            changingColorValue = value
            field = value
        }
    var changingColorValue = 0

    fun reset() {
        isRender = false
        multiplierValue = -1f
        radiusSize = 0f
        originalColorValue = Color.TRANSPARENT
        changingColorValue = Color.TRANSPARENT
    }
}