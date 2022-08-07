package cn.devmeteor.weihashi.model

import cn.devmeteor.tableview.Lesson

class Lesson(
    term: String,
    name: String,
    weekday: String,
    start: Int,
    end: Int,
    place: String,
    var teacher: String,
    var bg: String
) : Lesson(term, name, weekday, start, end, place) {
    companion object {
        val emptyLesson = Lesson("", "", "", 0, 0, "", "", "")
    }
}