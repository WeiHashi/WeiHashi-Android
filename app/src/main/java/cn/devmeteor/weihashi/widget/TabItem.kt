package cn.devmeteor.weihashi.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.devmeteor.weihashi.R

class TabItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var text: String? = null
    private val initStatus: Boolean
    private val icon: ImageView
    private val textView: TextView
    private var normalSrc = R.drawable.home_normal
    private var normalTextColor = Color.parseColor("#b3b3b3")
    private var selectedSrc = R.drawable.home_selected
    private var selectedTextColor = Color.parseColor("#c1272d")

    init {
        val attrArray = context.obtainStyledAttributes(attrs, R.styleable.TabItem)
        text = attrArray.getString(R.styleable.TabItem_text)
        initStatus = attrArray.getBoolean(R.styleable.TabItem_initStatus, false)
        normalSrc = attrArray.getResourceId(R.styleable.TabItem_normalSrc, normalSrc)
        normalTextColor = attrArray.getColor(R.styleable.TabItem_normalTextColor, normalTextColor)
        selectedSrc = attrArray.getResourceId(R.styleable.TabItem_selectedSrc, selectedSrc)
        selectedTextColor =
            attrArray.getColor(R.styleable.TabItem_selectedTextColor, selectedTextColor)
        attrArray.recycle()
        gravity = Gravity.CENTER
        orientation = VERTICAL
        icon = ImageView(context)
        icon.layoutParams = LayoutParams(
            resources.getDimension(R.dimen.tabItemIconSize).toInt(),
            resources.getDimension(R.dimen.tabItemIconSize).toInt()
        )
        addView(icon)
        textView = TextView(context)
        addView(textView)
        textView.text = text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        val textParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        textParams.topMargin = 10
        textView.layoutParams = textParams
        isSelected = initStatus
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            icon.setImageResource(selectedSrc)
            textView.setTextColor(selectedTextColor)
        } else {
            icon.setImageResource(normalSrc)
            textView.setTextColor(normalTextColor)
        }
    }
}