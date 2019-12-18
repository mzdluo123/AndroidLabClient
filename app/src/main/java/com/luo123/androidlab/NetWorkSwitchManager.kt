package com.luo123.androidlab

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.webkit.WebView
import android.widget.Toast
import java.net.InetAddress

class NetWorkSwitchManager(val context: Context, val webView: WebView) {
    enum class NetWorkType {
        PUBLIC, PRIVATE
    }

    private var netWorkType = NetWorkType.PUBLIC
    private val PUBLICADDRESS = "https://x.kenvix.com:7352/"
    private val PRIVATEADDRESS = "https://lab.kenvix.com/"
    private var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var isListenerStarted = true

    init {
        //链接变化的监听器
        connectivityManager.requestNetwork(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    if (!isListenerStarted) {
                        return
                    }
                    switch(notice = true)
                }
            })

    }


    //判断是否能链接内网
    private fun isConnByHttp(): Boolean {
        return try {
            val address = InetAddress.getByName("lab.kenvix.com")
            val reachable = address.isReachable(100)
            reachable
        } catch (e: Exception) {
            false
        }

    }

    fun startNetWorkListener() {
        isListenerStarted = true

    }

    fun stopNetWorkListener() {
        isListenerStarted = false

    }

    private fun switch(notice: Boolean = false) {
        val network = connectivityManager.activeNetwork ?: return
        //使用移动网络
        if (connectivityManager.getNetworkCapabilities(network).hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            )
        ) {
            netWorkType = NetWorkType.PUBLIC
        }
        //使用wifi
        if (connectivityManager.getNetworkCapabilities(network).hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )
        ) {
            if (isConnByHttp()) {
                netWorkType = NetWorkType.PRIVATE
                if (notice) {
                    Toast.makeText(context, "下拉刷新可通过内网高速访问论坛", Toast.LENGTH_SHORT).show()
                }
            } else {
                netWorkType = NetWorkType.PUBLIC
            }
        }
    }

    private fun getUrlBase(): String {
        if (netWorkType == NetWorkType.PRIVATE) {
            return PRIVATEADDRESS
        }
        return PUBLICADDRESS
    }

    private fun getNewUrl(new: Boolean): String {
        if (new) {
            return getUrlBase()
        }
        val url = webView.url
        switch()
        if ("file://" in url) {
            return getUrlBase()
        }
        if (netWorkType == NetWorkType.PRIVATE) {
            return url.replace(PUBLICADDRESS, PRIVATEADDRESS)
        } else {
            return url.replace(PRIVATEADDRESS, PUBLICADDRESS)
        }

    }

    fun refresh(handler: Handler) {
        if (!webView.canGoBack()) {
            webView.loadUrl(getNewUrl(true))
            return
        } else {
            handler.post(Runnable {
                webView.loadUrl(getNewUrl(false))
            })
        }

    }


}