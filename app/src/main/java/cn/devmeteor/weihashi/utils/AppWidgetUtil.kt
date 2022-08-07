package cn.devmeteor.weihashi.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import cn.devmeteor.weihashi.logd

object AppWidgetUtil {

    fun updateWidget(context: Context, clz: Class<*>, views: RemoteViews) {
        val awm = AppWidgetManager.getInstance(context.applicationContext)
        val widgetId = awm.getAppWidgetIds(ComponentName(context, clz))
        awm.updateAppWidget(widgetId, views)
    }

    fun sendBroadcast(context: Context, action: String, params: Bundle? = null) {
        logd("sendWidgetBroadcast:$action")
        context.sendBroadcast(Intent(action).apply {
            `package` = context.packageName
            if (params != null) {
                putExtras(params)
            }
        })
    }

    fun getBroadcastIntent(context: Context, action: String): PendingIntent =
            PendingIntent.getBroadcast(context, 0, Intent(action).apply {
                `package` = context.packageName
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }, with(PendingIntent.FLAG_UPDATE_CURRENT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    or(PendingIntent.FLAG_IMMUTABLE)
                } else {
                    this
                }
            })


    fun getActivityIntent(context: Context, clz: Class<*>): PendingIntent =
            PendingIntent.getActivity(context, 0, Intent(context, clz).apply {
                `package` = context.packageName
            }, with(PendingIntent.FLAG_UPDATE_CURRENT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    or(PendingIntent.FLAG_IMMUTABLE)
                } else {
                    this
                }
            })

}