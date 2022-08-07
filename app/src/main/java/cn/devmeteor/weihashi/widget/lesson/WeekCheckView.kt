package cn.devmeteor.weihashi.widget.lesson

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import cn.devmeteor.weihashi.R
import com.hurryyu.bestchooser.ChooserView

class WeekCheckView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyle: Int = 0
) : ChooserView(context, attr, defStyle) {

    private lateinit var view: TextView

    override fun createView(attrs: AttributeSet?) {
        val contentView = LayoutInflater.from(context).inflate(R.layout.view_week_check, this)
        view = contentView.findViewById(R.id.week_check)
        attrs?.apply {
            val typedArray = context.obtainStyledAttributes(this, R.styleable.WeekCheckView)
            view.text = typedArray.getString(R.styleable.WeekCheckView_week) ?: ""
            typedArray.recycle()
        }
    }

    override fun onSelectChange(isSelect: Boolean) {
        view.isSelected = isSelected
        view.setTextColor(if (isSelect) Color.parseColor("#c1272d") else Color.parseColor("#858C96"))
    }


    fun setText(text: String) {
        view.text = text
    }
}