package cn.devmeteor.weihashi.widget.share

import android.content.Context
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.channel.*
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet

class ShareBottomSheet(
    val context: Context,
    val listener: QMUIBottomSheet.BottomGridSheetBuilder.OnSheetItemClickListener
) {
    private val sheet = QMUIBottomSheet.BottomGridSheetBuilder(context)
        .addItem(
            R.drawable.share_qq,
            "QQ",
            QQ.CHANNEL_QQ,
            QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE
        )
        .addItem(
            R.drawable.share_qzone,
            "QQ空间",
            Qzone.CHANNEL_QZONE,
            QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE
        )
        .addItem(
            R.drawable.share_wechat,
            "微信",
            WeiXin.CHANNEL_WEIXIN,
            QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE
        )
        .addItem(
            R.drawable.share_pyq,
            "朋友圈",
            WxTimeline.CHANNEL_WX_TIMELINE,
            QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE
        )
        .addItem(
            R.drawable.share_weibo,
            "微博",
            Weibo.CHANNEL_WEIBO,
            QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE
        )
        .setTitle("分享到")
        .setOnSheetItemClickListener(listener)
        .setAddCancelBtn(true)
        .setSkinManager(QMUISkinManager.defaultInstance(context))
        .build()


    fun show() {
        sheet.show()
    }
}