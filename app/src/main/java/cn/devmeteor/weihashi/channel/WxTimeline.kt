package cn.devmeteor.weihashi.channel

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.channel.WeiXin.Companion.weiXin
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import java.io.ByteArrayOutputStream

class WxTimeline private constructor() : Channel, IWXAPIEventHandler {

    companion object {
        const val CHANNEL_WX_TIMELINE = "wx_timeline"
        val wxTimeLine by lazy { WxTimeline() }
    }

    private var shareListener: ShareListener? = null
    private val api: IWXAPI by lazy { weiXin.getApi() }

    override fun share(
        activity: Activity,
        shareContent: ShareContent,
        shareListener: ShareListener
    ) {
        this.shareListener = shareListener
        when (shareContent.type) {
            ShareContent.TYPE_WEB -> shareWeb(activity, shareContent)
        }
    }

    private fun shareWeb(
        activity: Activity,
        shareContent: ShareContent
    ) {
        val web = WXWebpageObject().apply { webpageUrl = shareContent.url }
        val msg = WXMediaMessage(web).apply {
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
        }
        val req = SendMessageToWX.Req().apply {
            transaction = System.currentTimeMillis().toString()
            message = msg
            scene = SendMessageToWX.Req.WXSceneTimeline
        }
        api.sendReq(req)
    }

    private class Receiver(private val api: IWXAPI) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            api.registerApp(Constants.WX_APP_ID)
        }
    }

    fun handleIntent(intent: Intent) {
        api.handleIntent(intent, this)
    }

    override fun getName(): String = CHANNEL_WX_TIMELINE

    override fun onReq(req: BaseReq) {
    }

    override fun onResp(resp: BaseResp) {
        when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK -> shareListener?.onSuccess(WeiXin.CHANNEL_WEIXIN)
            BaseResp.ErrCode.ERR_USER_CANCEL -> shareListener?.onCancel()
            BaseResp.ErrCode.ERR_SENT_FAILED -> shareListener?.onFail(WeiXin.CHANNEL_WEIXIN, "")
        }
    }
}