package cn.devmeteor.weihashi.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.databinding.WidgetDialogBinding

class Dialog(context: Context, private val title: String? = null, closeable: Boolean = true) {

    private val view = LayoutInflater.from(context).inflate(R.layout.widget_dialog, null)
    private val dialog: AlertDialog = AlertDialog.Builder(context)
        .setView(view)
        .setCancelable(true)
        .create()
    var binding: WidgetDialogBinding = WidgetDialogBinding.bind(view)
    private var showListener: ShowListener? = null
    private var hideListener: HideListener? = null


    init {
        setCloseable(closeable)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.dialogClose.setOnClickListener { dismiss() }
        title?.let { binding.dialogTitle.text = title }
    }

    fun dismiss() {
        hideListener?.onHide()
        dialog.dismiss()
    }

    fun setContentView(view: View) {
        binding.dialogWindow.addView(view)
    }

    fun showClose(show: Boolean) {
        binding.dialogClose.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun show() {
        showListener?.onShow()
        dialog.show()
    }

    fun setTitle(title: String) {
        binding.dialogTitle.text = title
    }

    fun setCloseable(closeable: Boolean) {
        showClose(closeable)
        dialog.setCancelable(closeable)
    }

    interface ShowListener {
        fun onShow()
    }

    interface HideListener {
        fun onHide()
    }

    fun isShowing(): Boolean = dialog.isShowing

    fun getDialog(): AlertDialog = dialog

}