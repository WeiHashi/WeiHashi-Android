package cn.devmeteor.weihashi

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import cn.devmeteor.weihashi.utils.Util.kv
import com.blankj.utilcode.util.LogUtils
import com.tencent.mmkv.MMKV
import java.util.*


inline fun <reified VB : ViewBinding> Activity.inflate() = lazy {
    inflateBinding<VB>(layoutInflater).apply { setContentView(root) }
}

inline fun <reified VB : ViewBinding> Dialog.inflate() = lazy {
    inflateBinding<VB>(layoutInflater).apply { setContentView(root) }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified VB : ViewBinding> inflateBinding(layoutInflater: LayoutInflater) =
    VB::class.java.getMethod("inflate", LayoutInflater::class.java)
        .invoke(null, layoutInflater) as VB

fun String.httpsScheme(): String = replace("http://", "https://")

fun String.save(tag: String): String = apply {
    kv.encode(tag, this)
}

fun String.remove() = kv.remove(this)

fun String.delete(vararg strings: String): String {
    var c = this
    for (s in strings) {
        c = c.replace(s, "")
    }
    return c
}

fun String.lowerCase(): String = toLowerCase(Locale.ROOT)

fun String.upperCase(): String = toUpperCase(Locale.ROOT)

fun MMKV.remove(vararg ids: String) = removeValuesForKeys(ids)

inline fun logd(vararg msg: String?) = LogUtils.d(msg)
inline fun loge(vararg msg: String?) = LogUtils.e(msg)