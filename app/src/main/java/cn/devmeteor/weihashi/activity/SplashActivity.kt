package cn.devmeteor.weihashi.activity

import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.api.ApiClient.configApi
import cn.devmeteor.weihashi.base.BaseActivity
import cn.devmeteor.weihashi.channel.QQ
import cn.devmeteor.weihashi.channel.QQ.Companion.qq
import cn.devmeteor.weihashi.databinding.ActivitySplashBinding
import cn.devmeteor.weihashi.inflate
import cn.devmeteor.weihashi.logd
import cn.devmeteor.weihashi.loge
import cn.devmeteor.weihashi.utils.Util.kv
import cn.devmeteor.weihashi.viewmodel.UserViewModel
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : BaseActivity(), QQ.LoginListener {

    private val binding: ActivitySplashBinding by inflate()
    private val userVM: UserViewModel by viewModels()

    override fun init() {
        binding
        configApi.getGuestData().enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                logd(response.code().toString())
                if (response.code() != 200 && kv.containsKey(Constants.KEY_GUEST)) {
                    loge("游客入口关闭", response.message())
                    kv.clearAll()
                    checkUserState()
                    return
                }
                if (kv.containsKey(Constants.KEY_GUEST)) {
                    logd("已有游客数据", response.code().toString())
                    checkUserState()
                    return
                }
                val map = response.body()
                if (map!=null){
                    logd("获取到游客数据", map.toString())
                    for (entry in map.entries) {
                        kv.encode(entry.key, entry.value)
                    }
                }
                checkUserState()
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                showDialog("网络不可用", "请检查网络连接或联网控制，待网络正常后重启APP", { _, _ ->
                    finish()
                }, false)
            }
        })
    }

    private fun checkUserState() {
        val initJob = userVM.tryInitData()
        QMUIStatusBarHelper.setStatusBarDarkMode(this)
        Handler(Looper.getMainLooper()).postDelayed({
            if (initJob != null && initJob.isActive) {
                initJob.cancel()
            }
            if (!kv.containsKey(Constants.KEY_OPENID)) {
                goAndFinish(LoginActivity::class.java)
            } else if (kv.containsKey(Constants.KEY_GUEST)) {
                logd("进入游客模式")
                goAndFinish(MainActivity::class.java)
            } else {
                qq.checkLogin(this)
            }
        }, 3000)
    }

    override fun onBackPressed() {}
    override fun onSuccess() {
        if (!userVM.checkTest()) {
            QMUIDialog.MessageDialogBuilder(this)
                .setMessage("您暂时没有测试权限")
                .addAction("去申请") { _, _ ->
                    go(
                        WebActivity::class.java, bundleOf(
                            WebActivity.PARAM_URL to Constants.TEST_APPLY_URL,
                            WebActivity.PARAM_PARAMS to "openid=${kv.decodeString(Constants.KEY_OPENID)}"
                        )
                    )
                }.addAction("取消") { dialog, _ ->
                    dialog?.dismiss()
                }.create()
                .show()
            return
        }
        goAndFinish(MainActivity::class.java)
    }

    override fun onFail(reason: String) {
        toastLong(reason)
        goAndFinish(LoginActivity::class.java)
    }

    override fun onCancel() {
        goAndFinish(LoginActivity::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        qq.removeCallbacks()
    }

}