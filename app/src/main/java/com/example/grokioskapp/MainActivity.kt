package com.example.grokioskapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.grokioskapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var kioskManager: KioskManager
    private lateinit var wifiManager: WiFiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kioskManager = KioskManager(this)
        wifiManager = WiFiManager(this)

        // Setup kiosk mode
        startKioskMode()

        // Check the WiFi status and load the appropriate fragment
        checkConnectivityAndNavigate()
    }

    private fun startKioskMode() {
        if (kioskManager.isDeviceOwner()) {
            Log.i(TAG, "Starting kiosk mode as device owner")
            kioskManager.startKioskMode(this)
        } else {
            Log.w(TAG, "Not device owner, some kiosk features will be limited")
            // Add instructions for setting as device owner
        }
    }

    private fun checkConnectivityAndNavigate() {
        if (wifiManager.isConnectedToWifi()) {
            // Already connected, load WebView
            loadWebViewFragment()
        } else {
            // Not connected, show WiFi selector
            loadWiFiSelectorFragment()
        }
    }

    fun onWifiConnected() {
        // Called when the WiFi selector fragment successfully connects
        loadWebViewFragment()
    }

    private fun loadWiFiSelectorFragment() {
        replaceFragment(WiFiSelectorFragment.newInstance())
    }

    private fun loadWebViewFragment() {
        replaceFragment(WebViewFragment.newInstance())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // Ensure kiosk mode is active when resuming
        if (!kioskManager.isInKioskMode()) {
            kioskManager.startKioskMode(this)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Block system keys in kiosk mode
        return kioskManager.onKeyEvent(keyCode) || super.onKeyDown(keyCode, event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Block specific touch gestures in kiosk mode if needed
        return if (ev != null && kioskManager.onTouchEvent(ev)) {
            true
        } else {
            super.dispatchTouchEvent(ev)
        }
    }

    // Block app from being closed
    @Suppress("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Don't call super to prevent back key from working
    }

    // Restart activity if app is launched again
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Check if we need to update the UI based on connectivity
        checkConnectivityAndNavigate()
    }
}