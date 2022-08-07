package cn.devmeteor.weihashi.utils

import java.lang.RuntimeException

object TestManager {

    private var isTest = false
    private var hasInit = false

    fun init() {
        if (hasInit) {
            throw RuntimeException("重复初始化")
        }
        isTest = true
        hasInit = true
    }

    fun isTest() = isTest

}