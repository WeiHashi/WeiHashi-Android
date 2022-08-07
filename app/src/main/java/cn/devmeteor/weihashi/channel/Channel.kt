package cn.devmeteor.weihashi.channel

import android.app.Activity

interface Channel {
    fun share(activity: Activity, shareContent: ShareContent, shareListener: ShareListener)
    fun getName(): String
}