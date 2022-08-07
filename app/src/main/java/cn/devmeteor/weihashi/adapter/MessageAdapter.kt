package cn.devmeteor.weihashi.adapter

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.activity.MessageDetailActivity
import cn.devmeteor.weihashi.base.BaseAdapter
import cn.devmeteor.weihashi.databinding.ItemMessageBinding
import cn.devmeteor.weihashi.model.Message

class MessageAdapter(
    context: Context,
    list: ArrayList<Message>
) :
    BaseAdapter<Message, ItemMessageBinding>(context, R.layout.item_message, list) {

    override fun onBindViewHolder(holder: VH, position: Int) {
        val binding: ItemMessageBinding = DataBindingUtil.bind(holder.itemView)!!
        binding.message = data[position]
        holder.itemView.setOnClickListener {
            context.startActivity(Intent(context, MessageDetailActivity::class.java).apply {
                putExtra(MessageDetailActivity.PARAM_MESSAGE, binding.message)
            })
        }
        binding.executePendingBindings()
    }

}