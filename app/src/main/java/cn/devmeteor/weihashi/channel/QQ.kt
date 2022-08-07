package cn.devmeteor.weihashi.channel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import cn.devmeteor.weihashi.*
import cn.devmeteor.weihashi.utils.Util.kv
import com.tencent.connect.UserInfo
import com.tencent.connect.share.QQShare
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import org.json.JSONObject
import com.tencent.connect.common.Constants as QQConstants

class QQ private constructor() : Channel {

    companion object {
        const val CHANNEL_QQ = "QQ"
        const val PARAM_NICKNAME = "nickname"
        const val PARAM_FIGURE_URL = "figureurl_qq_2"
        const val ERR_NULL = -101
        val qq by lazy { QQ() }
    }

    private lateinit var mTencent: Tencent
    private lateinit var appContext: Context
    private var shareListener: ShareListener? = null
    private var loginListener: LoginListener? = null
    private var loginCheckListener: LoginListener? = null

    fun init(context: Context) {
        appContext = context
        mTencent = Tencent.createInstance(
            Constants.QQ_APP_ID,
            context,
            Constants.FILE_PROVIDER_AUTHORITIES
        )
    }

    override fun share(
        activity: Activity,
        shareContent: ShareContent,
        shareListener: ShareListener
    ) {
        this.shareListener = shareListener
        mTencent.shareToQQ(activity, Bundle().apply {
            putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
            putString(QQShare.SHARE_TO_QQ_TITLE, shareContent.title)
            putString(QQShare.SHARE_TO_QQ_SUMMARY, shareContent.summary)
            putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareContent.url)
            putString(QQShare.SHARE_TO_QQ_IMAGE_URL, shareContent.thumb)
            putString(QQShare.SHARE_TO_QQ_APP_NAME, activity.resources.getString(R.string.app_name))
            putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE)
        }, shareResult)
    }

    private val shareResult = object : BaseUiListener() {

        override fun onSuccess(obj: JSONObject) {
            shareListener?.onSuccess(CHANNEL_QQ)
        }

        override fun onFail(code: Int, reason: String) {
            shareListener?.onFail(CHANNEL_QQ, reason)
        }

        override fun onCancel() {
            shareListener?.onCancel()
        }

    }

    fun login(activity: Activity, listener: LoginListener) {
        this.loginListener = listener
        mTencent.login(activity, "all", loginResult)
    }

    private val loginResult = object : BaseUiListener() {

        override fun onSuccess(obj: JSONObject) {
            kv.encode(
                Constants.KEY_OPENID,
                obj.getString(QQConstants.PARAM_OPEN_ID).apply { mTencent.openId = this }
            )
            val token = obj.getString(QQConstants.PARAM_ACCESS_TOKEN)
            val expiresIn = obj.getString(QQConstants.PARAM_EXPIRES_IN)
            kv.encode(Constants.KEY_ACCESS_TOKEN, token)
            kv.encode(Constants.KEY_EXPIRES_IN, expiresIn)
            mTencent.setAccessToken(token, expiresIn)
            updateUserInfo()
        }

        override fun onFail(code: Int, reason: String) {
            loginListener?.onFail(reason)
        }

        override fun onCancel() {
            loginListener?.onCancel()
        }
    }

    private fun updateUserInfo() {
        UserInfo(appContext, mTencent.qqToken).getUserInfo(userInfoResult)
    }

    private val userInfoResult = object : BaseUiListener() {
        override fun onSuccess(obj: JSONObject) {
            try {
                kv.apply {
                    encode(Constants.KEY_NICKNAME, obj.getString(PARAM_NICKNAME))
                    encode(Constants.KEY_AVATAR, obj.getString(PARAM_FIGURE_URL).httpsScheme())
                }
                loginListener?.onSuccess()
            } catch (e: Exception) {
                loginListener?.onFail("获取用户信息失败")
            }
        }

        override fun onFail(code: Int, reason: String) {
            loginListener?.onFail(reason)
        }

        override fun onCancel() {}
    }

    fun checkLogin(listener: LoginListener) {
        this.loginCheckListener = listener
        mTencent.openId = kv.decodeString(Constants.KEY_OPENID)
        mTencent.setAccessToken(
            kv.decodeString(Constants.KEY_ACCESS_TOKEN),
            kv.decodeString(Constants.KEY_EXPIRES_IN)
        )
        mTencent.checkLogin(checkLoginResult)
    }

    private val checkLoginResult = object : BaseUiListener() {
        override fun onSuccess(obj: JSONObject) {
            loginCheckListener?.onSuccess()
        }

        override fun onFail(code: Int, reason: String) {
            if (code < 0) {
                loginCheckListener?.onSuccess()
                return
            }
            loginCheckListener?.onFail(reason)
        }

        override fun onCancel() {
            loginCheckListener?.onCancel()
        }
    }

    interface LoginListener {
        fun onSuccess()
        fun onFail(reason: String)
        fun onCancel()
    }

    abstract class BaseUiListener : IUiListener {
        override fun onComplete(p0: Any?) {
            if (p0 == null) {
                onFail(ERR_NULL, "结果为空")
                return
            }
            onSuccess(p0 as JSONObject)
        }

        abstract fun onSuccess(obj: JSONObject)


        override fun onError(p0: UiError) {
            onFail(p0.errorCode, p0.errorMessage)
        }

        abstract fun onFail(code: Int, reason: String)


        override fun onWarning(p0: Int) {
            logd(CHANNEL_QQ, "onWarning: $p0")
        }

    }

//    fun openConversation(activity: Activity, qqNum: String): String? =
//        when (mTencent.startIMAio(activity, qqNum, BuildConfig.APPLICATION_ID)) {
//            -1 -> "QQ号为空"
//            -2 -> "QQ版本过低，请升级到最新版"
//            -3 -> "QQ号过短"
//            -4 -> "QQ号非法"
//            -5 -> "未知错误"
//            else -> null
//        }

    fun openConversation(activity: Activity, qqNum: String): String? = try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=${qqNum}")
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
        null
    } catch (e: Exception) {
        "打开会话失败"
    }

    fun callBack(requestCode: Int, resultCode: Int, data: Intent?) {
        var listener: IUiListener? = when (requestCode) {
            QQConstants.REQUEST_LOGIN -> loginResult
            QQConstants.REQUEST_QQ_SHARE -> shareResult
            else -> null
        }
        listener?.let { Tencent.onActivityResultData(requestCode, resultCode, data, listener) }
        listener = null
    }

    fun getTencent(): Tencent = mTencent

    override fun getName(): String = CHANNEL_QQ

    fun removeCallbacks() {
        shareListener = null
        loginListener = null
        loginCheckListener = null
    }
}