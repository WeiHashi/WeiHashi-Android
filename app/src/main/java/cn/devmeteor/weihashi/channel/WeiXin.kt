package cn.devmeteor.weihashi.channel

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.logd
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.io.ByteArrayOutputStream

class WeiXin private constructor(): Channel, IWXAPIEventHandler {

    companion object {
        const val CHANNEL_WEIXIN = "WeiXin"
        val weiXin by lazy { WeiXin() }
    }

    private lateinit var api: IWXAPI
    private var shareListener: ShareListener? = null

    fun init(context: Context) {
        api = WXAPIFactory.createWXAPI(context, Constants.WX_APP_ID, true)
        api.registerApp(Constants.WX_APP_ID)
        context.registerReceiver(Receiver(api), IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP))
    }

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
            scene = SendMessageToWX.Req.WXSceneSession
        }
        api.sendReq(req)
    }

    override fun getName(): String = CHANNEL_WEIXIN

    fun handleIntent(intent: Intent) {
        api.handleIntent(intent, this)
    }

    private class Receiver(private val api: IWXAPI) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            api.registerApp(Constants.WX_APP_ID)
        }
    }

    override fun onReq(req: BaseReq) {
    }

    override fun onResp(resp: BaseResp) {
        logd("onResp: ${resp.errCode} ${resp.errStr}")
        when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK -> shareListener?.onSuccess(CHANNEL_WEIXIN)
            BaseResp.ErrCode.ERR_USER_CANCEL -> shareListener?.onCancel()
            BaseResp.ErrCode.ERR_SENT_FAILED -> shareListener?.onFail(CHANNEL_WEIXIN, "")
        }
    }

    fun getApi() = api

}