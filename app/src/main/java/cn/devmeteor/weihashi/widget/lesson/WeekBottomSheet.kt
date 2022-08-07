package cn.devmeteor.weihashi.widget.lesson

import android.content.Context
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet


class WeekBottomSheet(
    context: Context,
    totalWeek: Int,
    listener: QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener
) {
    private var sheet = QMUIBottomSheet.BottomListSheetBuilder(context)
        .apply {
            for (i in 1..totalWeek) {
                addItem("第${i}周", i.toString())
            }
        }
        .setTitle("选择教学周")
        .setGravityCenter(true)
        .setOnSheetItemClickListener(listener)
        .setAddCancelBtn(true)
        .setAllowDrag(true)
        .setSkinManager(QMUISkinManager.defaultInstance(context))
        .build()


    fun show() {
        sheet.show()
    }
}