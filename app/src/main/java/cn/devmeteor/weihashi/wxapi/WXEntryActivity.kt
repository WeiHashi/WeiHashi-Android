package cn.devmeteor.weihashi.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import cn.devmeteor.weihashi.channel.WeiXin.Companion.weiXin

class WXEntryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weiXin.handleIntent(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        weiXin.handleIntent(intent)
        finish()
    }
}