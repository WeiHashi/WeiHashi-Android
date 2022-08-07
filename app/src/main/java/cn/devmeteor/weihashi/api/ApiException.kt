package cn.devmeteor.weihashi.api

class ApiException(val code: Int, val msg: String) : Exception(msg)