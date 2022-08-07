package cn.devmeteor.weihashi.model

import cn.devmeteor.responsegenerator.ResponseListModel
import cn.devmeteor.responsegenerator.ResponseModel

@ResponseModel
@ResponseListModel
data class Notice(
    var obj_id: String,
    var cate: String,
    var title: String,
    var detail: String,
    var type: Int,
    var timestamp: Long,
    var top_level: Int
)