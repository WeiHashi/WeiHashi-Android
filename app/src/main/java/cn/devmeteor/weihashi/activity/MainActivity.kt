package cn.devmeteor.weihashi.activity

import android.Manifest
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.viewpager.widget.ViewPager
import cn.devmeteor.weihashi.*
import cn.devmeteor.weihashi.adapter.FragmentAdapter
import cn.devmeteor.weihashi.api.ApiClient.configApi
import cn.devmeteor.weihashi.base.BaseActivity
import cn.devmeteor.weihashi.channel.QQ.Companion.qq
import cn.devmeteor.weihashi.channel.Qzone.Companion.qzone
import cn.devmeteor.weihashi.channel.Weibo.Companion.weibo
import cn.devmeteor.weihashi.databinding.ActivityMainBinding
import cn.devmeteor.weihashi.fragment.HomeFragment
import cn.devmeteor.weihashi.fragment.MessageFragment
import cn.devmeteor.weihashi.fragment.UserFragment
import cn.devmeteor.weihashi.model.Update
import cn.devmeteor.weihashi.utils.UpdateManager
import cn.devmeteor.weihashi.utils.UpdateManager.Companion.updateManager
import cn.devmeteor.weihashi.utils.Util
import cn.devmeteor.weihashi.viewmodel.UserViewModel
import cn.devmeteor.weihashi.widget.Dialog
import cn.devmeteor.weihashi.widget.SpannableLink
import com.blankj.utilcode.util.SpanUtils
import com.permissionx.guolindev.PermissionX
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : BaseActivity(), ViewPager.OnPageChangeListener, UpdateManager.UpdateListener,
    Callback<Map<String, String>> {

    companion object {
        const val EXTRA_SPECIFIC_ITEM = "specific_item"
    }

    private val userVM: UserViewModel by viewModels()
    private val binding: ActivityMainBinding by inflate()
    private val userFragment = UserFragment.newInstance()

    private val privacyDialog: Dialog by lazy { Dialog(this, "微哈师使用须知") }

    private val maintenanceActionListener: QMUIDialogAction.ActionListener =
        QMUIDialogAction.ActionListener { _, _ ->
            finish()
        }

    override fun init() {
        weibo.init(this)
        binding.mainTab.attachViewPager(binding.mainPager)
        val fragmentAdapter = FragmentAdapter(
            supportFragmentManager, listOf(
                HomeFragment.newInstance(),
                MessageFragment.newInstance(),
                userFragment
            )
        )
        binding.mainPager.apply {
            offscreenPageLimit = 4
            adapter = fragmentAdapter
            addOnPageChangeListener(this@MainActivity)
        }
        checkFirstOpen()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val no = intent?.getIntExtra(EXTRA_SPECIFIC_ITEM, -1) ?: -1
        if (no != -1) {
            binding.mainPager.setCurrentItem(no, false)
            userFragment.showJwDialog()
        }
    }

    override fun onBackPressed() {
        Intent(Intent.ACTION_MAIN).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            addCategory(Intent.CATEGORY_HOME)
            startActivity(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        qq.callBack(requestCode, resultCode, data)
        qzone.callback(requestCode, resultCode, data)
        weibo.callback(data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int
    ) {
    }

    override fun onPageSelected(position: Int) {
        binding.mainTab.switchTo(position)
        if (position == 2) {
            QMUIStatusBarHelper.setStatusBarDarkMode(this)
        } else {
            QMUIStatusBarHelper.setStatusBarLightMode(this)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onNewVersion(update: Update, filePath: String) {
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

    override fun onNoUpdate() {

    }

    override fun onFail() {

    }

    private fun doUpdate() {
        updateManager.downloadPackage()
    }

    override fun onResponse(
        call: Call<Map<String, String>>,
        response: Response<Map<String, String>>
    ) {
        val resp = response.body() ?: return
        val start = resp[Constants.KEY_MAINTENANCE_START]!!.save(Constants.KEY_MAINTENANCE_START)
        val end = resp[Constants.KEY_MAINTENANCE_END]!!.save(Constants.KEY_MAINTENANCE_END)
        if (Util.dateInRange(Util.date2Timestamp(Date()), start, end)) {
            showDialog("维护中", "服务器正在维护中，维护时间为：$start - $end", maintenanceActionListener, false)
            return
        }
        updateManager.checkUpdate(this)
    }

    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {

    }

    private fun checkFirstOpen() {
        if (!userVM.checkFirstOpen()) {
            requestPermission()
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
                    )
                    )
                }
            })
            .append("和")
            .append("《微哈师隐私政策》")
            .setClickSpan(object : SpannableLink() {
                override fun onSpanClick(widget: View?) {
                    go(WebActivity::class.java, bundleOf(
                        WebActivity.PARAM_URL to Constants.PRIVACY_URL
                    )
                    )
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
            requestPermission()
        }
        privacyDialog.setContentView(root)
        privacyDialog.setCloseable(false)
        privacyDialog.show()
    }

    private fun requestPermission(){
        PermissionX.init(this)
            .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "该权限为APP必需权限，请允许",
                    "授权"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "请前往设置页面开启权限",
                    "确定"
                )
            }
            .request { _, _, _ -> configApi.checkMaintenance().enqueue(this) }
    }

}