package cn.devmeteor.weihashi.api

import cn.devmeteor.responsegenerator.Response
import cn.devmeteor.weihashi.api.ApiClient.api
import cn.devmeteor.weihashi.model.ResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository {

    suspend fun jwLogin(openid: String, username: String, passwd: String) = request {
        api.jwLogin(openid, username, passwd)
    }

    suspend fun getDailyLesson(cookies: String) = request {
        api.getDailyLesson(cookies)
    }

    suspend fun getGrade(cookies: String) = request {
        api.getGrade(cookies)
    }

    suspend fun getBanner() = request {
        api.getBanner()
    }

    suspend fun getBannerContent(objId: String) = request {
        api.getBannerContent(objId)
    }

    suspend fun updateAccount(openid: String, nickname: String, head: String) = request {
        api.updateAccount(openid, nickname, head)
    }

    suspend fun getMessageP1() = request {
        api.getMessageP1()
    }

    suspend fun getMoreMessages(page: Int) = request {
        api.getMoreMessages(page)
    }

    private suspend fun <T : Response> request(op: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            op()
        }.apply {
            when (ResultCode.fromCode(code)) {
                ResultCode.CODE_ERROR -> throw RuntimeException(msg)
                ResultCode.CONNECT_ERROR,
                ResultCode.LOGIN_FAIL,
                ResultCode.NO_GRADE,
                ResultCode.RESOLVE_ERROR,
                ResultCode.NO_LESSON,
                ResultCode.GET_FAILED,
                ResultCode.CONTENT_NOT_EXIST -> throw ApiException(code, msg)
                else -> {
                }
            }
        }

}