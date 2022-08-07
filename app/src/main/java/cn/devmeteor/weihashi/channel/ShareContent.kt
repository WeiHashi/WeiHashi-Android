package cn.devmeteor.weihashi.channel

data class ShareContent(
    var type: Int,
    var title: String,
    var summary: String,
    var url: String,
    var thumb: String
) {
    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_IMAGE = 1
        const val TYPE_VOICE = 2
        const val TYPE_VIDEO = 3
        const val TYPE_WEB = 4
        const val TYPE_PROGRAM = 5
    }

    fun typeOf(type: Int): Boolean = this.type == type

}
