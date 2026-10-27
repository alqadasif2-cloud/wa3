package com.trustwallet.pro

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val TAG = "TrustWalletPro"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        
        // Prevent white flashes while loading
        webView.setBackgroundColor(Color.parseColor("#070b13"))

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        // Configure WebViewAssetLoader to serve local assets under secure https:// origin
        // This is mandatory for ES Modules, CORS resolution, and LocalStorage persistence
        val assetLoader = WebViewAssetLoader.Builder()
            .setDomain("appassets.androidplatform.net")
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        // Capture JavaScript logs and errors directly in Android Logcat
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    val level = it.messageLevel()
                    val msg = "JS [${level}]: ${it.message()} (${it.sourceId()}:${it.lineNumber()})"
                    when (level) {
                        ConsoleMessage.MessageLevel.ERROR -> Log.e(TAG, msg)
                        ConsoleMessage.MessageLevel.WARNING -> Log.w(TAG, msg)
                        else -> Log.d(TAG, msg)
                    }
                }
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url ?: return null
                Log.d(TAG, "AssetLoader intercepting: $url")
                return assetLoader.shouldInterceptRequest(url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                val isMain = request?.isForMainFrame == true
                Log.e(TAG, "Resource error on [main=$isMain] ${request?.url}: ${error?.description} (code ${error?.errorCode})")

                // If loading from appassets domain fails on main frame, fallback to file URL as safety net
                if (isMain && request?.url.toString().startsWith("https://appassets.androidplatform.net/")) {
                    Log.w(TAG, "Falling back to file:///android_asset/dist/index.html")
                    view?.post {
                        view.loadUrl("file:///android_asset/dist/index.html")
                    }
                }
            }
        }

        // Load entry URL under https://appassets.androidplatform.net/
        val entryUrl = "https://appassets.androidplatform.net/assets/dist/index.html"
        Log.i(TAG, "Launching WebView entry URL: $entryUrl")
        webView.loadUrl(entryUrl)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
