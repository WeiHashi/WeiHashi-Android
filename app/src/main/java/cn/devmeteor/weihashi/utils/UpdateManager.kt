package cn.devmeteor.weihashi.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.widget.Toast
import cn.devmeteor.weihashi.BuildConfig
import cn.devmeteor.weihashi.api.ApiClient.configApi
import cn.devmeteor.weihashi.logd
import cn.devmeteor.weihashi.model.Update
import cn.devmeteor.weihashi.upperCase
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.tencent.bugly.crashreport.CrashReport
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UpdateManager private constructor() : Callback<Update> {

    private val filePath: String = "${PathUtils.getExternalDownloadsPath()}/微哈师.apk"
    private lateinit var context: Context
    private var listener: UpdateListener? = null
    private lateinit var downloadManager: DownloadManager
    private lateinit var update: Update

    fun init(context: Context) {
        this.context = context
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    interface UpdateListener {
        fun onNewVersion(update: Update, filePath: String)
        fun onNoUpdate()
        fun onFail()
    }

    fun checkUpdate(updateListener: UpdateListener) {
        listener = updateListener
        configApi.checkUpdate(BuildConfig.VERSION_NAME).enqueue(this)
    }

    companion object {
        val updateManager by lazy { UpdateManager() }
    }


    override fun onFailure(call: Call<Update>, t: Throwable) {
        CrashReport.postCatchedException(t)
        listener?.onFail()
    }

    override fun onResponse(call: Call<Update>, response: Response<Update>) {
        val resp = response.body()!!
        if (!resp.update) {
            listener?.onNoUpdate()
            return
        }
        logd(filePath)
        update = resp
        listener?.onNewVersion(resp, filePath)
    }

    fun downloadPackage() {
        if (checkExistedPackage()) {
            logd("开始安装")
            AppUtils.installApp(filePath)
            return
        }
        downloadManager.enqueue(DownloadManager.Request(Uri.parse(update.apkFileUrl)).apply {
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "微哈师.apk")
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            setTitle("微哈师")
            setDescription("微哈师更新")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        })
        Toast.makeText(context, "下载开始,请关注通知栏下载进度", Toast.LENGTH_LONG).show()
    }

    private fun checkExistedPackage(): Boolean {
        val file = File(filePath)
        val valid = file.exists()
                && TextUtils.equals(FileUtils.getFileMD5ToString(file).upperCase(), update.newMd5.upperCase())
        logd(valid.toString())
        if (!valid && file.exists()) {
            file.delete()
        }
        return valid
    }
}