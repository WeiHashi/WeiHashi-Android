package cn.devmeteor.weihashi.model

import cn.devmeteor.responsegenerator.ResponseModel

@ResponseModel
data class MessageP1(var total: Int, var tops: ArrayList<Notice>, var normals: ArrayList<Notice>)