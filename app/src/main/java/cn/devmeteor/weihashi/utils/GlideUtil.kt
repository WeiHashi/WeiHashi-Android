package cn.devmeteor.weihashi.utils

import android.content.Context
import android.os.Handler
import android.widget.ImageView
import cn.devmeteor.weihashi.R
import com.bumptech.glide.Glide

object GlideUtil {

    fun loadBanner(context: Context, url: String, target: ImageView) {
        if (url.isBlank()) return
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.banner_placeholder)
            .into(target)
    }

    fun loadAvatar(context: Context, url: String, target: ImageView) {
        if (url.isBlank()) return
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.user_placeholder)
            .error(R.drawable.user_placeholder)
            .into(target)
    }

    fun loadMessageImage(context: Context, url: String, target: ImageView) {
        if (url.isBlank()) return
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.banner_placeholder)
            .error(R.drawable.banner_placeholder)
            .into(target)
    }

    fun clearCache(context: Context,handler:Handler) {
        Glide.get(context).apply {
            clearMemory()
            Thread {
                clearDiskCache()
                handler.sendEmptyMessage(0)
            }.start()
        }
    }

}