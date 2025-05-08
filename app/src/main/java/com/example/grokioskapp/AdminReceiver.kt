package com.example.grokioskapp

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserManager
import android.util.Log

class AdminReceiver : DeviceAdminReceiver() {
    companion object {
        private const val TAG = "AdminReceiver"

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, AdminReceiver::class.java)
        }

        fun getDevicePolicyManager(context: Context): DevicePolicyManager {
            return context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        }

        fun isDeviceOwner(context: Context): Boolean {
            val dpm = getDevicePolicyManager(context)
            return dpm.isDeviceOwnerApp(context.packageName)
        }

        fun setKioskPolicies(context: Context, enable: Boolean) {
            val dpm = getDevicePolicyManager(context)
            val componentName = getComponentName(context)

            if (!dpm.isDeviceOwnerApp(context.packageName)) {
                Log.e(TAG, "Not device owner, cannot set kiosk policies")
                return
            }

            if (enable) {
                try {
                    // Lock task mode policies
                    dpm.setLockTaskPackages(componentName, arrayOf(context.packageName))

                    // Disable keyguard and status bar
                    dpm.setKeyguardDisabled(componentName, true)
                    dpm.setStatusBarDisabled(componentName, true)

                    // Disable system apps - properly accessing the SystemUpdatePolicy class
                    val updatePolicy = SystemUpdatePolicy.createWindowedInstallPolicy(0, 0)
                    dpm.setSystemUpdatePolicy(componentName, updatePolicy)

                    // Disable user control
                    dpm.addUserRestriction(componentName, UserManager.DISALLOW_SAFE_BOOT)
                    dpm.addUserRestriction(componentName, UserManager.DISALLOW_FACTORY_RESET)
                    dpm.addUserRestriction(componentName, UserManager.DISALLOW_ADD_USER)
                    dpm.addUserRestriction(componentName, UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA)
                    dpm.addUserRestriction(componentName, UserManager.DISALLOW_ADJUST_VOLUME)

                    // Disable specific features
                    dpm.setCameraDisabled(componentName, true)
                    dpm.setScreenCaptureDisabled(componentName, true)

                    Log.i(TAG, "Kiosk policies enabled")
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting kiosk policies: ${e.message}")
                }
            } else {
                try {
                    // Clear all policies
                    dpm.clearDeviceOwnerApp(context.packageName)
                    Log.i(TAG, "Kiosk policies disabled")
                } catch (e: Exception) {
                    Log.e(TAG, "Error clearing kiosk policies: ${e.message}")
                }
            }
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Device admin enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.i(TAG, "Device admin disabled")
    }
}