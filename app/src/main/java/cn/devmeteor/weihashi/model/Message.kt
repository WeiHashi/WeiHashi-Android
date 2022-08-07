package cn.devmeteor.weihashi.model

import android.os.Parcelable
import cn.devmeteor.weihashi.utils.Util
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Message(
    var objId: String,
    var title: String,
    var detail: String,
    var top: Boolean,
    var timestamp: String
) : Parcelable {

    companion object {
        fun fromNotice(notice: Notice, top: Boolean): Message = with(notice) {
            Message(obj_id, title, detail, top, Util.date2Timestamp(Date(timestamp)))
        }
    }
}
