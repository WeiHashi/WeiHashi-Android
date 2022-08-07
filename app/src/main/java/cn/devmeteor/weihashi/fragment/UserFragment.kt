package cn.devmeteor.weihashi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import cn.devmeteor.weihashi.BuildConfig
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.activity.SettingsActivity
import cn.devmeteor.weihashi.activity.WebActivity
import cn.devmeteor.weihashi.api.ApiException
import cn.devmeteor.weihashi.base.BaseFragment
import cn.devmeteor.weihashi.channel.*
import cn.devmeteor.weihashi.channel.QQ.Companion.qq
import cn.devmeteor.weihashi.channel.Qzone.Companion.qzone
import cn.devmeteor.weihashi.channel.WeiXin.Companion.weiXin
import cn.devmeteor.weihashi.channel.Weibo.Companion.weibo
import cn.devmeteor.weihashi.channel.WxTimeline.Companion.wxTimeLine
import cn.devmeteor.weihashi.databinding.FragmentUserBinding
import cn.devmeteor.weihashi.databinding.ViewDialogBindJwBinding
import cn.devmeteor.weihashi.loge
import cn.devmeteor.weihashi.model.Update
import cn.devmeteor.weihashi.utils.GlideUtil
import cn.devmeteor.weihashi.utils.UpdateManager
import cn.devmeteor.weihashi.utils.UpdateManager.Companion.updateManager
import cn.devmeteor.weihashi.viewmodel.UserViewModel
import cn.devmeteor.weihashi.widget.Dialog
import cn.devmeteor.weihashi.widget.share.ShareBottomSheet
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import java.net.ConnectException
import java.net.SocketTimeoutException

class UserFragment : BaseFragment(R.layout.fragment_user),
    View.OnClickListener, ShareListener,
    QMUIBottomSheet.BottomGridSheetBuilder.OnSheetItemClickListener, UpdateManager.UpdateListener {

    private lateinit var binding: FragmentUserBinding
    private val bindJwDialog: Dialog by lazy { Dialog(requireActivity(), "绑定教务平台") }

    private val shareBottomSheet: ShareBottomSheet by lazy {
        ShareBottomSheet(requireContext(), this@UserFragment)
    }

    private val viewModel: UserViewModel by activityViewModels()

    companion object {
        @JvmStatic
        fun newInstance() = UserFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            binding = FragmentUserBinding.bind(this!!)
        }
    }

    private fun handleApiException(e: ApiException) {
        hideLoading()
        showFail(e.msg)
    }

    override fun handleOtherException(e: Exception) {
        hideLoading()
        if (e is SocketTimeoutException || e is ConnectException) {
            showFail("连接服务器失败")
            return
        }
        toastLong(e.toString())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) = with(binding) {
        super.onActivityCreated(savedInstanceState)
        viewModel.handleException(
            this@UserFragment,
            { handleOtherException(it) },
            { handleApiException(it) })
        userBindJw.setOnClickListener(this@UserFragment)
        userFeedback.setOnClickListener(this@UserFragment)
        userSettings.setOnClickListener(this@UserFragment)
        userShare.setOnClickListener(this@UserFragment)
        userUpdate.setOnClickListener(this@UserFragment)
        userAbout.setOnClickListener(this@UserFragment)
        initJwDialog()
        binding.model = viewModel
        binding.lifecycleOwner = this@UserFragment
        viewModel.updateQQInfo()
        GlideUtil.loadAvatar(requireContext(), viewModel.getAvatar(), binding.userAvatar)
    }

    private fun initJwDialog() {
        val jwView = LayoutInflater.from(context).inflate(R.layout.view_dialog_bind_jw, null)
        val jwBinding: ViewDialogBindJwBinding = DataBindingUtil.bind(jwView)!!
        jwBinding.vm = viewModel
        jwBinding.lifecycleOwner = this@UserFragment
        viewModel.updateJwInfo()
        bindJwDialog.setContentView(jwView)
        jwBinding.bindJw.setOnClickListener {
            showLoading("正在绑定")
            viewModel.bind(
                jwBinding.jwInputStudentId.text.toString(),
                jwBinding.jwInputPassword.text.toString()
            ) { onBindOrUnbind(it, jwBinding) }
        }
        jwBinding.unbindJw.setOnClickListener { viewModel.unbind { onBindOrUnbind(it, jwBinding) } }
    }

    private fun onBindOrUnbind(bound: Boolean, jwBinding: ViewDialogBindJwBinding) {
        hideLoading()
        showSuccess("已${if (bound) "绑定" else "解绑"}")
        bindJwDialog.dismiss()
        if (!bound) jwBinding.jwInputPassword.setText("")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.user_feedback -> go(WebActivity::class.java, Bundle().apply {
                putString(WebActivity.PARAM_URL, Constants.FEEDBACK_URL)
                putInt(WebActivity.PARAM_ACTION, WebActivity.ACTION_POST)
                putString(WebActivity.PARAM_PARAMS, viewModel.getFeedBackParams())
            })
            R.id.user_bind_jw -> bindJwDialog.show()
            R.id.user_settings -> go(SettingsActivity::class.java)
            R.id.user_share -> shareBottomSheet.show()
            R.id.user_update -> checkUpdate()
            R.id.user_about -> go(WebActivity::class.java, Bundle().apply {
                putString(WebActivity.PARAM_URL, Constants.ABOUT_URL)
            })
        }
    }

    private fun checkUpdate() {
        showLoading("正在检查更新")
        updateManager.checkUpdate(this)
    }

    fun showJwDialog() = bindJwDialog.show()

    override fun onSuccess(channel: String) {
        toastShort("$channel 分享成功")
    }

    override fun onFail(channel: String, reason: String) {
        loge("TAG", "onFail: $channel--->$reason")
    }

    override fun onCancel() {
        toastShort("分享取消")
    }

    override fun onClick(dialog: QMUIBottomSheet, itemView: View) {
        dialog.dismiss()
        when (itemView.tag as String) {
            QQ.CHANNEL_QQ -> share(qq)
            WeiXin.CHANNEL_WEIXIN -> share(weiXin)
            WxTimeline.CHANNEL_WX_TIMELINE -> share(wxTimeLine)
            Qzone.CHANNEL_QZONE -> share(qzone)
            Weibo.CHANNEL_WEIBO -> share(weibo)
        }
    }

    private fun share(channel: Channel) =
        channel.share(requireActivity(), Constants.SHARE_APP, this)

    override fun onNewVersion(update: Update, filePath: String) {
        runOnUiThread {
            hideLoading()
            showDialog(
                "提示",
                "当前版本：${BuildConfig.VERSION_NAME}\n最新版本：${update.newVersion}\n大小：${update.targetSize}\n更新内容：${update.updateLog}",
                { dialog, index ->
                    when (index) {
                        0 -> {
                            doUpdate()
                            if (!update.isConstraint) {
                                dialog?.dismiss()
                            }
                        }
                        1 -> dialog?.dismiss()
                    }
                },
                !update.isConstraint
            )
        }
    }

    override fun onNoUpdate() {
        runOnUiThread {
            hideLoading()
            toastLong("已更新到最新版本")
        }
    }

    override fun onFail() {
        hideLoading()
        runOnUiThread {
            hideLoading()
            toastLong("检查更新失败")
        }
    }


    private fun doUpdate() {
        updateManager.downloadPackage()
    }

    override fun onDestroy() {
        super.onDestroy()
        weibo.removeCallbacks()
    }

}