package com.example.grokioskapp

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.grokioskapp.databinding.FragmentWebviewBinding

class WebViewFragment : Fragment() {
    companion object {
        private const val TAG = "WebViewFragment"
        private const val DJANGO_APP_URL = "https://your-django-app-url.com" // Replace with your URL

        fun newInstance(): WebViewFragment {
            return WebViewFragment()
        }
    }

    private var _binding: FragmentWebviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebView()
    }

    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            // Prevent zoom controls
            displayZoomControls = false
            builtInZoomControls = false
            // Allow mixed content if needed
            // Cache mode
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                binding.progressBar.visibility = View.GONE
                binding.errorView.visibility = View.VISIBLE
            }
        }

        binding.retryButton.setOnClickListener {
            binding.errorView.visibility = View.GONE
            loadUrl()
        }

        loadUrl()
    }

    private fun loadUrl() {
        binding.webView.loadUrl(DJANGO_APP_URL)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}