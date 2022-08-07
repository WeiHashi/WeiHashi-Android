package cn.devmeteor.weihashi.channel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import cn.devmeteor.weihashi.channel.QQ.Companion.qq
import com.tencent.connect.common.Constants
import com.tencent.connect.share.QzoneShare
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import org.json.JSONObject

class Qzone private constructor() : Channel {

    companion object {
        const val CHANNEL_QZONE = "Qzone"
        val qzone by lazy { Qzone() }
    }

    private var shareListener: ShareListener? = null
    private val tencent: Tencent by lazy { qq.getTencent() }

    override fun share(
        activity: Activity,
        shareContent: ShareContent,
        shareListener: ShareListener
    ) {
        this.shareListener = shareListener
        tencent.shareToQzone(activity, Bundle().apply {
            putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT)
            putString(QzoneShare.SHARE_TO_QQ_TITLE, shareContent.title)
            putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareContent.summary)
            putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, shareContent.url)
        }, shareResult)
    }

    private val shareResult = object : QQ.BaseUiListener() {
        override fun onSuccess(obj: JSONObject) {
            shareListener?.onSuccess(CHANNEL_QZONE)
        }

        override fun onFail(code:Int,reason: String) {
            shareListener?.onFail(CHANNEL_QZONE, reason)
        }

        override fun onCancel() {
            shareListener?.onCancel()
        }
    }

    fun callback(requestCode: Int, resultCode: Int, data: Intent?) {
        val listener: IUiListener? = when (requestCode) {
            Constants.REQUEST_QZONE_SHARE -> shareResult
            else -> null
        }
        listener?.let { Tencent.onActivityResultData(requestCode, resultCode, data, listener) }
    }

    override fun getName(): String = CHANNEL_QZONE
}