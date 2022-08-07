package cn.devmeteor.weihashi

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import cn.devmeteor.weihashi.channel.QQ.Companion.qq
import cn.devmeteor.weihashi.channel.WeiXin.Companion.weiXin
import cn.devmeteor.weihashi.utils.PoiManager
import cn.devmeteor.weihashi.utils.TestManager
import cn.devmeteor.weihashi.utils.UpdateManager.Companion.updateManager
import cn.devmeteor.weihashi.utils.Util
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.blankj.utilcode.util.LogUtils
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk


class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        val map = HashMap<String, Any>()
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)
        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.BD09LL)
        MMKV.initialize(this)
        qq.init(this)
        weiXin.init(this)
        updateManager.init(this)
        LogUtils.getConfig().isLogSwitch = BuildConfig.DEBUG
        if (!BuildConfig.DEBUG) {
            CrashReport.initCrashReport(this, Constants.BUGLY_APP_ID, false)
        }
        TestManager.init()
        checkShouldUpdatePoiTree()
    }

    private fun checkShouldUpdatePoiTree() {
        val lastRefreshDateStr = PoiManager.getLastRefreshTime()
        val lastRefreshDateTime = Util.dateStr2Time(lastRefreshDateStr) ?: 0
        val now = System.currentTimeMillis()
        if (now - lastRefreshDateTime > Constants.WEEK_LEN) {
            PoiManager.refreshPoiTree { _, _ -> logd("触发poi数据更新") }
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}