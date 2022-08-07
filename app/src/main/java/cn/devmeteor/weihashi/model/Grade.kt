package cn.devmeteor.weihashi.model

import cn.devmeteor.responsegenerator.ResponseModel

@ResponseModel
data class Grade(
    var totalCredit: String,
    var gpa: String,
    var avgScore: String,
    var list: List<CJ>
)