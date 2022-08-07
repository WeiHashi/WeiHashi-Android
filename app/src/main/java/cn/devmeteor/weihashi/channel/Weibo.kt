package cn.devmeteor.weihashi.channel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.R
import com.sina.weibo.sdk.api.TextObject
import com.sina.weibo.sdk.api.WebpageObject
import com.sina.weibo.sdk.api.WeiboMultiMessage
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.common.UiError
import com.sina.weibo.sdk.openapi.IWBAPI
import com.sina.weibo.sdk.openapi.WBAPIFactory
import com.sina.weibo.sdk.share.WbShareCallback
import java.io.ByteArrayOutputStream
import java.util.*

class Weibo private constructor() : Channel, WbShareCallback {

    companion object {
        const val CHANNEL_WEIBO = "weibo"
        val weibo by lazy { Weibo() }
    }

    private var shareListener: ShareListener? = null
    private lateinit var api: IWBAPI

    fun init(context: Context) {
        api = WBAPIFactory.createWBAPI(context).apply {
            registerApp(
                context,
                AuthInfo(
                    context,
                    Constants.WEIBO_APP_KEY,
                    Constants.WEIBO_REDIRECT_URL,
                    Constants.WEIBO_SCOPE
                )
            )
        }
    }

    override fun share(
        activity: Activity,
        shareContent: ShareContent,
        shareListener: ShareListener
    ) {
        this.shareListener = shareListener
        api.apply {
            shareMessage(WeiboMultiMessage().apply {
                textObject = TextObject().apply { text = shareContent.summary }
                mediaObject = WebpageObject().apply {
                    defaultText = shareContent.title
                    title = shareContent.title
                    description = shareContent.summary
                    ByteArrayOutputStream().use {
                        val bitmap = BitmapFactory.decodeResource(
                            activity.resources,
                            R.drawable.ic_launcher_radius
                        )
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)
                        thumbData = it.toByteArray()
                    }
                    actionUrl = shareContent.url
                    identify = UUID.randomUUID().toString()
                }
            }, false)
        }
    }

    fun callback(intent: Intent?) {
        api.doResultIntent(intent, this)
    }

    override fun getName(): String = CHANNEL_WEIBO

    override fun onComplete() {
        shareListener?.onSuccess(CHANNEL_WEIBO)
    }

    override fun onCancel() {
        shareListener?.onCancel()
    }

    override fun onError(error: UiError) {
        shareListener?.onFail(CHANNEL_WEIBO, "")
    }

    fun removeCallbacks() {
        shareListener = null
    }
}