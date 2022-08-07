package cn.devmeteor.weihashi.viewmodel

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.api.Repository
import cn.devmeteor.weihashi.base.BaseViewModel
import cn.devmeteor.weihashi.delete
import cn.devmeteor.weihashi.logd
import cn.devmeteor.weihashi.model.*
import cn.devmeteor.weihashi.save
import cn.devmeteor.weihashi.utils.Util
import cn.devmeteor.weihashi.utils.Util.gson
import cn.devmeteor.weihashi.utils.Util.kv
import kotlinx.coroutines.Job
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class TimetableViewModel : BaseViewModel() {

    private val repo = Repository()
    private val selectedWeek = MutableLiveData(1)
    private val currentWeek = MutableLiveData(1)
    private val totalWeek = MutableLiveData(18)
    private val term = MutableLiveData("2020-2021-2")
    private val lessons = MutableLiveData(ArrayList<ServerLesson>())
    private val shownLessons = MutableLiveData(ArrayList<Lesson>())
    private val indexLessons = MutableLiveData(ArrayList<Lesson>())
    private val bgMap = MutableLiveData(HashMap<String, Int>())
    private val detailLesson = MutableLiveData(Lesson.emptyLesson)
    private val detailWeeks = MutableLiveData("")
    private val addLessonName = MutableLiveData("")
    private val addLessonWeekday = MutableLiveData("周一")
    private val addLessonStart = MutableLiveData(1)
    private val addLessonEnd = MutableLiveData(1)
    private val addLessonWeeks = MutableLiveData(ArrayList<Int>())
    private val addLessonTeacher = MutableLiveData("")
    private val addLessonPlace = MutableLiveData("")

    fun getLessons() = lessons
    fun getTerm() = term
    fun getSelectedWeek() = selectedWeek
    fun getCurrentWeek() = currentWeek
    fun getShownLessons() = shownLessons
    fun getTotalWeek() = totalWeek
    fun getBgMap() = bgMap
    fun getDetailLesson() = detailLesson
    fun getDetailWeeks() = detailWeeks
    fun getIndexLessons() = indexLessons
    fun getAddLessonName() = addLessonName
    fun getAddLessonWeekday() = addLessonWeekday
    fun getAddLessonStart() = addLessonStart
    fun getAddLessonEnd() = addLessonEnd
    fun getAddLessonTeacher() = addLessonTeacher
    fun getAddLessonPlace() = addLessonPlace

    fun initLessons() {
        val lessonString = kv.decodeString(Constants.KEY_LESSON_DATA)
        if (lessonString != null) {
            val lessonList = gson.fromJson(lessonString, LessonList::class.java)
            totalWeek.value = lessonList.weekTotal
            lessons.value = lessonList.lessons
        }
    }

    fun updateLessons(): Job = handleResult {
        var cookies = kv.decodeString(Constants.KEY_COOKIES)
        val openid = kv.decodeString(Constants.KEY_OPENID)
        val studentId = kv.decodeString(Constants.KEY_STUDENT_ID)
        val password = kv.decodeString(Constants.KEY_PASSWORD)
        if (cookies == null) {
            cookies = repo.jwLogin(
                openid!!,
                studentId!!,
                password!!
            ).data.cookies.save(Constants.KEY_COOKIES)
        }
        val res = repo.getDailyLesson(cookies)
        if (ResultCode.fromCode(res.code) == ResultCode.NEED_LOGIN) {
            repo.jwLogin(openid!!, studentId!!, password!!).data.cookies.save(Constants.KEY_COOKIES)
            updateLessons()
            return@handleResult
        }
        gson.toJson(res.data).save(Constants.KEY_LESSON_DATA)
        totalWeek.value = res.data.weekTotal
        lessons.value = res.data.lessons
    }

    fun updateDetailWeeks() {
        var list = ArrayList<Int>()
        for (serverLesson in lessons.value!!) {
            for (part in serverLesson.parts) {
                if (serverLesson.name == detailLesson.value!!.name
                    && serverLesson.teacher == detailLesson.value!!.teacher
                    && part.start == detailLesson.value!!.start
                    && part.end == detailLesson.value!!.end
                ) {
                    list = part.weeks
                }
            }
        }
        detailWeeks.value = list.toString().delete("[", "]")
    }

    fun initWeek() {
        val termStartValue = kv.decodeString(Constants.KEY_TERM_START) ?: return
        val weekValue = Util.getCurrentWeek(termStartValue,totalWeek.value!!)
        logd(weekValue.toString())
        selectedWeek.value = weekValue
        currentWeek.value = weekValue
    }

    fun updateShownLessons() {
        val list = ArrayList<Lesson>()
        val map = HashMap<String, Int>()
        for (serverLesson in lessons.value!!) {
            map[serverLesson.name] = Color.parseColor(serverLesson.bg)
            for (part in serverLesson.parts) {
                for (w in part.weeks) {
                    if (w == selectedWeek.value)
                        list.add(
                            Lesson(
                                term.value!!,
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
        bgMap.value = map
        shownLessons.value = list
    }

    fun updateIndexLessons() {
        indexLessons.value?.clear()
        for (serverLesson in lessons.value!!) {
            for (part in serverLesson.parts) {
                for (w in part.weeks) {
                    if (w == selectedWeek.value && part.weekday == Util.getWeekDay(Date())) {
                        indexLessons.value?.add(
                            Lesson(
                                term.value!!,
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
        }
    }

    fun deleteThis() {
        val dl = detailLesson.value!!
        for (serverLesson in lessons.value!!) {
            for (part in serverLesson.parts) {
                if (serverLesson.name == dl.name
                    && serverLesson.teacher == dl.teacher
                    && part.weekday == dl.weekday
                    && part.start == dl.start
                    && part.end == dl.end
                ) {
                    part.weeks.remove(currentWeek.value)
                    saveLessons()
                    initLessons()
                    updateIndexLessons()
                    return
                }
            }
        }
    }

    fun deleteThisTime() {
        val dl = detailLesson.value!!
        for (serverLesson in lessons.value!!) {
            for (part in serverLesson.parts) {
                if (serverLesson.name == dl.name
                    && serverLesson.teacher == dl.teacher
                    && part.weekday == dl.weekday
                    && part.start == dl.start
                    && part.end == dl.end
                ) {
                    serverLesson.parts.remove(part)
                    saveLessons()
                    initLessons()
                    updateIndexLessons()
                    return
                }
            }
        }
    }

    fun deleteThisLesson() {
        val dl = detailLesson.value!!
        for (serverLesson in lessons.value!!) {
            if (serverLesson.name == dl.name
                && serverLesson.teacher == dl.teacher
            ) {
                lessons.value?.remove(serverLesson)
                saveLessons()
                initLessons()
                updateIndexLessons()
                return
            }
        }
    }

    fun updateAddedParams(weekday: String, start: Int, end: Int): String {
        addLessonWeekday.value = weekday
        addLessonStart.value = min(start, end)
        addLessonEnd.value = max(start, end)
        return "${addLessonWeekday.value} 第${addLessonStart.value}节 至 第${addLessonEnd.value}节"
    }

    fun addOrRemoveSelected(week: String, selected: Boolean) {
        if (selected) {
            addLessonWeeks.value?.add(week.toInt())
        } else {
            addLessonWeeks.value?.remove(week.toInt())
        }
    }

    fun checkAddForm(): String {
        if (addLessonName.value!!.isBlank()) {
            return "请输入课程名"
        }
        if (addLessonWeeks.value!!.isEmpty()) {
            return "请选择教学周"
        }
        if (addLessonTeacher.value!!.isBlank()) {
            return "请输入教师名"
        }
        return ""
    }

    fun checkSameLesson(): Int {
        lessons.value!!.forEachIndexed { index, item ->
//            logd((item.name == addLessonName.value).toString())
//            logd((item.teacher == addLessonTeacher.value).toString())
            if (item.name == addLessonName.value && item.teacher == addLessonTeacher.value) {
                return index
            }
        }
        return -1
    }

    fun checkConflict(): Boolean {
        val all = lessons.value!!
        for (i in all.indices) {
            val lessonItem = all[i]
            for (j in lessonItem.parts.indices) {
                val partItem = lessonItem.parts[j]
                for (k in partItem.weeks.indices) {
                    val weekItem = partItem.weeks[k]
//                    logd((addLessonWeekday.value!!.replace("周", "星期") == partItem.weekday).toString())
//                    logd((!(addLessonEnd.value!! < partItem.start || addLessonStart.value!! > partItem.end)).toString())
//                    logd((addLessonWeeks.value!!.contains(weekItem)).toString())
//                    logd()
                    if (addLessonWeekday.value!!.replace("周", "星期") == partItem.weekday
                        && !(addLessonEnd.value!! < partItem.start || addLessonStart.value!! > partItem.end)
                        && addLessonWeeks.value!!.contains(weekItem)
                    ) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun doAdd(callback: () -> Unit) {
//        logd(
//            addLessonName.value,
//            addLessonWeekday.value,
//            addLessonStart.value.toString(),
//            addLessonEnd.value.toString(),
//            addLessonWeeks.value.toString(),
//            addLessonTeacher.value.toString(),
//            addLessonPlace.value.toString()
//        )
        val all = lessons.value!!
        val hasSameLesson = checkSameLesson()
        val bgs = ArrayList<String>()
        for (i in all.indices)
            bgs.add(all[i].bg!!)
        val bg = createBg(bgs)
        for (i in all.indices) {
            val lessonItem = all[i]
            for (j in lessonItem.parts.indices) {
                val partItem = lessonItem.parts[j]
                for (k in partItem.weeks.indices) {
                    val weekItem = partItem.weeks[k]
                    if (addLessonWeekday.value!!.replace("周", "星期") == partItem.weekday &&
                        !(addLessonEnd.value!! < partItem.start || addLessonStart.value!! > partItem.end) &&
                        addLessonWeeks.value!!.indexOf(weekItem) > -1
                    ) {
                        addLessonWeeks.value!!.remove(weekItem)
                    }
                }
            }
        }
        if (addLessonWeeks.value!!.isEmpty()) {
            callback()
            return
        }
        if (hasSameLesson != -1) {
            val lessonParts = all[hasSameLesson].parts
            for (i in lessonParts.indices) {
                val lessonPart = lessonParts[i]
                if (addLessonWeekday.value!!.replace("周", "星期") == lessonPart.weekday
                    && addLessonStart.value == lessonPart.start
                    && addLessonEnd.value == lessonPart.end
                ) {
                    val lessonWeeks = lessonPart.weeks
                    for (j in addLessonWeeks.value!!.indices) {
                        if (lessonWeeks.indexOf(addLessonWeeks.value!![j]) == -1) {
                            lessonWeeks.add(addLessonWeeks.value!![j])
                        }
                    }
                    lessons.value = all
                    callback()
                    return
                }
            }
            lessonParts.add(
                Part(
                    addLessonWeekday.value!!.replace("周", "星期"),
                    addLessonStart.value!!,
                    addLessonEnd.value!!,
                    addLessonPlace.value!!,
                    addLessonWeeks.value!!
                )
            )
            lessons.value = all
            callback()
            return
        }
        all.add(
            ServerLesson(
                addLessonName.value!!,
                addLessonTeacher.value!!,
                bg,
                arrayListOf(
                    Part(
                        addLessonWeekday.value!!.replace("周", "星期"),
                        addLessonStart.value!!,
                        addLessonEnd.value!!,
                        addLessonPlace.value!!,
                        addLessonWeeks.value!!
                    )
                ),
                true
            )
        )
        lessons.value = all
        callback()
    }

    private fun createBg(all: ArrayList<String>): String {
        val r = floor(Math.random() * 255)
        val g = floor(Math.random() * 255)
        val b = floor(Math.random() * 255)
        if (r * 0.299 + g * 0.578 + b * 0.114 < 115) return createBg(all)
        var R = r.toInt().toString(16)
        var G = g.toInt().toString(16)
        var B = b.toInt().toString(16)
        R = if (R.length == 1) "0$R" else R
        G = if (G.length == 1) "0$G" else G
        B = if (B.length == 1) "0$B" else B
        val bg = "#$R$G$B"
        for (i in all.indices)
            if (all[i] == bg)
                return createBg(all)
        return bg
    }

    fun saveLessons() {
        gson.toJson(
            hashMapOf(
                Constants.KEY_LESSONS to lessons.value,
                Constants.KEY_WEEK_TOTAL to totalWeek.value
            )
        ).save(Constants.KEY_LESSON_DATA)
    }

    fun changeDetailColor() {
        val bgs = ArrayList<String>()
        for (i in lessons.value!!.indices)
            bgs.add(lessons.value!![i].bg!!)
        val tmpDetail = detailLesson.value!!.apply {
            bg = createBg(bgs).apply { bgs.add(this) }
        }
        detailLesson.value = tmpDetail
        val all = lessons.value!!
        for (lesson in all) {
            if (lesson.name == tmpDetail.name && lesson.teacher == tmpDetail.teacher) {
                lesson.bg = tmpDetail.bg
                saveLessons()
                lessons.value = all
                return
            }
        }
    }

    fun checkManual(): Boolean {
        for (lesson in lessons.value!!) {
            if (lesson.isManual) {
                return true
            }
        }
        return false
    }

    fun initTerm() {
        val termValue = kv.decodeString(Constants.KEY_TERM) ?: return
        term.value = termValue
    }
}