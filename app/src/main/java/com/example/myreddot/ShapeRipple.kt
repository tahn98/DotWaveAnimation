package com.example.myreddot

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.example.myreddot.data.ShapeRippleEntry
import com.example.myreddot.model.BaseShape
import java.util.*

/**
 * created by @tahn on 02-sep-2021
 * ref : https://github.com/poldz123/ShapeRipple
 */
class ShapeRipple : View {

    private var rippleColor = 0
    private var rippleFromColor = 0
    private var rippleToColor = 0
    private var rippleDuration = 0
    private var rippleStrokeWidth = 0
    private var rippleInterval = 0f
    private var rippleMaximumRadius = 0f
    private var rippleIntervalFactor = 0f
    private var rippleCount = 0
    private var viewWidth = 0
    private var viewHeight = 0
    private var maxRippleRadius = 0
    private var lastMultiplierValue = 0f
    private var isEnableColorTransition = true
    private var enableSingleRipple = false
    private var isEnableStrokeStyle = false
        set(enableStrokeStyle) {
            field = enableStrokeStyle
            if (enableStrokeStyle) {
                shapePaint!!.style = Paint.Style.STROKE
            } else {
                shapePaint!!.style = Paint.Style.FILL
            }
        }
    private lateinit var shapeRippleEntries: LinkedList<ShapeRippleEntry>
    private var rippleValueAnimator: ValueAnimator? = null
    private lateinit var rippleInterpolator: Interpolator
    private var rippleShape: BaseShape? = null
    private var shapePaint: Paint? = null
    private var isStopped = false
    private var lifeCycleManager: LifeCycleManager? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {

        // initialize the paint for the shape ripple
        shapePaint = Paint()
        shapePaint?.isAntiAlias = true
        shapePaint?.isDither = true
        shapePaint?.style = Paint.Style.FILL
        shapeRippleEntries = LinkedList()
        rippleShape = BaseShape()
        rippleColor = DEFAULT_RIPPLE_COLOR
        rippleFromColor = DEFAULT_RIPPLE_FROM_COLOR
        rippleToColor = DEFAULT_RIPPLE_TO_COLOR
        rippleStrokeWidth = resources.getDimensionPixelSize(R.dimen.default_stroke_width)
        rippleDuration = DEFAULT_RIPPLE_DURATION
        rippleIntervalFactor = DEFAULT_RIPPLE_INTERVAL_FACTOR
        rippleInterpolator = LinearInterpolator()

        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.ConnectingRipple, 0, 0)
            try {
                rippleColor =
                    ta.getColor(R.styleable.ConnectingRipple_ripple_color, DEFAULT_RIPPLE_COLOR)
                rippleFromColor = ta.getColor(
                    R.styleable.ConnectingRipple_ripple_from_color,
                    DEFAULT_RIPPLE_FROM_COLOR
                )
                rippleToColor = ta.getColor(
                    R.styleable.ConnectingRipple_ripple_to_color,
                    DEFAULT_RIPPLE_TO_COLOR
                )
                setRippleDuration(
                    ta.getInteger(
                        R.styleable.ConnectingRipple_ripple_duration,
                        DEFAULT_RIPPLE_DURATION
                    )
                )
                isEnableColorTransition =
                    ta.getBoolean(R.styleable.ConnectingRipple_enable_color_transition, true)
                enableSingleRipple =
                    ta.getBoolean(R.styleable.ConnectingRipple_enable_single_ripple, false)
                rippleMaximumRadius = ta.getDimensionPixelSize(
                    R.styleable.ConnectingRipple_ripple_maximum_radius,
                    NO_VALUE
                ).toFloat()
                rippleCount = ta.getInteger(R.styleable.ConnectingRipple_ripple_count, NO_VALUE)
                isEnableStrokeStyle =
                    ta.getBoolean(R.styleable.ConnectingRipple_enable_stroke_style, false)
                setRippleStrokeWidth(
                    ta.getDimensionPixelSize(
                        R.styleable.ConnectingRipple_ripple_stroke_width,
                        resources.getDimensionPixelSize(R.dimen.default_stroke_width)
                    )
                )
            } finally {
                ta.recycle()
            }
        }

        start(rippleDuration)

        // Only attach the activity for ICE_CREAM_SANDWICH and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            lifeCycleManager = LifeCycleManager(this)
            lifeCycleManager!!.attachListener()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawDot(canvas)

        for (shapeRippleEntry in shapeRippleEntries) {
            if (shapeRippleEntry.isRender) {
                shapeRippleEntry.baseShape?.onDraw(
                    canvas,
                    shapeRippleEntry.x,
                    shapeRippleEntry.y,
                    shapeRippleEntry.radiusSize,
                    shapeRippleEntry.changingColorValue,
                    shapeRippleEntry.rippleIndex,
                    shapePaint!!
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        initializeEntries(rippleShape)
        rippleShape?.width = viewWidth
        rippleShape?.height = viewHeight
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        stop()
    }

    private fun drawDot(canvas: Canvas){
        rippleShape?.onDraw(
            canvas,
            width / 2,
            height / 2,
            10f,
            Color.RED,
            0,
            shapePaint!!
        )
    }

    /**
     * This method will initialize the list of [ShapeRippleEntry] with
     * initial position, color, index, and multiplier value.)
     *
     * @param shapeRipple the renderer of shape ripples
     */
    private fun initializeEntries(shapeRipple: BaseShape?) {
        shapePaint?.strokeWidth = rippleStrokeWidth.toFloat()
        if (viewWidth == 0 && viewHeight == 0) {
            return
        }

        shapeRippleEntries.clear()

        maxRippleRadius =
            if (rippleMaximumRadius != NO_VALUE.toFloat())
                rippleMaximumRadius.toInt()
            else viewWidth.coerceAtMost(viewHeight) / 2 - rippleStrokeWidth / 2
        rippleCount =
            if (rippleCount > NO_VALUE) rippleCount else maxRippleRadius / rippleStrokeWidth
        rippleInterval = DEFAULT_RIPPLE_INTERVAL_FACTOR / rippleCount

        for (i in 0 until rippleCount) {
            val shapeRippleEntry = ShapeRippleEntry(shapeRipple)
            shapeRippleEntry.x = viewWidth / 2
            shapeRippleEntry.y = viewHeight / 2
            shapeRippleEntry.multiplierValue = -(rippleInterval * i.toFloat())
            shapeRippleEntry.rippleIndex = i
            shapeRippleEntry.originalColorValue = rippleColor
            shapeRippleEntries.add(shapeRippleEntry)

            // we only render 1 ripple when it is enabled
            if (enableSingleRipple) {
                break
            }
        }
    }

    /**
     * Refreshes the list of ticket entries after certain options are changed such as the [.rippleColor],
     * This will only execute after the [.initializeEntries], this is safe to call before it.
     */
    private fun reconfigureEntries() {

        // do not re configure when dimension is not calculated
        // or if the list is empty
        if (viewWidth == 0 && viewHeight == 0 && (shapeRippleEntries.size == 0)) {
            return
        }

        // sets the stroke width of the ripple
        shapePaint?.strokeWidth = rippleStrokeWidth.toFloat()
        for (shapeRippleEntry in shapeRippleEntries!!) {
            shapeRippleEntry.originalColorValue = rippleColor
            shapeRippleEntry.baseShape = rippleShape
        }
    }

    /**
     * Start the [.rippleValueAnimator] with specified duration for each ripple.
     */
    private fun start(millis: Int) {

        // Do a ripple value renderer
        rippleValueAnimator = ValueAnimator.ofFloat(0f, 1f)
        rippleValueAnimator?.duration = millis.toLong()
        rippleValueAnimator?.repeatMode = ValueAnimator.RESTART
        rippleValueAnimator?.repeatCount = ValueAnimator.INFINITE
        rippleValueAnimator?.interpolator = rippleInterpolator
        rippleValueAnimator?.addUpdateListener { animation -> render(animation.animatedValue as Float) }
        rippleValueAnimator?.start()
    }

    /**
     * This is the main renderer for the list of ripple, we always check that the first ripple is already
     * finished.
     *
     *
     * When the ripple is finished it is [ShapeRippleEntry.reset] and move to the end of the list to be reused all over again
     * to prevent creating a new instance of it.
     *
     *
     * Each ripple will be configured to be either rendered or not rendered to the view to prevent extra rendering process.
     *
     * @param multiplierValue the current multiplier value of the [.rippleValueAnimator]
     */
    private fun render(multiplierValue: Float) {

        // Do not render when entries are empty
        if (shapeRippleEntries.size == 0) {
            return
        }
        var firstEntry = shapeRippleEntries.peekFirst()!!

        // Calculate the multiplier value of the first entry
        var firstEntryMultiplierValue =
            firstEntry.multiplierValue + (multiplierValue - lastMultiplierValue).coerceAtLeast(0f)

        // Check if the first entry is done the ripple (happens when the ripple reaches to end)
        if (firstEntryMultiplierValue >= 1.0f) {

            // Remove and relocate the first entry to the last entry
            val removedEntry = shapeRippleEntries.pop()
            removedEntry.reset()
            removedEntry.originalColorValue = rippleColor
            shapeRippleEntries.addLast(removedEntry)

            // Get the new first entry of the list
            firstEntry = shapeRippleEntries.peekFirst()!!

            // Calculate the new multiplier value of the first entry of the list
            firstEntryMultiplierValue =
                firstEntry.multiplierValue + (multiplierValue - lastMultiplierValue).coerceAtLeast(
                    0f
                )
            firstEntry.x = viewWidth / 2
            firstEntry.y = viewHeight / 2
            if (enableSingleRipple) {
                firstEntryMultiplierValue = 0f
            }
        }
        var index = 0
        for (shapeRippleEntry in shapeRippleEntries) {

            // set the updated index
            shapeRippleEntry.rippleIndex = index

            // calculate the shape multiplier by index
            val currentEntryMultiplier = firstEntryMultiplierValue - rippleInterval * index

            // Check if we render the current ripple in the list
            // We render when the multiplier value is >= 0
            if (currentEntryMultiplier >= 0) {
                shapeRippleEntry.isRender = true
            } else {
                // We continue to the next item
                // since we know that we do not
                // need the calculations below
                shapeRippleEntry.isRender = false
                continue
            }

            // We already calculated the multiplier value of the first entry of the list
            if (index == 0) {
                shapeRippleEntry.multiplierValue = firstEntryMultiplierValue
            } else {
                shapeRippleEntry.multiplierValue = currentEntryMultiplier
            }

            // calculate the color if we enabled the color transition
            shapeRippleEntry.changingColorValue =
                if (isEnableColorTransition) ShapePulseUtil.evaluateTransitionColor(
                    currentEntryMultiplier,
                    shapeRippleEntry.originalColorValue,
                    rippleToColor
                ) else rippleColor

            // calculate the current ripple size
            shapeRippleEntry.radiusSize = maxRippleRadius * currentEntryMultiplier
            index += 1
        }

        // save the last multiplier value
        lastMultiplierValue = multiplierValue

        // we draw the shapes
        invalidate()
    }

    /**
     * Stop the [.rippleValueAnimator] and clears the [.shapeRippleEntries]
     */
    fun stop() {
        if (rippleValueAnimator != null) {
            rippleValueAnimator?.cancel()
            rippleValueAnimator?.end()
            rippleValueAnimator?.removeAllUpdateListeners()
            rippleValueAnimator?.removeAllListeners()
            rippleValueAnimator = null
        }
        shapeRippleEntries.clear()
        invalidate()
    }

    /**
     * Starts the ripple by stopping the current [.rippleValueAnimator] using the [.stop]
     * then initializing ticket entries using the [.initializeEntries]
     * and lastly starting the [.rippleValueAnimator] using [.start]
     */
    private fun startRipple() {
        //stop the animation from previous before starting it again
        stop()
        initializeEntries(rippleShape)
        start(rippleDuration)
        isStopped = false
    }

    /**
     * Stops the ripple see [.stop] for more details
     */
    fun stopRipple() {
        stop()
        isStopped = true
    }

    /**
     * This restarts the ripple or continue where it was left off, this is mostly used
     * for [LifeCycleManager].
     */
    fun restartRipple() {
        if (isStopped) {
            return
        }
        startRipple()
    }


    fun getRippleMaximumRadius(): Float {
        return maxRippleRadius.toFloat()
    }


    fun isEnableSingleRipple(): Boolean {
        return enableSingleRipple
    }


    fun getRippleStrokeWidth(): Int {
        return rippleStrokeWidth
    }

    fun getRippleColor(): Int {
        return rippleColor
    }

    fun getRippleFromColor(): Int {
        return rippleFromColor
    }

    fun getRippleToColor(): Int {
        return rippleToColor
    }

    fun getRippleDuration(): Int {
        return rippleDuration
    }

    fun getRippleCount(): Int {
        return rippleCount
    }

    fun getRippleInterpolator(): Interpolator? {
        return rippleInterpolator
    }

    fun getRippleShape(): BaseShape? {
        return rippleShape
    }


    /**
     * Change the maximum size of the ripple, default to the size of the layout.
     *
     *
     * Value must be greater than 1
     *
     * @param rippleMaximumRadius The floating ripple interval for each ripple
     */
    fun setRippleMaximumRadius(rippleMaximumRadius: Float) {
        require(rippleMaximumRadius > NO_VALUE) { "Ripple max radius must be greater than 0" }
        this.rippleMaximumRadius = rippleMaximumRadius
        requestLayout()
    }

    /**
     * Enables the single ripple rendering
     *
     * @param enableSingleRipple flag for enabling single ripple
     */
    fun setEnableSingleRipple(enableSingleRipple: Boolean) {
        this.enableSingleRipple = enableSingleRipple
        initializeEntries(rippleShape)
    }

    /**
     * Change the stroke width for each ripple
     *
     * @param rippleStrokeWidth The stroke width in pixel
     */
    fun setRippleStrokeWidth(rippleStrokeWidth: Int) {
        require(rippleStrokeWidth > 0) { "Ripple duration must be > 0" }
        this.rippleStrokeWidth = rippleStrokeWidth
    }

    /**
     * Change the base color of each ripple
     *
     * @param rippleColor The ripple color
     */
    fun setRippleColor(rippleColor: Int) {
        setRippleColor(rippleColor, true)
    }

    /**
     * Change the base color of each ripple
     *
     * @param rippleColor The ripple color
     * @param instant     flag for when changing color is instant without delay
     */
    fun setRippleColor(rippleColor: Int, instant: Boolean) {
        this.rippleColor = rippleColor
        if (instant) {
            reconfigureEntries()
        }
    }

    /**
     * Change the starting color of the color transition
     *
     * @param rippleFromColor The starting color
     */
    fun setRippleFromColor(rippleFromColor: Int) {
        setRippleFromColor(rippleFromColor, true)
    }

    /**
     * Change the starting color of the color transition
     *
     * @param rippleFromColor The starting color
     * @param instant         flag for when changing color is instant without delay
     */
    fun setRippleFromColor(rippleFromColor: Int, instant: Boolean) {
        this.rippleFromColor = rippleFromColor
        if (instant) {
            reconfigureEntries()
        }
    }

    /**
     * Change the end color of the color transition
     *
     * @param rippleToColor The end color
     */
    fun setRippleToColor(rippleToColor: Int) {
        setRippleToColor(rippleToColor, true)
    }

    /**
     * Change the end color of the color transition
     *
     * @param rippleToColor The end color
     * @param instant       flag for when changing color is instant without delay
     */
    fun setRippleToColor(rippleToColor: Int, instant: Boolean) {
        this.rippleToColor = rippleToColor
        if (instant) {
            reconfigureEntries()
        }
    }

    /**
     * Change the ripple duration of the animator
     *
     * @param millis The duration in milliseconds
     */
    fun setRippleDuration(millis: Int) {
        require(rippleDuration > 0) { "Ripple duration must be > 0" }
        rippleDuration = millis

        // We set the duration here this will auto change the animator
        if (rippleValueAnimator != null) {
            rippleValueAnimator!!.duration = rippleDuration.toLong()
        }
    }

    /**
     * Change the [Interpolator] of the animator
     *
     * @param rippleInterpolator The interpolator
     */
    fun setRippleInterpolator(rippleInterpolator: Interpolator?) {
        if (rippleInterpolator == null) {
            throw NullPointerException("Ripple interpolator in null")
        }
        this.rippleInterpolator = rippleInterpolator
    }

    /**
     * Change the number of ripples, default value is calculated based on the
     * layout_width / ripple_width.
     *
     * @param rippleCount The number of ripples
     */
    fun setRippleCount(rippleCount: Int) {
        if (rippleCount <= NO_VALUE) {
            throw NullPointerException("Invalid ripple count")
        }
        this.rippleCount = rippleCount
        requestLayout()
    }

    /**
     * Change the shape renderer of the ripples
     *
     * @param rippleShape The renderer of shapes ripple
     */
    fun setRippleShape(rippleShape: BaseShape?) {
        this.rippleShape = rippleShape
        reconfigureEntries()
    }

    companion object {
        val TAG = ShapeRipple::class.java.simpleName
        private const val NO_VALUE = 0

        /**
         * Debug logging flag for the library
         */
        var DEBUG = false

        private val DEFAULT_RIPPLE_COLOR = Color.parseColor("#FFF44336")

        private val DEFAULT_RIPPLE_FROM_COLOR = Color.parseColor("#FFF44336")

        private val DEFAULT_RIPPLE_TO_COLOR = Color.parseColor("#00FFFFFF")

        private const val DEFAULT_RIPPLE_DURATION = 1500

        private const val DEFAULT_RIPPLE_INTERVAL_FACTOR = 1f

        fun enableDebugging() {
            DEBUG = true
        }
    }
}