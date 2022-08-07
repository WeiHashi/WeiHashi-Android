package cn.devmeteor.weihashi.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import cn.devmeteor.weihashi.R

object NotificationUtil {
    private const val DEFAULT_ICON = R.mipmap.ic_launcher
    private const val DEFAULT_TITLE = "微哈师"
    private const val DEFAULT_AUTO_CANCEL = true
    private const val DEFAULT_NOTIFICATION_ID = 1
    private fun getDefaultChannelId(context: Context): String {
        return context.packageName
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getDefaultNotificationChannel(context: Context): NotificationChannel {
        val channel =
            NotificationChannel(context.packageName, "WEIHASHI", NotificationManager.IMPORTANCE_LOW)
        channel.enableLights(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        channel.setShowBadge(true)
        return channel
    }

    fun sendNotification(
        context: Context,
        content: String?,
        pendingIntent: PendingIntent?,
        id: Int,
        channel: NotificationChannel?
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = Notification.Builder(context)
        builder.setSmallIcon(DEFAULT_ICON)
        builder.setContentTitle(DEFAULT_TITLE)
        builder.setContentText(content)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(DEFAULT_AUTO_CANCEL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(getDefaultChannelId(context))
            notificationManager.createNotificationChannel(channel!!)
        }
        notificationManager.notify(id, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendNotification(context: Context, title: String?, content: String?) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = Notification.Builder(context, getDefaultChannelId(context))
        builder.setSmallIcon(DEFAULT_ICON)
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setAutoCancel(DEFAULT_AUTO_CANCEL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(getDefaultChannelId(context))
            notificationManager.createNotificationChannel(getDefaultNotificationChannel(context))
        }
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, builder.build())
    }

    fun sendNotification(
        context: Context,
        title: String?,
        content: String?,
        channel: NotificationChannel?
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = Notification.Builder(context)
        builder.setSmallIcon(DEFAULT_ICON)
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setAutoCancel(DEFAULT_AUTO_CANCEL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(getDefaultChannelId(context))
            notificationManager.createNotificationChannel(channel!!)
        }
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, builder.build())
    }

    fun sendNotification(
        context: Context,
        title: String?,
        content: String?,
        pendingIntent: PendingIntent?
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = Notification.Builder(context)
        builder.setSmallIcon(DEFAULT_ICON)
        builder.setContentTitle(title)
        builder.setContentIntent(pendingIntent)
        builder.setContentText(content)
        builder.setAutoCancel(DEFAULT_AUTO_CANCEL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(getDefaultChannelId(context))
            notificationManager.createNotificationChannel(getDefaultNotificationChannel(context))
        }
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, builder.build())
    }
}