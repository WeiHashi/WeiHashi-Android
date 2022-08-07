package cn.devmeteor.treepicker

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class TabAdapter(
    private val config: Configuration,
    private val context: Context,
    private val stack: Stack<TreeNode>,
    private val resStack: Stack<Int>,
    private val tabClickListener: TreePicker.PickerItemClickListener
) :
    RecyclerView.Adapter<TabAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, config.tabTextSize.toFloat())
            setPadding(config.tabItemPadding, 0, config.tabItemPadding, 0)
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                config.tabHeight
            )
        })

    override fun onBindViewHolder(holder: VH, position: Int) {
        (holder.itemView as TextView).setTextColor(
            if (position == stack.size - 1) config.tabTextSelectedColor
            else config.tabTextNormalColor
        )
        val drawable = if (position == stack.size - 1)
            config.tabUnderline
        else
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.shape_underline_normal,
                null
            )!!
        drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        holder.itemView.setCompoundDrawables(
            null,
            null,
            null,
            drawable
        )
        holder.itemView.text = stack[position].name
        holder.itemView.setOnClickListener {
            val node = stack[position]
            tabClickListener.onClick(node, 0)
            while (stack[stack.size - 1] != node) {
                stack.pop()
                resStack.pop()
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = stack.size

    override fun getItemViewType(position: Int): Int = position

}