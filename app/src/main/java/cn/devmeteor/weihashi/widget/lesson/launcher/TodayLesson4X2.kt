package cn.devmeteor.weihashi.widget.lesson.launcher

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.activity.SplashActivity
import cn.devmeteor.weihashi.activity.TimetableActivity
import cn.devmeteor.weihashi.logd
import cn.devmeteor.weihashi.model.Lesson
import cn.devmeteor.weihashi.model.LessonList
import cn.devmeteor.weihashi.utils.AppWidgetUtil
import cn.devmeteor.weihashi.utils.Util
import cn.devmeteor.weihashi.utils.Util.gson
import cn.devmeteor.weihashi.utils.Util.kv
import java.util.*

class TodayLesson4X2 : AppWidgetProvider() {

    companion object {
        private const val MSG_NEED_LOGIN = "未登录，请启动App登录"
        private const val MSG_NO_LESSON = "今天没有课，可以好好休息一下"
        private const val MSG_DATA_ERROR = "数据错误，点击启动App刷新"
        private var todayList: ArrayList<Lesson>? = null
        private var currentOffset = 0
    }


    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        logd("onUpdate")
        if (kv.containsKey(Constants.KEY_OPENID)) {
            refreshData(context)
            return
        }
        showTips(context, MSG_NEED_LOGIN)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        logd("onEnabled")
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        logd("onDisabled")
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        logd(action)
        when (action) {
            Constants.ACTION_LESSON_DATA_CHANGED -> refreshData(context)
            Constants.ACTION_NEED_LOGIN -> showTips(context, MSG_NEED_LOGIN)
            Constants.ACTION_LAST_PAGE -> switchLastPage(context)
            Constants.ACTION_NEXT_PAGE -> switchNextPage(context)
        }
    }

    private fun switchNextPage(context: Context) {
        logd("nextPage")
        showLessons(context, 1)
    }

    private fun switchLastPage(context: Context) {
        logd("lastPage")
        showLessons(context, -1)
    }


    private fun showTips(context: Context, msg: String) {
        logd("showTips:$msg")
        val views = RemoteViews(context.packageName, R.layout.widget_today_lesson_4_2)
        views.setViewVisibility(R.id.widget_today_lessons, View.GONE)
        views.setTextViewText(R.id.widget_today_tips, msg)
        views.setViewVisibility(R.id.widget_today_tips, View.VISIBLE)
        views.setViewVisibility(R.id.widget_index_lesson_toolbar, View.GONE)
        views.setOnClickPendingIntent(
            R.id.widget_today_tips,
            AppWidgetUtil.getActivityIntent(context, SplashActivity::class.java)
        )
        AppWidgetUtil.updateWidget(context, TodayLesson4X2::class.java, views)
    }

    //有课、没课、未获取
    private fun refreshData(context: Context) {
        val lessonData = kv.decodeString(Constants.KEY_LESSON_DATA)
        val termStartValue = kv.decodeString(Constants.KEY_TERM_START)
        val term = kv.decodeString(Constants.KEY_TERM)
        val online = kv.containsKey(Constants.KEY_OPENID)
        if (online && (termStartValue == null || term == null)) {
            showTips(context, MSG_DATA_ERROR)
            return
        }
        if (lessonData == null) {
            showTips(context, MSG_NO_LESSON)
            return
        }
        val week = Util.getCurrentWeek(termStartValue!!, 18)
        val lessonList = gson.fromJson(lessonData, LessonList::class.java).lessons
        val today = ArrayList<Lesson>()
        for (serverLesson in lessonList) {
            for (part in serverLesson.parts) {
                for (w in part.weeks) {
                    if (w == week && part.weekday == Util.getWeekDay(Date()))
                        today.add(
                            Lesson(
                                term!!,
                                serverLesson.name,
                                part.weekday,
                                part.start,
                                part.end,
                                part.place,
                                serverLesson.teacher,
                                serverLesson.bg!!
                            )
                        )
                }
            }
        }
        if (today.isEmpty()) {
            todayList = null
            showTips(context, MSG_NO_LESSON)
            return
        }
        todayList = today
        //显示数据
        logd(todayList.toString())
        currentOffset = 0
        showLessons(context, 0)
    }

    private fun showLessons(context: Context, offsetIncrement: Int) {
        if (todayList == null) {
            if (!kv.containsKey(Constants.KEY_OPENID)) {
                showTips(context, MSG_NEED_LOGIN)
            }
            return
        }
        val totalOffset = (todayList!!.size + 1) / 2
        val newOffset = currentOffset + offsetIncrement
        logd(newOffset.toString())
        val views = RemoteViews(context.packageName, R.layout.widget_today_lesson_4_2)
        val rightOutRange = rightOutRange(newOffset, totalOffset, views)
        val leftOutRange = leftOutRange(newOffset, views)
        if (leftOutRange || rightOutRange) {
            return
        }
        currentOffset = newOffset
        val start = 2 * newOffset
        views.removeAllViews(R.id.widget_today_lessons)
        for (i in 0 until 2) {
            val index = start + i
            val item = if (index >= todayList!!.size) null else todayList!![index]
            views.addView(
                R.id.widget_today_lessons,
                RemoteViews(context.packageName, R.layout.widget_item_index_lesson).apply {
                    setInt(
                        R.id.widget_index_lesson_container,
                        "setBackgroundColor",
                        if (item == null) Color.WHITE else getBgColor(i, 2)
                    )
                    if (item != null) {
                        setTextViewTextSize(R.id.widget_index_lesson_name, TypedValue.COMPLEX_UNIT_SP, 12F)
                        setTextViewText(R.id.widget_index_lesson_name, item.name)
                        setTextViewText(R.id.widget_index_lesson_time, Util.getLessonTime(item.start, item.end))
                        setTextViewText(R.id.widget_index_lesson_place, item.place)
                    }
                })
        }
        views.setTextViewText(R.id.widget_index_lesson_page_indicator, "${currentOffset + 1}/$totalOffset")
        views.setViewVisibility(R.id.widget_today_lessons, View.VISIBLE)
        views.setViewVisibility(R.id.widget_index_lesson_toolbar, View.VISIBLE)
        views.setOnClickPendingIntent(
            R.id.widget_today_lessons,
            AppWidgetUtil.getActivityIntent(context, TimetableActivity::class.java)
        )
        views.setViewVisibility(R.id.widget_today_tips, View.GONE)
        views.setOnClickPendingIntent(
            R.id.widget_index_lesson_page_before,
            AppWidgetUtil.getBroadcastIntent(context, Constants.ACTION_LAST_PAGE)
        )
        views.setOnClickPendingIntent(
            R.id.widget_index_lesson_page_next,
            AppWidgetUtil.getBroadcastIntent(context, Constants.ACTION_NEXT_PAGE)
        )
        AppWidgetUtil.updateWidget(context, TodayLesson4X2::class.java, views)
    }

    private fun leftOutRange(newOffset: Int, views: RemoteViews): Boolean {
        logd("lastOut", (newOffset - 1 < 0).toString())
        views.setImageViewResource(
            R.id.widget_index_lesson_page_before,
            if (newOffset - 1 < 0) R.drawable.page_before_disabled else R.drawable.page_before_enabled
        )
        return newOffset < 0
    }

    private fun rightOutRange(newOffset: Int, totalOffset: Int, views: RemoteViews): Boolean {
        logd("nextOut", (newOffset + 1 >= totalOffset).toString())
        views.setImageViewResource(
            R.id.widget_index_lesson_page_next,
            if (newOffset + 1 >= totalOffset) R.drawable.page_next_disabled else R.drawable.page_next_enabled
        )
        return newOffset >= totalOffset
    }

    private fun getBgColor(index: Int, size: Int): Int = if (index % size == 0) {
        Color.parseColor("#f2f2f2")
    } else {
        Color.WHITE
    }
}