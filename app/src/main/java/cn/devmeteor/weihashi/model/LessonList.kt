package cn.devmeteor.weihashi.model

import cn.devmeteor.responsegenerator.ResponseModel

@ResponseModel
data class LessonList(var weekTotal: Int, var lessons: ArrayList<ServerLesson>)