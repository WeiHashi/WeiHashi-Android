package cn.devmeteor.weihashi.widget.lesson

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.utils.Util
import com.github.gzuliyujiang.basepicker.BottomDialog
import com.github.gzuliyujiang.wheelview.widget.WheelView

class PartPicker(activity: Activity) : BottomDialog(activity) {

    private lateinit var weekdayPicker: WheelView
    private lateinit var startPicker: WheelView
    private lateinit var endPicker: WheelView

    private var onPartPickedListener: OnPartPickedListener? = null

    override fun createContentView(activity: Activity): View =
        LayoutInflater.from(activity).inflate(R.layout.view_part_picker, null)

    override fun initView(contentView: View) {
        super.initView(contentView)
        weekdayPicker = contentView.findViewById(R.id.part_picker_weekday)
        startPicker = contentView.findViewById(R.id.part_picker_start)
        endPicker = contentView.findViewById(R.id.part_picker_end)
        contentView.findViewById<View>(R.id.wheel_picker_address_cancel)
            .setOnClickListener { dismiss() }
        contentView.findViewById<View>(R.id.wheel_picker_address_confirm)
            .setOnClickListener {
                onPartPickedListener?.onPartPicked(
                    weekdayPicker.currentPosition,
                    startPicker.currentPosition,
                    endPicker.currentPosition
                )
                dismiss()
            }
    }

    fun setData(weekList: List<String>) {
        weekdayPicker.data = Util.weekdays.asList()
        startPicker.data = weekList
        endPicker.data = weekList
    }

    fun setDefault(weekdayIndex: Int, startIndex: Int, endIndex: Int) {
        weekdayPicker.setDefaultPosition(weekdayIndex)
        startPicker.setDefaultPosition(startIndex)
        endPicker.setDefaultPosition(endIndex)
    }

    interface OnPartPickedListener {
        fun onPartPicked(weekdayIndex: Int, startIndex: Int, endIndex: Int)
    }


    fun setOnPartPickListener(onPartPickedListener: OnPartPickedListener) {
        this.onPartPickedListener = onPartPickedListener
    }
}