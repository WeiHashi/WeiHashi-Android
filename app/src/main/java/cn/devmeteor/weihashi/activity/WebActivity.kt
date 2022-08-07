package cn.devmeteor.weihashi.activity

import android.annotation.SuppressLint
import cn.devmeteor.weihashi.base.BaseActionBarActivity
import cn.devmeteor.weihashi.databinding.ActivityWebBinding
import cn.devmeteor.weihashi.inflate
import cn.devmeteor.weihashi.utils.JSInterface
import com.tencent.smtt.export.external.interfaces.JsResult
import com.tencent.smtt.export.external.interfaces.SslError
import com.tencent.smtt.export.external.interfaces.SslErrorHandler
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient

class WebActivity : BaseActionBarActivity() {

    companion object {
        const val PARAM_URL = "url"
        const val PARAM_PARAMS = "params"
        const val PARAM_ACTION = "action"
        const val ACTION_GET = 0
        const val ACTION_POST = 1
    }

    val binding: ActivityWebBinding by inflate()
    private var params: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun init() {
        setSupportActionBar("加载中...", binding)
        binding.webView.addJavascriptInterface(JSInterface(), "whsInterface")
        binding.webView.settings.apply {
            allowFileAccess = true
            useWideViewPort = true
            javaScriptCanOpenWindowsAutomatically = true
            loadWithOverviewMode = true
            setUserAgent(userAgentString.replace("Android", "HFWSH_USER Android"))
            javaScriptEnabled = true
            domStorageEnabled = true
            setGeolocationEnabled(true)
            setAppCacheEnabled(true)
            setSupportZoom(false)
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onLoadResource(p0: WebView?, p1: String?) {
                p0?.let { setActionBarTitle(it.title) }
            }

            override fun onReceivedSslError(p0: WebView?, p1: SslErrorHandler?, p2: SslError?) {
                p1?.proceed()
            }
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onJsConfirm(
                p0: WebView?,
                p1: String?,
                p2: String?,
                p3: JsResult?
            ): Boolean {
                showDialog("提示", p2 ?: "", { dialog, index ->
                    if (index == 0) p3?.confirm() else p3?.cancel()
                    dialog?.dismiss()
                })
                return true
            }
        }
        intent.getStringExtra(PARAM_PARAMS)?.apply { params = this }
        binding.webView.apply {
            val url = intent.getStringExtra(PARAM_URL) ?: ""
            when (intent.getIntExtra(PARAM_ACTION, ACTION_GET)) {
                ACTION_GET -> loadUrl("$url${if (params.isBlank()) "" else "?$params"}")
                ACTION_POST -> postUrl(url, params.toByteArray())
            }
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack())
            binding.webView.goBack()
        else
            finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.apply {
            stopLoading()
            settings.javaScriptEnabled = false
            clearHistory()
            removeAllViews()
            destroy()
        }
    }
}