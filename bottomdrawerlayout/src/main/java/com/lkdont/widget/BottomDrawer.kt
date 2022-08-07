package com.lkdont.widget

import android.content.Context
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

/**
 * 底部抽屉菜单
 *
 * Created by kidonliang on 2017/8/1.
 */
class BottomDrawer(context: Context, attrs: AttributeSet? = null)
    : FrameLayout(context, attrs) {

    private var mScrollableViewId: Int = -1

    /**
     * 设置可滚动的子控件
     *
     * @param id 控件id
     */
    fun setScrollableViewId(@IdRes id: Int) {
        mScrollableViewId = id
    }

    private var mScrollable: View? = null

    private fun findScrollableView(): View? {

        if (mScrollable == null) {
            if (mScrollableViewId > 0) {
                // 如果容器下有多个view，则可以通过id来找到对应的view
                mScrollable = findViewById(mScrollableViewId)
            } else {
                // 默认返回容器下的第一个view
                mScrollable = getChildAt(0)
            }
        }

        return mScrollable
    }

    override fun canScrollVertically(direction: Int): Boolean {
        val child: View = findScrollableView() ?: return super.canScrollVertically(direction)
//        return ViewCompat.canScrollVertically(child, direction)
        return child.canScrollVertically(direction)
    }

}
