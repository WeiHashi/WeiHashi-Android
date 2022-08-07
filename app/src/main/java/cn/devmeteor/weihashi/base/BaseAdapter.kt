package cn.devmeteor.weihashi.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.databinding.ItemMessageBinding

abstract class BaseAdapter<T, B : ViewBinding>(
    val context: Context,
    private val id: Int,
    val data: List<T>
) :
    RecyclerView.Adapter<BaseAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding: B = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            id,
            parent,
            false
        )
        return VH(binding.root)
    }

    override fun getItemCount(): Int = data.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView)
}