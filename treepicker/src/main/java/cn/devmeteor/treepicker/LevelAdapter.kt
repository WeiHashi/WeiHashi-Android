package cn.devmeteor.treepicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView

class LevelAdapter(
    private val config: Configuration,
    private val context: Context,
    private var list: MutableList<TreeNode>,
    private val pickerItemClickListener: TreePicker.PickerItemClickListener
) :
    RecyclerView.Adapter<LevelAdapter.VH>() {
    class VH(itemView: RelativeLayout, config: Configuration) : RecyclerView.ViewHolder(itemView) {

        val text: TextView
        val checkIcon: ImageView

        init {
            itemView.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = TextView(context).apply {
                    setPadding(config.levelPadding)
                    setTextColor(Color.BLACK)
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, config.levelTextSize.toFloat())
                    ellipsize = TextUtils.TruncateAt.END
                }
                checkIcon = ImageView(context).apply {
                    setImageDrawable(config.levelCheckDrawable)
                    visibility = View.GONE
                    layoutParams = RelativeLayout.LayoutParams(
                        text.textSize.toInt(),
                        text.textSize.toInt()
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_END)
                        addRule(RelativeLayout.CENTER_VERTICAL)
                        marginEnd = config.levelPadding
                    }
                }
                addView(text)
                addView(checkIcon)
            }
        }
    }

    private var selected = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(RelativeLayout(context).apply {
        }, config)

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: VH, @SuppressLint("RecyclerView") position: Int) {
        holder.text.setTextColor(
            if (position == selected) config.levelTextSelectedColor
            else config.levelTextNormalColor
        )
        holder.checkIcon.visibility = if (position == selected) View.VISIBLE else View.GONE
        holder.text.text = list[position].name
        holder.itemView.setOnClickListener {
            pickerItemClickListener.onClick(list[position], position)
            if (list[position].next != null) {
                list = list[position].next!!
            } else {
                selected = position
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = position
}