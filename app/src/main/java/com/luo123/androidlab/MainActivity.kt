package com.luo123.androidlab

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.net.InetAddress


class MainActivity : AppCompatActivity() {


    private val handler = Handler()

    private val TAG = "Main"
    private var address = "https://x.kenvix.com:7352/"
    val SCRIPT = """
        $('#header-menu').hide();
        $('#nav-dropdown').removeAttr('');
        $('#content > div > div.visible-xs.visible-sm.pagination-block.text-center.ready > div.wrapper').hide();
    """.trimIndent()

    private lateinit var connectivityManager: ConnectivityManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        setUi()

        connectivityManager.requestNetwork(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {

                    //使用移动网络
                    if (connectivityManager.getNetworkCapabilities(network).hasTransport(
                            NetworkCapabilities.TRANSPORT_CELLULAR
                        )
                    ) {
                        Log.d(TAG, "使用外网")
                        address = "https://x.kenvix.com:7352/"

                    }
                    //使用wifi
                    if (connectivityManager.getNetworkCapabilities(network).hasTransport(
                            NetworkCapabilities.TRANSPORT_WIFI
                        )
                    ) {
                        if (isConnByHttp()) {
                            Log.d(TAG, "使用内网")
                            address = "https://lab.kenvix.com/"
                        }
                    }
                    //异步任务回调
                    handler.post {
                        var script: String
                        if ("https://x.kenvix.com:7352/" in main_webView.url.toString()) {
                            script = "window.location.href = '${main_webView.url.toString().replace(
                                "https://x.kenvix.com:7352/", address
                            )}'"
                        } else {

                            script = "window.location.href = '${main_webView.url.toString().replace(
                                "https://lab.kenvix.com/", address
                            )}'"
                        }
                        main_webView.evaluateJavascript(script, ValueCallback { })

                    }
                }


            })

        //设置
        val settings = main_webView.settings
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.userAgentString += " AndroidLabClient/${BuildConfig.VERSION_NAME}"

        //管理加载资源
        main_webView.webViewClient =
            object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return address in main_webView.url.toString()
                }


                override fun onPageFinished(view: WebView?, url: String?) {
                    Log.d(TAG, "当前url ${view?.url}")
                    if (baseContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {  //横屏状态下
                        getWindow().setFlags(
                            WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN
                        )
                        main_webView.settings.apply {
                            setBuiltInZoomControls(true)
                            setSupportZoom(true)
                            setDisplayZoomControls(true)
                        }
                        main_webView.evaluateJavascript(SCRIPT,
                            ValueCallback {

                            })
                    }
                }
            }

        //下拉刷新的颜色
        swipe_refresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light, android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        //下拉刷新回调
        swipe_refresh.setOnRefreshListener {
            main_webView.clearCache(true)
            main_webView.reload()
            swipe_refresh.isRefreshing = false
        }

    }

    override fun onResume() {
        if (!main_webView.canGoBack()) {
            main_webView.loadUrl(address)   //如果是冷启动加载首页
        }
        super.onResume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && main_webView.canGoBack()) {
            main_webView.goBack()  //back键返回
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun setUi() {
        this.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR   //设置白底黑图标状态栏
        supportActionBar?.hide()   //隐藏标题栏
    }


    //保存和加载信息
    override fun onSaveInstanceState(outState: Bundle) {
        main_webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        main_webView.restoreState(savedInstanceState)

    }


    //判断是否能链接内网
    fun isConnByHttp(): Boolean {
        try {
            val address = InetAddress.getByName("lab.kenvix.com")

            val reachable = address.isReachable(100)
            return reachable
        } catch (e: Exception) {
            return false
        }

    }
}
