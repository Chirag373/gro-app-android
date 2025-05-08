package com.example.grokioskapp

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager

class KioskManager(private val context: Context) {
    companion object {
        private const val TAG = "KioskManager"
    }

    private val dpm = AdminReceiver.getDevicePolicyManager(context)
    private val componentName = AdminReceiver.getComponentName(context)

    fun isDeviceOwner(): Boolean {
        return AdminReceiver.isDeviceOwner(context)
    }

    fun startKioskMode(activity: Activity) {
        if (isDeviceOwner()) {
            try {
                // Set kiosk policies
                AdminReceiver.setKioskPolicies(context, true)

                // Enter lock task mode
                if (dpm.isLockTaskPermitted(context.packageName)) {
                    activity.startLockTask()
                    Log.i(TAG, "Started lock task mode")

                    // Apply window flags for kiosk mode
                    applyKioskWindowFlags(activity)
                } else {
                    Log.e(TAG, "Lock task not permitted for this package")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start kiosk mode: ${e.message}")
            }
        } else {
            Log.e(TAG, "Not device owner, cannot start kiosk mode")
        }
    }

    private fun applyKioskWindowFlags(activity: Activity) {
        activity.window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )
    }

    fun isInKioskMode(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        } else {
            activityManager.isInLockTaskMode
        }
    }

    // Handle key events to block system keys
    fun onKeyEvent(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_POWER,
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_APP_SWITCH,
            KeyEvent.KEYCODE_SEARCH -> true // Consume the event
            else -> false // Let other keys pass through
        }
    }

    // Handle touch events to prevent specific gestures
    fun onTouchEvent(event: MotionEvent): Boolean {
        // Implement gesture detection here if needed
        return false
    }
}