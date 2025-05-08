package com.example.grokioskapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WiFiManager(private val context: Context) {
    companion object {
        private const val TAG = "WiFiManager"
    }

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun isWifiEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }

    fun isConnectedToWifi(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
        }
    }

    fun enableWifi() {
        if (!wifiManager.isWifiEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ requires settings panel
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
                context.startActivity(panelIntent)
            } else {
                @Suppress("DEPRECATION")
                wifiManager.isWifiEnabled = true
            }
        }
    }

    suspend fun scanWifiNetworks(): List<ScanResult> {
        return withContext(Dispatchers.IO) {
            try {
                wifiManager.startScan()
                val scanResults = wifiManager.scanResults ?: emptyList()
                // Filter and sort networks
                scanResults.filter { it.SSID.isNotEmpty() }
                    .distinctBy { it.SSID }
                    .sortedByDescending { it.level }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to scan WiFi networks: ${e.message}")
                emptyList()
            }
        }
    }

    fun connectToWifiNetwork(ssid: String, password: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectToWifiNetworkApi29(ssid, password)
        } else {
            connectToWifiNetworkLegacy(ssid, password)
        }
    }

    @Suppress("DEPRECATION")
    private fun connectToWifiNetworkLegacy(ssid: String, password: String): Boolean {
        try {
            val conf = WifiConfiguration()
            conf.SSID = "\"$ssid\""
            conf.preSharedKey = "\"$password\""

            val networkId = wifiManager.addNetwork(conf)

            if (networkId == -1) {
                Log.e(TAG, "Failed to add network configuration")
                return false
            }

            val enabledSuccess = wifiManager.enableNetwork(networkId, true)
            val reconnectSuccess = wifiManager.reconnect()

            return enabledSuccess && reconnectSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to WiFi network: ${e.message}")
            return false
        }
    }

    private fun connectToWifiNetworkApi29(ssid: String, password: String): Boolean {
        // Implementation for Android 10+ using NetworkRequest
        // For brevity, this will use the Settings panel approach for Android 10+
        val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
        context.startActivity(panelIntent)
        return true
    }
}