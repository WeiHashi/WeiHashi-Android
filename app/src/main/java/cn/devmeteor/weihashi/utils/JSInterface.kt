package cn.devmeteor.weihashi.utils

import android.webkit.JavascriptInterface
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.utils.Util.kv
import com.blankj.utilcode.util.AppUtils

class JSInterface {

    @JavascriptInterface
    fun getOpenid(): String = kv.decodeString(Constants.KEY_OPENID) ?: ""

    @JavascriptInterface
    fun getVersionName(): String = "V${AppUtils.getAppVersionName()}${if (TestManager.isTest()) "测试版" else ""}"

}