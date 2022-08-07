package cn.devmeteor.treepicker

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

open class TreePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val tab: RecyclerView
    private val list: RecyclerView
    private var data: TreeNode? = null
    private var tabStack: Stack<TreeNode>? = null
    private val resStack: Stack<Int> = Stack()
    private var config: Configuration = Configuration.Builder(context).build()
    private var onPickListener: OnPickListener? = null
    private var lastPickNode: TreeNode? = null

    init {
        resolveAttrs(attrs)
        orientation = VERTICAL
        tab = RecyclerView(context).apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            scrollBarSize = 0
            overScrollMode = OVER_SCROLL_NEVER
        }
        list = RecyclerView(context).apply {
            overScrollMode = OVER_SCROLL_NEVER
            layoutManager = LinearLayoutManager(context)
            val divider=DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(context,R.drawable.shape_divider)!!)
            addItemDecoration(divider)
            scrollBarSize = 0
        }
        addView(tab)
        addView(View(context).apply {
            setBackgroundColor(Color.parseColor("#cccccc"))
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                context.resources.getDimension(R.dimen.height_picker_divider).toInt()
            )
        })
        addView(list)
    }

    fun config(configuration: Configuration) {
        config = configuration
    }

    private fun resolveAttrs(attrs: AttributeSet?) {
        if (attrs == null) return
        context.obtainStyledAttributes(attrs, R.styleable.TreePicker).apply {
            config.tabItemPadding = getDimension(
                R.styleable.TreePicker_tabItemPadding,
                config.tabItemPadding.toFloat()
            ).toInt()
            config.tabHeight =
                getDimension(R.styleable.TreePicker_tabHeight, config.tabHeight.toFloat()).toInt()
            config.tabTextSelectedColor =
                getColor(R.styleable.TreePicker_tabTextSelectedColor, config.tabTextSelectedColor)
            config.tabTextNormalColor =
                getColor(R.styleable.TreePicker_tabTextNormalColor, config.tabTextNormalColor)
            getDrawable(R.styleable.TreePicker_tabUnderline)?.let { config.tabUnderline = it }
            config.tabTextSize = getDimension(
                R.styleable.TreePicker_tabTextSize,
                config.tabTextSize.toFloat()
            ).toInt()
            config.levelTextSize = getDimension(
                R.styleable.TreePicker_levelTextSize,
                config.levelTextSize.toFloat()
            ).toInt()
            config.levelPadding = getDimension(
                R.styleable.TreePicker_levelPadding,
                config.levelPadding.toFloat()
            ).toInt()
            getDrawable(R.styleable.TreePicker_levelCheckDrawable)?.let {
                config.levelCheckDrawable = it
            }
            config.levelTextNormalColor =
                getColor(R.styleable.TreePicker_levelTextNormalColor, config.levelTextNormalColor)
            config.levelTextSelectedColor = getColor(
                R.styleable.TreePicker_levelTextSelectedColor,
                config.levelTextSelectedColor
            )
            recycle()
        }
    }

    open fun setData(convertible: Convertible) {
        val data = convertible.convert()
        this.data = data
        tabStack = Stack()
        tabStack!!.push(data)
        tab.adapter = TabAdapter(config, context, tabStack!!, resStack, tabClickListener)
        list.adapter = LevelAdapter(
            config,
            context,
            data.next!!,
            levelClickListener as PickerItemClickListener
        )
    }


    private val levelClickListener = object : PickerItemClickListener {
        override fun onClick(node: TreeNode, position: Int) {
            if (node.next != null) {
                list.scrollToPosition(0)
                onPickListener?.onPick(
                    position,
                    if (tabStack != null) ArrayList(resStack) else ArrayList(),
                    false
                )
                tabStack?.push(node)
                resStack.push(position)
                tab.adapter?.notifyDataSetChanged()
                if (tabStack != null) {
                    tab.scrollToPosition(tabStack!!.size - 1)
                }
            } else {
                if (node != lastPickNode) {
                    onPickListener?.onPick(
                        position,
                        if (tabStack != null) ArrayList(resStack) else ArrayList(),
                        true
                    )
                    lastPickNode = node
                }
            }
        }
    }

    private val tabClickListener = object : PickerItemClickListener {
        override fun onClick(node: TreeNode, position: Int) {
            list.adapter = LevelAdapter(
                config, context, node.next!!,
                levelClickListener
            )
            list.scrollToPosition(0)
        }
    }

    fun setOnPickListener(onPickListener: OnPickListener) {
        this.onPickListener = onPickListener
    }

    fun isListOnTop()=!list.canScrollVertically(-1)
    fun isListOnBottom()=!list.canScrollVertically(1)

    interface PickerItemClickListener {
        fun onClick(node: TreeNode, position: Int)
    }

    interface OnPickListener {
        fun onPick(node: Int, parents: List<Int>, isFinalPick: Boolean)
    }

    interface Convertible {
        fun convert(): TreeNode
    }

}