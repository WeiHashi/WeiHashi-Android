package cn.devmeteor.weihashi.viewmodel

import androidx.lifecycle.MutableLiveData
import cn.devmeteor.weihashi.api.Repository
import cn.devmeteor.weihashi.base.BaseViewModel
import cn.devmeteor.weihashi.model.Message
import kotlin.math.ceil

class MessageViewModel : BaseViewModel() {

    private val repo = Repository()
    private val currentPage = MutableLiveData(1)
    private var totalPage = MutableLiveData(1)
    private val messageList = MutableLiveData(ArrayList<Message>())

    fun refresh() {
        currentPage.value = 1
        handleResult {
            repo.getMessageP1().apply {
                messageList.value?.clear()
                totalPage.value = ceil(data.total / 10.0).toInt()
                data.tops.forEach {
                    try {
                        it.cate.toInt()
                        messageList.value?.add(Message.fromNotice(it, true))
                    } catch (e: Exception) {
                    }
                }
                data.normals.forEach {
                    try {
                        it.cate.toInt()
                        messageList.value?.add(Message.fromNotice(it, false))
                    } catch (e: Exception) {
                    }
                }
                messageList.postValue(messageList.value)
            }
        }
    }

    fun loadMore() {
        currentPage.value = currentPage.value!! + 1
        handleResult {
            repo.getMoreMessages(currentPage.value!!).data.forEach {
                try {
                    it.cate.toInt()
                    messageList.value?.add(Message.fromNotice(it, false))
                } catch (e: Exception) {
                }
            }
            messageList.postValue(messageList.value)
        }
    }

    fun noMoreMessage() = currentPage.value == totalPage.value

    fun getMessageList() = messageList

}