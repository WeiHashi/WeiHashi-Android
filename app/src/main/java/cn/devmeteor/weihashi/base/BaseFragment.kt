package cn.devmeteor.weihashi.base

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import cn.devmeteor.weihashi.Constants
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog

abstract class BaseFragment(private val vid: Int) : Fragment() {

    private var loadingDialog: QMUITipDialog? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(vid, null)
    }

    fun go(clazz: Class<*>, args: Bundle? = null) {
        val intent = Intent(activity, clazz)
        if (args != null) {
            intent.putExtras(args)
        }
        startActivity(intent)
    }

    fun showLoading(text: String = "加载中...") {
        loadingDialog = QMUITipDialog.Builder(requireContext())
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
            .setTipWord(text)
            .create()
        loadingDialog?.show()
    }

    fun hideLoading() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    fun showSuccess(text: String) {
        val successDialog = QMUITipDialog.Builder(requireContext())
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
            .setTipWord(text)
            .create()
        successDialog.show()
        handler.postDelayed({
            successDialog.dismiss()
        }, Constants.TOAST_LENGTH)
    }

    fun showFail(text: String) {
        val failDialog = QMUITipDialog.Builder(requireContext())
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
            .setTipWord(text)
            .create()
        failDialog.show()
        handler.postDelayed({
            failDialog.dismiss()
        }, Constants.TOAST_LENGTH)
    }

    fun showInfo(text: String) {
        val infoDialog = QMUITipDialog.Builder(requireContext())
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_NOTHING)
            .setTipWord(text)
            .create()
        infoDialog.show()
        handler.postDelayed({
            infoDialog.dismiss()
        }, Constants.TOAST_LENGTH)
    }

    fun toastShort(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }

    abstract fun handleOtherException(e: Exception)

    fun runOnUiThread(action: Runnable){
        requireActivity().runOnUiThread(action)
    }

    fun showDialog(title: String, msg: String, listener: QMUIDialogAction.ActionListener, cancelable: Boolean = true) {
        QMUIDialog.MessageDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(msg)
            .addAction("确定",listener)
            .apply {
                if (cancelable){
                    addAction("取消",listener)
                }
            }
            .create()
            .show()
    }

}