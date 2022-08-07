package cn.devmeteor.weihashi.activity

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.os.bundleOf
import cn.devmeteor.weihashi.*
import cn.devmeteor.weihashi.base.BaseActionBarActivity
import cn.devmeteor.weihashi.channel.QQ.Companion.qq
import cn.devmeteor.weihashi.databinding.ActivitySettingsBinding
import cn.devmeteor.weihashi.utils.GlideUtil
import cn.devmeteor.weihashi.utils.PoiManager
import cn.devmeteor.weihashi.utils.Util.kv
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet

class SettingsActivity : BaseActionBarActivity(), View.OnClickListener,
    QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener {

    val binding: ActivitySettingsBinding by inflate()
    private val handler = Handler(Looper.getMainLooper()) {
        when (it.what) {
            0 -> showSuccess("已清除")
        }
        return@Handler false
    }

    companion object {
        private const val CLEAR_OPTION_TIMETABLE = "清除课程数据"
        private const val CLEAR_OPTION_GRADE = "清除成绩数据"
    }

    private val clearOptions by lazy {
        QMUIBottomSheet.BottomListSheetBuilder(this)
            .addItem(CLEAR_OPTION_TIMETABLE)
            .addItem(CLEAR_OPTION_GRADE)
            .setGravityCenter(true)
            .setOnSheetItemClickListener(this)
            .build()
    }

    override fun init() {
        setSupportActionBar("设置", binding)
        binding.settingsClearCache.setOnClickListener(this)
        binding.settingsClearData.setOnClickListener(this)
        binding.settingsClearAll.setOnClickListener(this)
        binding.settingsTestFeedback.setOnClickListener(this)
        binding.settingsDebugX5.setOnClickListener(this)
        binding.settingsRefreshMapData.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.settings_clear_cache -> clearCache()
            R.id.settings_clear_data -> clearOptions.show()
            R.id.settings_clear_all -> clearAll()
            R.id.settings_test_feedback -> openConversation()
            R.id.settings_debug_x5 -> debugX5()
            R.id.settings_refresh_map_data -> refreshMapData()
        }
    }

    private fun refreshMapData() {
        showLoading("正在获取数据")
        PoiManager.refreshPoiTree { success, new ->
            hideLoading()
            when {
                new -> {
                    showSuccess("更新完成")
                    binding.settingsMapDataLastRefresh.text = "上次更新：${PoiManager.getLastRefreshTime()}"
                }
                success -> {
                    showSuccess("已是最新")
                }
                else -> {
                    showFail("更新失败")
                }
            }

        }
    }

    private fun debugX5() {
        go(WebActivity::class.java, bundleOf(WebActivity.PARAM_URL to Constants.X5_DEBUG_URL))
    }

    private fun openConversation() {
        qq.openConversation(this, "2633979287")?.apply {
            showFail(this)
        }
    }

    private fun clearAll() {
        showDialog("", "确认清除？", { dialog, index ->
            if (index == 0) {
                kv.remove(Constants.KEY_LESSON_DATA, Constants.KEY_GRADE)
                clearCache()
            }
            dialog?.dismiss()
        })
    }

    private fun clearCache() {
        GlideUtil.clearCache(this, handler)
    }

    override fun onClick(sheet: QMUIBottomSheet?, itemView: View?, position: Int, tag: String) {
        sheet?.dismiss()
        showDialog("", "确认清除？", { dialog, index ->
            if (index == 0) performClear(tag)
            dialog?.dismiss()
        })
    }

    private fun performClear(tag: String) {
        if (tag == CLEAR_OPTION_TIMETABLE) Constants.KEY_LESSON_DATA.remove()
        if (tag == CLEAR_OPTION_GRADE) Constants.KEY_GRADE.remove()
        handler.sendEmptyMessage(0)
    }
}