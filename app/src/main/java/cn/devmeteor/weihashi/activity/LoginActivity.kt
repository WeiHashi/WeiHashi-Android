package cn.devmeteor.weihashi.activity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.base.BaseActivity
import cn.devmeteor.weihashi.channel.QQ
import cn.devmeteor.weihashi.channel.QQ.Companion.qq
import cn.devmeteor.weihashi.databinding.ActivityLoginBinding
import cn.devmeteor.weihashi.inflate
import cn.devmeteor.weihashi.utils.AppWidgetUtil
import cn.devmeteor.weihashi.utils.Util.kv
import cn.devmeteor.weihashi.viewmodel.UserViewModel
import cn.devmeteor.weihashi.widget.Dialog
import cn.devmeteor.weihashi.widget.SpannableLink
import com.blankj.utilcode.util.SpanUtils
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView

class LoginActivity : BaseActivity(), QQ.LoginListener {

    val binding: ActivityLoginBinding by inflate()
    private val userVM: UserViewModel by viewModels()

    private val privacyDialog: Dialog by lazy { Dialog(this, "微哈师使用须知") }

    override fun init() {
        QMUIStatusBarHelper.setStatusBarDarkMode(this)
        checkFirstOpen()
        AppWidgetUtil.sendBroadcast(this, Constants.ACTION_NEED_LOGIN)
        binding.loginBtn.setOnClickListener {
            showLoading("正在登录")
            qq.login(this, this)
        }
    }

    private fun checkFirstOpen() {
        if (!userVM.checkFirstOpen()) {
            return
        }
        val root = LayoutInflater.from(this).inflate(R.layout.view_dialog_privacy, null)
        val tv: QMUISpanTouchFixTextView = root.findViewById(R.id.privacy_tips)
        val msg = SpanUtils.with(tv)
                .append("\u3000\u3000欢迎您下载使用微哈师，请您在开始使用前务必充分阅读并理解")
                .append("《微哈师用户协议》")
                .setClickSpan(object : SpannableLink() {
                    override fun onSpanClick(widget: View?) {
                        go(WebActivity::class.java, bundleOf(
                                WebActivity.PARAM_URL to Constants.AGREEMENT_URL
                        ))
                    }
                })
                .append("和")
                .append("《微哈师隐私政策》")
                .setClickSpan(object : SpannableLink() {
                    override fun onSpanClick(widget: View?) {
                        go(WebActivity::class.java, bundleOf(
                                WebActivity.PARAM_URL to Constants.PRIVACY_URL
                        ))
                    }
                })
                .append("的各项条款来了解微哈师对您的个人信息的收集和使用规则以及您应当享有的权利和应当承担的责任。点击“同意”即表示您接受上述服务协议和隐私政策，点击“取消”将退出微哈师。")
                .create()
        tv.text = msg
        root.findViewById<View>(R.id.privacy_exit).setOnClickListener {
            finish()
        }
        root.findViewById<View>(R.id.privacy_ok).setOnClickListener {
            userVM.firstOpened()
            privacyDialog.dismiss()
        }
        privacyDialog.setContentView(root)
        privacyDialog.setCloseable(false)
        privacyDialog.show()
    }

    override fun onSuccess() {
        val initJob = userVM.tryInitData()
        if (initJob == null) {
            showFail("初始化数据出错，请重试")
            return
        }
        initJob.invokeOnCompletion {
            if (it == null) {
                if (!userVM.checkTest()) {
                    hideLoading()
                    QMUIDialog.MessageDialogBuilder(this)
                            .setMessage("您暂时没有测试权限")
                            .addAction("去申请") { _, _ ->
                                go(WebActivity::class.java, bundleOf(
                                        WebActivity.PARAM_URL to Constants.TEST_APPLY_URL,
                                        WebActivity.PARAM_PARAMS to "openid=${kv.decodeString(Constants.KEY_OPENID)}"
                                ))
                            }.addAction("取消") { dialog, _ ->
                                dialog?.dismiss()
                            }.create()
                            .show()
                    return@invokeOnCompletion
                }
                toastShort("登录成功")
                goAndFinish(MainActivity::class.java)
            } else
                showFail("初始化数据出错，请重试")
        }
    }

    override fun onFail(reason: String) {
        hideLoading()
        toastLong(reason)
    }

    override fun onCancel() {
        hideLoading()
        toastShort("登录取消")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == com.tencent.connect.common.Constants.REQUEST_LOGIN) {
            qq.callBack(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun onDestroy() {
        super.onDestroy()
        qq.removeCallbacks()
    }
}