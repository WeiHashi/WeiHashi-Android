package cn.devmeteor.weihashi.viewmodel

import androidx.lifecycle.MutableLiveData
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.api.ApiException
import cn.devmeteor.weihashi.api.Repository
import cn.devmeteor.weihashi.base.BaseViewModel
import cn.devmeteor.weihashi.remove
import cn.devmeteor.weihashi.save
import cn.devmeteor.weihashi.utils.TestManager
import cn.devmeteor.weihashi.utils.Util.kv
import kotlinx.coroutines.Job
import java.net.URLEncoder

class UserViewModel : BaseViewModel() {

    private val repo = Repository()
    private val nickname = MutableLiveData("未登录")
    private val jwBound = MutableLiveData(false)
    private val name = MutableLiveData("")
    private val studentId = MutableLiveData("")
    private val avatar = MutableLiveData("")
    private val test = MutableLiveData(false)

    fun updateQQInfo() = with(kv) {
        nickname.value = decodeString(Constants.KEY_NICKNAME, "未登录")!!
        avatar.value = decodeString(Constants.KEY_AVATAR, "")!!
    }

    fun updateJwInfo() {
        studentId.value = kv.decodeString(Constants.KEY_STUDENT_ID, "")
        if (studentId.value.isNullOrEmpty())
            return
        name.value = kv.decodeString(Constants.KEY_NAME)!!
        jwBound.value = true
    }

    fun getNickname() = nickname

    fun isJwBound() = jwBound

    fun getName() = name

    fun getStudentId() = studentId

    fun getAvatar(): String = avatar.value!!

    fun bind(studentId: String, password: String, callback: (Boolean) -> Unit) {
        handleResult {
            if (studentId.isBlank() || password.isBlank()) {
                throw ApiException(0, "学号和密码不能为空")
            }
            val openid = kv.decodeString(Constants.KEY_OPENID, "unknown")!!
            val res = repo.jwLogin(openid, studentId, password)
            res.data.cookies.save(Constants.KEY_COOKIES)
            password.save(Constants.KEY_PASSWORD)
            this@UserViewModel.studentId.value = studentId.save(Constants.KEY_STUDENT_ID)
            this@UserViewModel.name.value = res.data.name.save(Constants.KEY_NAME)
            jwBound.value = true
            callback(true)
        }
    }

    fun unbind(callback: (Boolean) -> Unit) {
        kv.remove(
                Constants.KEY_STUDENT_ID,
                Constants.KEY_PASSWORD,
                Constants.KEY_NAME,
                Constants.KEY_COOKIES
        )
        name.value = ""
        studentId.value = ""
        jwBound.value = false
        callback(false)
    }

    fun getFeedBackParams(): String = "nickname=${nickname.value}" +
            "&openid=${kv.decodeString(Constants.KEY_OPENID)}" +
            "&avatar=${URLEncoder.encode(avatar.value, Charsets.UTF_8.name())}"

    fun tryInitData(): Job? {
        val openid = kv.decodeString(Constants.KEY_OPENID) ?: return null
        val nickname = kv.decodeString(Constants.KEY_NICKNAME) ?: return null
        val head = kv.decodeString(Constants.KEY_AVATAR) ?: return null
        return handleResult {
            repo.updateAccount(openid, nickname, head).data.apply {
                term.save(Constants.KEY_TERM)
                termStart.save(Constants.KEY_TERM_START)
                test.value = testPermission
            }
        }
    }

    fun checkTest() = !(TestManager.isTest() && !test.value!!)

    fun checkFirstOpen() = kv.decodeBool(Constants.KEY_FIRST_OPEN, true)

    fun firstOpened() = kv.encode(Constants.KEY_FIRST_OPEN, false)

}