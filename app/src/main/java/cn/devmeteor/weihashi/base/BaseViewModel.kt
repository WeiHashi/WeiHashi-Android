package cn.devmeteor.weihashi.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.api.ApiException
import cn.devmeteor.weihashi.logd
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.*

abstract class BaseViewModel : ViewModel() {

    private val apiException = MutableLiveData<ApiException>()
    private val otherException = MutableLiveData<Exception>()

    fun handleResult(op: suspend CoroutineScope.() -> Unit) = viewModelScope.launch {
        try {
            withTimeout(Constants.TIMEOUT_LIMIT) {
                op()
            }
        } catch (e: ApiException) {
            apiException.value = e
        } catch (e: Exception) {
            logd(Arrays.toString(e.stackTrace))
            CrashReport.postCatchedException(e)
            otherException.value = e
        }
    }

    fun handleException(
        owner: LifecycleOwner,
        otherExceptionHandler: (e: Exception) -> Unit,
        apiExceptionHandler: (e: ApiException) -> Unit = {}
    ) {
        apiException.observe(owner, { apiExceptionHandler(it) })
        otherException.observe(owner, { otherExceptionHandler(it) })
    }


}