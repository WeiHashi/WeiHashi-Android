package cn.devmeteor.weihashi.model

data class Part(
    var weekday: String,
    var start: Int,
    var end: Int,
    var place: String,
    var weeks: ArrayList<Int>
)