package cn.devmeteor.weihashi.widget.lesson

import android.content.Context
import android.util.AttributeSet
import cn.devmeteor.tableview.TableView
import cn.devmeteor.weihashi.model.Lesson
import cn.devmeteor.weihashi.utils.Util
import java.util.*

class Timetable @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : TableView<Lesson>(context, attrs, defStyle) {
    init {
        setWeekStart(Util.getCurrentWeekStart(Date()))
        setResolveFlags(arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"))
        setTimes(
            arrayOf(
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
            ), arrayOf(
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
        )
    }
}