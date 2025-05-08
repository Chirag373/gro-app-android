package com.example.grokioskapp

import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.grokioskapp.databinding.ItemWifiNetworkBinding

class WiFiNetworkAdapter(
    private var networks: List<ScanResult>,
    private val listener: OnWiFiNetworkClickListener
) : RecyclerView.Adapter<WiFiNetworkAdapter.NetworkViewHolder>() {

    interface OnWiFiNetworkClickListener {
        fun onWiFiNetworkClick(ssid: String)
    }

    fun updateNetworks(newNetworks: List<ScanResult>) {
        networks = newNetworks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkViewHolder {
        val binding = ItemWifiNetworkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NetworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NetworkViewHolder, position: Int) {
        val network = networks[position]
        holder.bind(network)
    }

    override fun getItemCount(): Int = networks.size

    inner class NetworkViewHolder(private val binding: ItemWifiNetworkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(network: ScanResult) {
            binding.tvNetworkName.text = network.SSID

            // Calculate signal strength as percentage
            val signalLevel = WifiManager.calculateSignalLevel(network.level, 5)
            val signalText = when (signalLevel) {
                0 -> "Very weak"
                1 -> "Weak"
                2 -> "Fair"
                3 -> "Good"
                4 -> "Excellent"
                else -> "Unknown"
            }
            binding.tvSignalStrength.text = signalText

            // Handle click event
            binding.root.setOnClickListener {
                listener.onWiFiNetworkClick(network.SSID)
            }
        }
    }
}