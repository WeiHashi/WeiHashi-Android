package cn.devmeteor.weihashi.viewmodel

import androidx.lifecycle.MutableLiveData
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.api.Repository
import cn.devmeteor.weihashi.base.BaseViewModel
import cn.devmeteor.weihashi.model.*
import cn.devmeteor.weihashi.save
import cn.devmeteor.weihashi.utils.Util.gson
import cn.devmeteor.weihashi.utils.Util.kv
import kotlinx.coroutines.Job

class GradeViewModel : BaseViewModel() {

    private val repo = Repository()
    private val cjs: MutableLiveData<ArrayList<GradeItem>> = MutableLiveData(ArrayList())
    private val gpa: MutableLiveData<String> = MutableLiveData("--")
    private val totalCredit: MutableLiveData<String> = MutableLiveData("--")
    private val avgScore: MutableLiveData<String> = MutableLiveData("--")

    fun getCjs() = cjs
    fun getGpa() = gpa
    fun getTotalCredit() = totalCredit
    fun getAvgScore() = avgScore

    fun updateGrade(): Job = handleResult {
        var cookies = kv.decodeString(Constants.KEY_COOKIES)
        val openid = kv.decodeString(Constants.KEY_OPENID)
        val studentId = kv.decodeString(Constants.KEY_STUDENT_ID)
        val password = kv.decodeString(Constants.KEY_PASSWORD)
        if (cookies == null) {
            cookies = repo.jwLogin(
                openid!!,
                studentId!!,
                password!!
            ).data.cookies.save(Constants.KEY_COOKIES)
        }
        val resp = repo.getGrade(cookies)
        if (resp.code == ResultCode.NEED_LOGIN.code) {
            repo.jwLogin(openid!!, studentId!!, password!!).data.cookies.save(Constants.KEY_COOKIES)
            updateGrade()
            return@handleResult
        }
        totalCredit.value = resp.data.totalCredit
        avgScore.value = resp.data.avgScore
        gpa.value = resp.data.gpa
        cjs.value?.clear()
        val list = resp.data.list
        for (i in list.indices) {
            if (i > 0 && list[i].term != list[i - 1].term) {
                cjs.value?.add(GradeDivider(list[i].term))
            }
            cjs.value?.add(list[i])
        }
        cjs.postValue(cjs.value)
        gson.toJson(resp.data).save(Constants.KEY_GRADE)
    }

    fun initData() {
        val grade = kv.decodeString(Constants.KEY_GRADE) ?: return
        gson.fromJson(grade, Grade::class.java).apply {
            this@GradeViewModel.gpa.value = gpa
            this@GradeViewModel.totalCredit.value = totalCredit
            this@GradeViewModel.avgScore.value = avgScore
            for (i in list.indices) {
                if (i > 0 && list[i].term != list[i - 1].term) {
                    cjs.value?.add(GradeDivider(list[i].term))
                }
                cjs.value?.add(list[i])
            }
        }
    }

}