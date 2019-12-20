package com.luo123.androidlab

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.luo123.androidlab.update.Updater
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private val handler = Handler()
    private val TAG = "Main"
    private var address = "https://x.kenvix.com:7352/"
    private val SCRIPT_FULLSCREEN = """
        $('#header-menu').hide();
        $('#nav-dropdown').removeAttr('');
        $('#content > div > div.visible-xs.visible-sm.pagination-block.text-center.ready > div.wrapper').hide();
        
    """.trimIndent()
    private val SCRIPT_SEETING = """
        $('#app-setting').remove();
$('#main-nav').after(`
<ul id="app-setting" class="nav navbar-nav pull-left">
					<li class="">
						<a class="navigation-link" href="androidlab://setting" title="" id="" data-original-title="客户端设置">
							
							<i class="fa fa-fw fa-wrench" data-content=""></i>
							<span class="visible-xs-inline">客户端设置</span>
							
						</a>
					</li>
	
				</ul>
`);
        """.trimIndent()

    private lateinit var netWorkSwitchManager: NetWorkSwitchManager

    private val FILECHOOSER_RESULTCODE = 1
    var uploadMessage: ValueCallback<Array<Uri>>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setUi()
        swipe_refresh.isRefreshing = true

        netWorkSwitchManager = NetWorkSwitchManager(baseContext, main_webView)
        //设置
        val settings = main_webView.settings
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.domStorageEnabled = true
        settings.userAgentString += " AndroidLabClient/${BuildConfig.VERSION_NAME}"

        //管理加载资源
        main_webView.webViewClient =
            object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    if ("file:///android_asset/index.html" in request?.url.toString()) { //如果是错误界面
                        return false
                    }
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
                    main_webView.evaluateJavascript(SCRIPT_SEETING, {})
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
                        main_webView.evaluateJavascript(SCRIPT_FULLSCREEN, {})  //插入优化代码
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    view?.loadUrl("file:///android_asset/index.html?errorCode=${error?.description}")
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
                    startActivityForResult(
                        Intent.createChooser(
                            fileChooserParams.createIntent(),
                            "Select a File to Upload"
                        ), 1
                    )
                    return true
                }
                return false
            }

        }

        //下拉刷新的颜色
        swipe_refresh.setColorSchemeResources(
            R.color.bbs_blue,
            android.R.color.holo_green_light, android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        //下拉刷新回调
        swipe_refresh.setOnRefreshListener {
            main_webView.clearCache(false)
            //异步任务回调
            netWorkSwitchManager.refresh(handler)

        }
        try {
            Updater(this, handler).checkUpdate(false)
        } catch (e: Exception) {
            Toast.makeText(baseContext, "无法检查更新", Toast.LENGTH_SHORT).show()
        }
//        val alert = AlertDialog.Builder(baseContext,R.style.Theme_AppCompat_Dialog)
//        alert.setMessage("lalalla")
//        alert.show()

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
        super.onActivityResult(requestCode, resultCode, intent)
    }

    override fun onPause() {
        netWorkSwitchManager.stopNetWorkListener()
        super.onPause()
    }

    override fun onResume() {
        netWorkSwitchManager.startNetWorkListener()
        netWorkSwitchManager.refresh(handler)
        super.onResume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ("file://" in main_webView.url.toString()) {   //错误界面直接退出
                finish()
                return true
            }
            if (main_webView.canGoBack()) {
                main_webView.goBack()  //back键返回
                return true
            }

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        main_webView.clearHistory()
        super.onDestroy()
    }


}
