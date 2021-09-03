package com.example.myreddot.shapeRipple

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
internal class LifeCycleManager(private val shapeRipple: ShapeRipple?) : ActivityLifecycleCallbacks {
    private var activity: Activity? = null

    fun attachListener() {
        if (shapeRipple == null) {
            return
        }
        activity = getActivity(shapeRipple.context)
        activity?.application?.registerActivityLifecycleCallbacks(this)
    }

    private fun detachListener() {
        if (activity == null) {
            return
        }
        activity?.application?.unregisterActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        if (shapeRipple == null || this.activity !== activity) {
            return
        }
        shapeRipple.restartRipple()
    }

    override fun onActivityPaused(activity: Activity) {
        if (shapeRipple == null || this.activity !== activity) {
            return
        }
        shapeRipple.stop()
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (this.activity !== activity) {
            return
        }
        detachListener()
    }

    private fun getActivity(context: Context): Activity {
        var ctx: Context? = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        throw IllegalArgumentException("Context does not derived from any activity, Do not use the Application Context!!")
    }
}