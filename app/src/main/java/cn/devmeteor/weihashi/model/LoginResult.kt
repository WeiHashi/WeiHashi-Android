package cn.devmeteor.weihashi.model

import cn.devmeteor.responsegenerator.ResponseModel

@ResponseModel
data class LoginResult(var name: String, var cookies: String)
