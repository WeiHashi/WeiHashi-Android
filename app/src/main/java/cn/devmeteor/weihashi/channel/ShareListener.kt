package cn.devmeteor.weihashi.channel

interface ShareListener {
    fun onSuccess(channel: String)
    fun onFail(channel: String, reason: String)
    fun onCancel()
}