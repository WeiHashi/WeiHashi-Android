package cn.devmeteor.weihashi.model


data class ServerLesson(
    var name: String,
    var teacher: String,
    var bg: String?,
    var parts: ArrayList<Part>,
    var isManual:Boolean=false
) {
    companion object {
        val emptyServerLesson = ServerLesson("", "", "", ArrayList())
    }
}