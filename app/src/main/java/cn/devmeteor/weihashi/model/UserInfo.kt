package cn.devmeteor.weihashi.model

import cn.devmeteor.responsegenerator.ResponseModel

@ResponseModel
data class UserInfo(var term: String, var termStart: String,var testPermission:Boolean)
