package cn.devmeteor.weihashi.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.logd
import cn.devmeteor.weihashi.model.WeekStartDate
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

object Util {

    val kv = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, Constants.STORE_ENC_KEY)!!
    val gson = Gson()

    private val tsFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINA)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val yearFormat = SimpleDateFormat("yyyy", Locale.CHINA)
    private val dateShowFormat = SimpleDateFormat("yyyy年M月d日", Locale.CHINA)
    private val mdShowFormat = SimpleDateFormat("M月d日", Locale.CHINA)
    private val weekdayFormat = SimpleDateFormat("E", Locale.CHINA)

    val weekdays = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val parts = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14")
    private val startTemplate = arrayOf(
        "",
        "8:10",
        "9:05",
        "10:10",
        "11:05",
        "13:15",
        "14:10",
        "15:05",
        "16:00",
        "16:55",
        "17:50",
        "18:45",
        "19:40"
    )
    private val endTemplate = arrayOf(
        "",
        "8:55",
        "9:50",
        "10:55",
        "11:50",
        "14:00",
        "14:55",
        "15:50",
        "16:45",
        "17:40",
        "18:35",
        "19:30",
        "20:25"
    )

    fun getMessageSummary(json: String) = StringBuilder().apply {
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            if (obj.getInt("type") == 1) {
                append(" ${obj.getString("content").trim()}")
            }
        }
    }

    fun getWeekDay(date: Date): String = weekdayFormat.format(date).replace("周", "星期")

    fun getCurrentWeekStart(date: Date): Date {
        val th = weekdayFormat.format(date)
        for (i in weekdays.indices) {
            if (weekdays[i] == th) {
                return dateFormat.parse(tsFormat.format(date.time - i * Constants.DAY_LEN))!!
            }
        }
        return date
    }

    fun getWeekStart(
        week: Int,
        weekTotal: Int,
        termStart: String?
    ): Date {
        val defaultValue = getCurrentWeekStart(Date())
        termStart ?: return defaultValue
        val weekList = getWeekList(termStart, weekTotal) ?: return defaultValue
        logd(weekList.toString())
        weekList.forEach {
            if (it.week == week) {
                logd(it.start.toString())
                return it.start
            }
        }
        return defaultValue
    }

    fun getWeekList(termStart: String, weekTotal: Int): List<WeekStartDate>? {
        var startDate = tsFormat.parse(termStart) ?: return null
        val list = ArrayList<WeekStartDate>()
        for (i in 0 until weekTotal) {
            val end = Date(
                startDate.time +
                        6 * Constants.DAY_LEN +
                        23 * Constants.HOUR_LEN +
                        59 * Constants.MINUTE_LEN +
                        59 * 1000 + 999
            )
            list.add(WeekStartDate(i + 1, startDate))
            startDate = Date(end.time + 1)
        }
        return list
    }

    fun getWeekText(week: Int): String = if (week <= 0) "假期中" else "第${week}周"

    fun isToday(d: String): Boolean = isSameDay(tsFormat.parse(d)!!, Date())

    fun isSameDay(d1: Date, d2: Date) =
        dateFormat.format(d1) == dateFormat.format(d2)

    fun isBeforeDay(d: String, distance: Int) =
        isSameDay(tsFormat.parse(d)!!, Date(Date().time - distance * Constants.DAY_LEN))

    fun isSameYear(d1: Date, d2: Date) = yearFormat.format(d1) == yearFormat.format(d2)

    fun parseTimestamp(d: String): String {
        val timeStr = timeFormat.format(timestamp2Date(d))
        if (isToday(d))
            return timeStr
        if (isBeforeDay(d, 1))
            return "昨天 $timeStr"
        if (isBeforeDay(d, 2))
            return "前天 $timeStr"
        if (isSameYear(tsFormat.parse(d)!!, Date()))
            return mdShowFormat.format(timestamp2Date(d))
        return dateShowFormat.format(timestamp2Date(d))
    }

    fun date2Timestamp(date: Date): String = tsFormat.format(date)

    fun timestamp2Date(ts: String): Date = tsFormat.parse(ts)!!

    fun loadUrlWithBrowser(context: Context, url: String) {
        context.startActivity(Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(url)
        })
    }

    fun dateInRange(target: String, from: String, to: String): Boolean =
        dateInRange(timestamp2Date(target), timestamp2Date(from), timestamp2Date(to))


    fun dateInRange(target: Date, from: Date, to: Date): Boolean =
        target.time in from.time.rangeTo(to.time)

    fun getLessonTime(start: Int, end: Int): String = "${startTemplate[start]}-${endTemplate[end]}"

    fun getCurrentWeek(startValue: String, totalWeek: Int): Int {
        val start = timestamp2Date(startValue).time
        val now = Date().time
        val calcValue = ceil((now - start).toFloat() / Constants.WEEK_LEN).toInt()
        return if (calcValue > totalWeek) -1 else calcValue
    }

    fun getDateStrToday(): String = dateFormat.format(Date(System.currentTimeMillis()))

    fun dateStr2Time(dateStr: String): Long? = dateFormat.parse(dateStr)?.time

}