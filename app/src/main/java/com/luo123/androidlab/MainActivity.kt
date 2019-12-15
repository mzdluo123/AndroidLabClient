package com.luo123.androidlab

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.*
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


    val FILECHOOSER_RESULTCODE = 1
    var uploadMessage: ValueCallback<Array<Uri>>? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        setUi()

        //链接变化的监听器
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
                }


            })

        //设置
        val settings = main_webView.settings
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.domStorageEnabled = true
        settings.userAgentString += " AndroidLabClient/${BuildConfig.VERSION_NAME}"

        //管理加载资源
        main_webView.webViewClient =
            object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    if (address in request?.url.toString()) {  //如果是论坛内部
                        return false
                    }
                    //如果是外部就使用系统浏览器打开
                    startActivity(Intent(Intent.ACTION_VIEW, request?.url))
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    swipe_refresh.isRefreshing = false
                    Log.d(TAG, "当前url ${view?.url}")
                    if (baseContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {  //横屏状态下
                        window.setFlags(  //全屏
                            WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN
                        )
                        main_webView.settings.apply {
                            //允许缩放
                            builtInZoomControls = true
                            setSupportZoom(true)
                            displayZoomControls = true
                        }
                        main_webView.evaluateJavascript(SCRIPT,  //插入优化代码
                            ValueCallback {

                            })
                    }
                }

            }
        //打开文件选择器事件
        main_webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                    uploadMessage = null
                }
                uploadMessage = filePathCallback

                if (fileChooserParams != null) {
                    //启动文件选择器
                    startActivityForResult(Intent.createChooser(fileChooserParams.createIntent(), "Select a File to Upload"), 1)
                    return true
                }
                return false
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
            //异步任务回调
            handler.post {
                val script: String
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

    }

    //接收文件选择器回调
    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
      if (requestCode == FILECHOOSER_RESULTCODE) {
            if (uploadMessage == null) return
            uploadMessage!!.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(
                    resultCode,
                    intent
                )

            )
            uploadMessage = null
        }
    }


    override fun onResume() {
        if (!main_webView.canGoBack()) {
            if (isConnByHttp()) {
                address = "https://lab.kenvix.com/"
            }
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


    override fun onDestroy() {
        main_webView.clearHistory()
        super.onDestroy()
    }

    //判断是否能链接内网
    fun isConnByHttp(): Boolean {
        return try {
            val address = InetAddress.getByName("lab.kenvix.com")

            val reachable = address.isReachable(100)
            reachable
        } catch (e: Exception) {
            false
        }

    }

}
