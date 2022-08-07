package cn.devmeteor.weihashi

import cn.devmeteor.weihashi.api.ApiClient.api
import cn.devmeteor.weihashi.utils.Util
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.*

class ApiTest {

    private var cookieString =
        """{"SERVERID":"121","JSESSIONID":"6AD222A890E05D384502C91AE8544DC2"}"""

    @Test
    fun login() = request {
        api.jwLogin(IgnoreConstants.openid, IgnoreConstants.xh, IgnoreConstants.passwd)
            .apply {
                if (data != null) {
                    cookieString = data.cookies
                }
            }
    }


    @Test
    fun getDailyLessons() = request {
        api.getDailyLesson(cookieString)
    }

    @Test
    fun getGrade() = request {
        api.getGrade(cookieString)
    }

    @Test
    fun getBanner() = request {
        api.getBanner()
    }

    @Test
    fun getBannerContent() = request {
        api.getBannerContent("dba84d604340")
    }


    @Test
    fun updateAccount() = request {
        api.updateAccount(IgnoreConstants.openid, "test", "xxx")
    }

    @Test
    fun getMessageP1() = request {
        api.getMessageP1()
    }

    @Test
    fun getMoreMessage() = request {
        api.getMoreMessages(2)
    }

    private fun request(block: suspend () -> Any) = runBlocking {
        GlobalScope.launch {
            println(block())
        }.join()
    }

    @Test
    fun w() {
        println(Util.date2Timestamp(Util.getCurrentWeekStart(Date())))
    }
}