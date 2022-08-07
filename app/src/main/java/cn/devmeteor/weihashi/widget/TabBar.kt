package cn.devmeteor.weihashi.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager

class TabBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var viewPager: ViewPager? = null

    init {
        orientation = HORIZONTAL
        setBackgroundColor(Color.WHITE)
    }

    fun attachViewPager(viewPager: ViewPager) {
        this.viewPager = viewPager
        for (index in 0 until childCount) {
            getChildAt(index).setOnClickListener {
                switchTo(index)
            }
        }
    }

    fun switchTo(index: Int) {
        for (i in 0 until childCount) {
            getChildAt(i).isSelected = i == index
        }
        viewPager?.setCurrentItem(index, false)
    }

}