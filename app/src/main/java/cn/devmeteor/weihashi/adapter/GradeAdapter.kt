package cn.devmeteor.weihashi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.databinding.ItemGradeBinding
import cn.devmeteor.weihashi.model.CJ
import cn.devmeteor.weihashi.model.GradeDivider
import cn.devmeteor.weihashi.model.GradeItem

class GradeAdapter(private val context: Context, private val list: List<GradeItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_DIVIDER = 1
    }


    class VH(itemView: View) : RecyclerView.ViewHolder(itemView)
    class Divider(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.grade_decoration_term)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Divider) {
            holder.tv.text = (list[position] as GradeDivider).text
        } else {
            val binding = DataBindingUtil.bind<ItemGradeBinding>(holder.itemView)!!
            binding.cj = list[position] as CJ
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_DIVIDER -> Divider(
                LayoutInflater.from(context).inflate(R.layout.item_grade_decoration, parent, false)
            )
            else -> VH(
                LayoutInflater.from(context).inflate(R.layout.item_grade, parent, false)
            )
        }


    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return if (list[position] is GradeDivider)
            VIEW_TYPE_DIVIDER
        else
            VIEW_TYPE_NORMAL
    }
}