package com.lkdont.widget

import android.content.Context
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View

/**
 * 抽屉背景阴影
 *
 * Created by kidonliang on 2017/8/2.
 */
class DrawerShadow(context: Context?, attrs: AttributeSet? = null) : View(context, attrs) {

    companion object {
        @JvmField val MAX_FADE = 0x9f
    }

    init {
        setBackgroundColor(ContextCompat.getColor(context!!, android.R.color.black))
        alpha = 0f
    }

    /**
     * 设置阴影深度
     */
    fun fade(fade: Int) {
        alpha = fade.toFloat() / 0xff
    }

}
