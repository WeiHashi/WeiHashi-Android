package cn.devmeteor.weihashi.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet

class BannerImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    private var width: Float = 0f
    private var height: Float = 0f

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        width = getWidth().toFloat()
        height = getHeight().toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        if (width >= 20 && height > 20) {
            val path = Path()
            path.moveTo(20f, 0f)
            path.lineTo(width - 20, 0f)
            path.quadTo(width, 0f, width, 20f)
            path.lineTo(width, height - 20)
            path.quadTo(width, height, width - 20, height)
            path.lineTo(20f, height)
            path.quadTo(0f, height, 0f, height - 20)
            path.lineTo(0f, 20f)
            path.quadTo(0f, 0f, 20f, 0f)
            canvas!!.clipPath(path)
        }
        super.onDraw(canvas)
    }
}