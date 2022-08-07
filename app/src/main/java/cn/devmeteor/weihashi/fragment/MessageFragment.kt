package cn.devmeteor.weihashi.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.adapter.MessageAdapter
import cn.devmeteor.weihashi.base.BaseFragment
import cn.devmeteor.weihashi.databinding.FragmentMessageBinding
import cn.devmeteor.weihashi.logd
import cn.devmeteor.weihashi.viewmodel.MessageViewModel

class MessageFragment : BaseFragment(R.layout.fragment_message) {

    private val viewModel: MessageViewModel by activityViewModels()
    private lateinit var binding: FragmentMessageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            binding = FragmentMessageBinding.bind(this)
        }
    }

    override fun handleOtherException(e: Exception) {
        logd(e.message)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this
        viewModel.handleException(this, { handleOtherException(it) })
        val messageAdapter = MessageAdapter(requireContext(), viewModel.getMessageList().value!!)
        viewModel.getMessageList().observe(viewLifecycleOwner) { updateListData(messageAdapter) }
        binding.messageList.layoutManager = LinearLayoutManager(activity)
        binding.messageList.adapter = messageAdapter
        binding.messageRefresh.apply {
            setOnRefreshListener { viewModel.refresh() }
            setOnLoadMoreListener { viewModel.loadMore() }
            autoRefresh()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateListData(messageAdapter: MessageAdapter) {
        messageAdapter.notifyDataSetChanged()
        if (binding.messageRefresh.isRefreshing)
            binding.messageRefresh.finishRefresh()
        if (binding.messageRefresh.isLoading) {
            if (viewModel.noMoreMessage())
                binding.messageRefresh.finishLoadMoreWithNoMoreData()
            else
                binding.messageRefresh.finishLoadMore()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MessageFragment()
    }
}