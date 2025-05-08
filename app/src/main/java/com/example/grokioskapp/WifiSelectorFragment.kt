package com.example.grokioskapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grokioskapp.databinding.FragmentWifiSelectorBinding
import kotlinx.coroutines.launch

class WiFiSelectorFragment : Fragment(), WiFiNetworkAdapter.OnWiFiNetworkClickListener {
    companion object {
        fun newInstance(): WiFiSelectorFragment {
            return WiFiSelectorFragment()
        }
    }

    private var _binding: FragmentWifiSelectorBinding? = null
    private val binding get() = _binding!!

    private lateinit var wifiManager: WiFiManager
    private lateinit var adapter: WiFiNetworkAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWifiSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wifiManager = WiFiManager(requireContext())

        // Setup RecyclerView
        adapter = WiFiNetworkAdapter(emptyList(), this)
        binding.rvWifiNetworks.layoutManager = LinearLayoutManager(context)
        binding.rvWifiNetworks.adapter = adapter

        // Setup refresh button
        binding.btnRefreshNetworks.setOnClickListener {
            scanWifiNetworks()
        }

        // Initial scan
        scanWifiNetworks()

        // Check WiFi state
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.enableWifi()
            binding.tvStatus.text = "Enabling WiFi..."
        }
    }

    private fun scanWifiNetworks() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = "Scanning for networks..."

        lifecycleScope.launch {
            val networks = wifiManager.scanWifiNetworks()

            binding.progressBar.visibility = View.GONE

            if (networks.isEmpty()) {
                binding.tvStatus.text = "No networks found. Try again."
            } else {
                binding.tvStatus.text = "Select a WiFi network"
                adapter.updateNetworks(networks)
            }
        }
    }

    override fun onWiFiNetworkClick(ssid: String) {
        binding.passwordContainer.visibility = View.VISIBLE
        binding.tvSelectedNetwork.text = ssid

        binding.btnConnect.setOnClickListener {
            val password = binding.etPassword.text.toString()
            connectToNetwork(ssid, password)
        }
    }

    private fun connectToNetwork(ssid: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = "Connecting to $ssid..."

        lifecycleScope.launch {
            val connected = wifiManager.connectToWifiNetwork(ssid, password)

            binding.progressBar.visibility = View.GONE

            if (connected) {
                binding.tvStatus.text = "Connected to $ssid"
                Toast.makeText(context, "Connected to $ssid", Toast.LENGTH_SHORT).show()

                // Notify the activity that we're connected
                (activity as? MainActivity)?.onWifiConnected()
            } else {
                binding.tvStatus.text = "Failed to connect to $ssid"
                Toast.makeText(context, "Failed to connect. Check the password and try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}