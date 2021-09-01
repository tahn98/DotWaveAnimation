package com.example.myreddot

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import java.util.*

/**
 * Created by tahn on 31/08/2021.
 */
class DotWave : RelativeLayout {
    private var mDotWaveColor = DEFAULT_COLOR
    private var mStrokeWidth = 0f
    private var mWaveRadius = 0f
    private var mWaveDurationTime = 0
    private var mWaveAmount = 0

    private var mWaveDelay = 0
    private var mWaveScale = 0f
    private var mWaveType = 0
    private var mWaveSize = 0
    private var mWaveAlpha = DEFAULT_ALPHA

    private var mPaint: Paint? = null

    var isRippleAnimationRunning = false
        private set
    private var animatorSet: AnimatorSet? = null
    private var animatorList: ArrayList<Animator>? = null
    private var waveLayoutParams: LayoutParams? = null
    private val waveList = ArrayList<RippleView>()

    constructor(context: Context?) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (isInEditMode) return
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DotWave)

        mDotWaveColor = typedArray.getColor(
            R.styleable.DotWave_dw_color,
            DEFAULT_COLOR
        )
        mStrokeWidth = typedArray.getDimension(
            R.styleable.DotWave_dw_strokeWidth,
            resources.getDimension(R.dimen.rippleStrokeWidth)
        )
        mWaveRadius = typedArray.getDimension(
            R.styleable.DotWave_dw_radius,
            resources.getDimension(R.dimen.rippleRadius)
        )
        mWaveDurationTime =
            typedArray.getInt(R.styleable.DotWave_dw_duration, DEFAULT_DURATION_TIME)
        mWaveAmount =
            typedArray.getInt(R.styleable.DotWave_dw_rippleAmount, DEFAULT_RIPPLE_COUNT)
        mWaveScale = typedArray.getFloat(R.styleable.DotWave_dw_scale, DEFAULT_SCALE)
        mWaveType = typedArray.getInt(R.styleable.DotWave_dw_type, DEFAULT_FILL_TYPE)
        mWaveSize = typedArray.getDimension(R.styleable.DotWave_dw_size, resources.getDimension(R.dimen.rippleStrokeWidth))
            .toInt()
        mWaveAlpha = typedArray.getFloat(R.styleable.DotWave_dw_alpha, DEFAULT_ALPHA)
        typedArray.recycle()

        mWaveDelay = mWaveDurationTime / mWaveAmount
        mPaint = Paint()
        mPaint?.isAntiAlias = true
        if (mWaveType == DEFAULT_FILL_TYPE) {
            mStrokeWidth = 0f
            mPaint?.style = Paint.Style.FILL
        } else mPaint?.style = Paint.Style.STROKE
        mPaint?.color = mDotWaveColor

        waveLayoutParams = LayoutParams(
            (2 * (mWaveRadius + mStrokeWidth)).toInt(),
            (2 * (mWaveRadius + mStrokeWidth)).toInt()
        )
        waveLayoutParams?.addRule(CENTER_IN_PARENT, TRUE)
        animatorSet = AnimatorSet()
        animatorSet?.interpolator = AccelerateDecelerateInterpolator()
        animatorList = ArrayList()

        for (i in 0 until mWaveAmount) {
            val rippleView = RippleView(getContext())
            addView(rippleView, waveLayoutParams)
            waveList.add(rippleView)
            val scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleX", 1f, mWaveScale)
            scaleXAnimator.repeatCount = ObjectAnimator.INFINITE
            scaleXAnimator.repeatMode = ObjectAnimator.RESTART
            scaleXAnimator.startDelay = (i * mWaveDelay).toLong()
            scaleXAnimator.duration = mWaveDurationTime.toLong()
            animatorList?.add(scaleXAnimator)
            val scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleY", 1f, mWaveScale)
            scaleYAnimator.repeatCount = ObjectAnimator.INFINITE
            scaleYAnimator.repeatMode = ObjectAnimator.RESTART
            scaleYAnimator.startDelay = (i * mWaveDelay).toLong()
            scaleYAnimator.duration = mWaveDurationTime.toLong()
            animatorList?.add(scaleYAnimator)
            val alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", mWaveAlpha, 0f)
            alphaAnimator.repeatCount = ObjectAnimator.INFINITE
            alphaAnimator.repeatMode = ObjectAnimator.RESTART
            alphaAnimator.startDelay = (i * mWaveDelay).toLong()
            alphaAnimator.duration = mWaveDurationTime.toLong()
            animatorList?.add(alphaAnimator)
        }

        addView(CircleView(getContext()), LayoutParams(
            mWaveSize,
            mWaveSize
        ).apply {
            addRule(CENTER_IN_PARENT, TRUE)
        })

        animatorSet?.playTogether(animatorList)
    }

    private inner class RippleView(context: Context?) : View(context) {
        override fun onDraw(canvas: Canvas) {
            val radius = width.coerceAtMost(height) / 2
            canvas.drawCircle(
                radius.toFloat(), radius.toFloat(), radius - mStrokeWidth,
                mPaint!!
            )
        }

        init {
            this.visibility = INVISIBLE
        }
    }

    private inner class CircleView : View {
        private var circleColor = Color.RED
        private var paint: Paint? = null

        constructor(context: Context) : super(context) {
            init(context, null)
        }

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            init(context, attrs)
        }

        private fun init(context: Context, attrs: AttributeSet?) {
            paint = Paint()
            paint?.isAntiAlias = true
        }

        fun setCircleColor(circleColor: Int) {
            this.circleColor = circleColor
            invalidate()
        }

        fun getCircleColor(): Int {
            return circleColor
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = mWaveSize
            val h = mWaveSize
            val pl = paddingLeft
            val pr = paddingRight
            val pt = paddingTop
            val pb = paddingBottom
            val usableWidth = w - (pl + pr)
            val usableHeight = h - (pt + pb)
            val radius = usableWidth.coerceAtMost(usableHeight) / 2
            val cx = pl + usableWidth / 2
            val cy = pt + usableHeight / 2
            paint?.color = circleColor
            canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), paint!!)
        }
    }

    fun startRippleAnimation() {
        if (!isRippleAnimationRunning) {
            for (rippleView in waveList) {
                rippleView.visibility = VISIBLE
            }
            animatorSet!!.start()
            isRippleAnimationRunning = true
        }
    }

    fun stopRippleAnimation() {
        if (isRippleAnimationRunning) {
            animatorSet!!.end()
            isRippleAnimationRunning = false
        }
    }

    companion object {
        private const val DEFAULT_COLOR = Color.RED
        private const val DEFAULT_RIPPLE_COUNT = 1
        private const val DEFAULT_DURATION_TIME = 800
        private const val DEFAULT_SCALE = 6.0f
        private const val DEFAULT_FILL_TYPE = 0
        private const val DEFAULT_ALPHA = 1f
    }
}