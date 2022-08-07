package com.lkdont.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.lkdont.widget.bottomdrawerlayout.R
import kotlin.math.abs


/**
 * 底部抽屉菜单容器
 *
 * Created by kidonliang on 2017/8/1.
 */
class BottomDrawerContainer(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    val DEBUG = false
    val TAG = "BottomDrawerContainer"

    // 底部抽屉最小显示高度
    private val MIN_HEIGHT: Int

    // 默认抽屉动画时长
    private val DEFAULT_ANIMATION_DURATION = 100L

    // 最小触发抽屉展开/隐藏的速度值
    private val MIN_TRIGGER_VELOCITY: Int
    private val TOUCH_SLOP: Int

    // 在抽屉关闭时，move事件超过这个数值才算有效
    private val MIN_MOVE_COUNT = 2

    private val location = IntArray(2)

    private var mMaxVelocity: Float

    private var grip: View? = null

    private var contentStateListener: ContentStateListener? = null

    fun setContentStateListener(contentStateListener: ContentStateListener) {
        this.contentStateListener = contentStateListener
    }

    init {
        MIN_HEIGHT = context.resources.getDimensionPixelSize(R.dimen.height_closed)
        val vc = ViewConfiguration.get(context)
        TOUCH_SLOP = vc.scaledTouchSlop
        mMaxVelocity = vc.scaledMaximumFlingVelocity.toFloat()
        MIN_TRIGGER_VELOCITY = 4 * vc.scaledMinimumFlingVelocity
    }

    private var mIsDragging = false
    private var mFirstDownY = 0F
    private var mDownY = 0F

    private var mVelocityTracker: VelocityTracker? = null

    private var mMoveCount = 0

    private var lastX = 0F
    private var lastY = 0F
    private var lastRawY = 0F

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {

            MotionEvent.ACTION_MOVE -> mMoveCount++

            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> mMoveCount = 0
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (DEBUG) Log.i(TAG, "onInterceptTouchEvent ev=" + ev.action)

        if (ev.action == MotionEvent.ACTION_DOWN) {
            mFirstDownY = ev.y
            lastX = ev.x
            lastY = ev.y
            lastRawY = ev.rawY
            //抽屉区域外事件不拦截
            if (ev.rawY < contentStateListener!!.contentRawY()) {
                return false
            }
        }

        if (ev.action == MotionEvent.ACTION_MOVE) {
            //只拦截抽屉区域内move事件
            if (ev.rawY > contentStateListener!!.contentRawY()) {
                //拦截横向滑动
                if (isOpened() && abs(ev.y - lastY) <= abs(ev.x - lastX)) {
                    return true
                }
                //拦截内部滑动到底时继续上滑
                if (isOpened() && contentStateListener!!.contentOnBottom() && ev.y - lastY < 0) {
                    return true
                }
            }
        }
//        Log.d("onInterceptTouchEvent", ev.rawY.toString())
        grip?.getLocationInWindow(location)
//        Log.d("onInterceptTouchEvent", (location[1]+grip!!.height).toString())
        //拦截头部露出区域事件
        if (grip != null && ev.rawY > location[1] && ev.rawY < location[1] + grip!!.height) {
            return true
        }
        //拦截滑动到顶部后继续下滑
        if ((!isOpened() || contentStateListener!!.contentOnTop()) && handleTouchEvent(ev)) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    fun setGrip(grip: View) {
        this.grip = grip
    }

    /**
     * 是否处理这个事件
     */
    private fun handleTouchEvent(ev: MotionEvent?): Boolean {
        if (mIsAnimating || ev == null || mTarget == null) return false
        val mHeight = measuredHeight
        val handle = (mHeight - ev.y) <= (mHeight - mTarget!!.y)

        if (handle) {
            val yDiff = ev.y - mFirstDownY
            if (isOpened()) {
                // 开启状态
                if (Math.abs(yDiff) > TOUCH_SLOP) {
                    // 是否允许下拉
                    val canScrollUp = mTarget?.canScrollVertically(-1) ?: false
                    if (yDiff > 0) {
                        // 下拉
                        return !canScrollUp
                    }
                }
                return false
            } else {
                if (DEBUG) Log.i(
                    TAG,
                    "handleTouchEvent state=closed, mMoveCount=$mMoveCount, yDiff=$yDiff, TOUCH_SLOP=$TOUCH_SLOP"
                )
                // 关闭状态，如果是点击就响应item的点击事件，如果是快速滑动拦截这个事件自己处理
                return mMoveCount >= MIN_MOVE_COUNT || Math.abs(yDiff) > TOUCH_SLOP
            }
        }

        return handle
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // init tracker
        addMovementToTracker(event)
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {

                if (!mIsDragging && Math.abs(event.y - mFirstDownY) > TOUCH_SLOP) {
                    mIsDragging = true
                    mDownY = event.y
                }

                if (mIsDragging) {
                    val deltaY = event.y - mDownY
                    mDownY = event.y

                    val mTargetY = mTarget!!.y
                    val mMeasureHeight = measuredHeight
                    val mTargetHeight = mTarget!!.measuredHeight
                    if (mTargetY + deltaY <= mMeasureHeight - mTargetHeight) {
                        mTarget!!.y = (mMeasureHeight - mTargetHeight).toFloat()
                    } else if (mTargetY + deltaY >= mMeasureHeight - MIN_HEIGHT) {
                        mTarget!!.y = (mMeasureHeight - MIN_HEIGHT).toFloat()
                    } else {
                        val offset = deltaY.toInt()
                        mTarget!!.y = mTargetY + offset
                    }
                    updateShadow()
                }
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {

                mIsDragging = false
                mFirstDownY = 0F
                mDownY = 0F

                computeVelocity()
                if (yVelocity > 0 && yVelocity > MIN_TRIGGER_VELOCITY) {
                    closeDrawer()
                } else if (yVelocity < 0 && (-yVelocity) > MIN_TRIGGER_VELOCITY) {
                    openDrawer()
                } else {
                    val mTargetY = mTarget!!.y
                    val mTargetHeight = mTarget!!.measuredHeight
                    val mMeasureHeight = measuredHeight
                    if ((mMeasureHeight - mTargetY) > (mTargetHeight / 2)) {
                        openDrawer()
                    } else {
                        closeDrawer()
                    }
                }

                // clear tracker
                clearMovementFromTracker()
            }
        }
        return true
    }

    private fun updateShadow() {
        mShadow?.fade(((1 - mTarget!!.y / (measuredHeight - MIN_HEIGHT)) * DrawerShadow.MAX_FADE).toInt())
    }

    private fun addMovementToTracker(event: MotionEvent?) {
        if (!mIsDragging || event == null) return
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
    }

    private fun clearMovementFromTracker() {
        mVelocityTracker?.clear()
        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    private var xVelocity: Float = 0F
    private var yVelocity: Float = 0F

    private fun computeVelocity() {
        if (mVelocityTracker == null) return
        //units是单位表示， 1代表px/毫秒, 1000代表px/秒
        mVelocityTracker!!.computeCurrentVelocity(1000, mMaxVelocity)
        xVelocity = mVelocityTracker!!.xVelocity
        yVelocity = mVelocityTracker!!.yVelocity
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (mTarget == null) {
            ensureTarget()
            if (mTarget == null) {
                Log.w(TAG, "onLayout: target drawer not found.")
                return
            }
        }

        val parentWidth = measuredWidth
        val parentHeight = measuredHeight

        mTarget?.layout(
            0,
            parentHeight - MIN_HEIGHT,
            parentWidth,
            parentHeight - MIN_HEIGHT + (mTarget?.measuredHeight ?: 0)
        )
    }

    private var mTarget: BottomDrawer? = null
    private var mShadow: DrawerShadow? = null

    /**
     * 找出抽屉控件
     */
    private fun ensureTarget() {
        for (i in 0..(childCount - 1)) {
            val child = getChildAt(i)
            if (child is BottomDrawer) {
                mTarget = child
            }
            if (child is DrawerShadow) {
                // 阴影
                mShadow = child
            }
        }
    }

    private var mIsAnimating = false
    private val mOpenAnimationInterpolator = DecelerateInterpolator()
    private val mCloseAnimationInterpolator = DecelerateInterpolator()

    private val mShadowClickListener = View.OnClickListener {
        if (isOpened())
            closeDrawer()
    }

    /**
     * 打开抽屉
     */
    fun openDrawer() {
        if (mIsAnimating || mIsDragging || mTarget == null) return
        val mMeasureHeight = measuredHeight
        val mTargetHeight = mTarget!!.measuredHeight

        val animator =
            ValueAnimator.ofFloat(mTarget!!.y, (mMeasureHeight - mTargetHeight).toFloat())
                .setDuration(2 * DEFAULT_ANIMATION_DURATION)
        animator.setTarget(mTarget)
        animator.interpolator = mOpenAnimationInterpolator
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            mTarget!!.y = value
            updateShadow()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                mIsAnimating = true
            }

            override fun onAnimationEnd(animation: Animator) {
                mIsAnimating = false
            }

            override fun onAnimationCancel(animation: Animator) {
                mIsAnimating = false
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
        mOpened = true
        mShadow?.setOnClickListener(mShadowClickListener)
        mShadow?.isClickable = true
    }

    /**
     * 关闭抽屉
     */
    fun closeDrawer() {
        if (mIsAnimating || mIsDragging || mTarget == null) return
        val mMeasureHeight = measuredHeight
        val animator = ValueAnimator.ofFloat(mTarget!!.y, (mMeasureHeight - MIN_HEIGHT).toFloat())
        animator.interpolator = mCloseAnimationInterpolator
        animator.setTarget(mTarget)
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            mTarget!!.y = value
            updateShadow()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                mIsAnimating = true
            }

            override fun onAnimationEnd(animation: Animator) {
                mIsAnimating = false
            }

            override fun onAnimationCancel(animation: Animator) {
                mIsAnimating = false
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
        mOpened = false
        mShadow?.setOnClickListener(null)
        mShadow?.isClickable = false
    }

    private var mOpened = false

    /**
     * 抽屉是否已打开
     */
    fun isOpened(): Boolean {
        return mOpened
    }

    interface ContentStateListener {
        fun contentOnTop(): Boolean
        fun contentOnBottom(): Boolean
        fun contentRawY(): Float
    }

}