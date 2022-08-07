package cn.devmeteor.weihashi.base

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.devmeteor.weihashi.Constants
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog

abstract class BaseActivity : AppCompatActivity() {

    private var loadingDialog: QMUITipDialog? = null
    private var successDialog: QMUITipDialog? = null
    private var failDialog: QMUITipDialog? = null
    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QMUIStatusBarHelper.translucent(this)
        QMUIStatusBarHelper.setStatusBarLightMode(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        init()
    }


    abstract fun init()

    fun go(clazz: Class<*>, args: Bundle? = null) {
        val intent = Intent(this, clazz)
        if (args != null) {
            intent.putExtras(args)
        }
        startActivity(intent)
    }

    fun goAndFinish(clazz: Class<*>, args: Bundle? = null) {
        go(clazz, args)
        finish()
    }

    fun toastShort(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    fun showLoading(text: String = "加载中...") {
        loadingDialog = QMUITipDialog.Builder(this)
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
        val successDialog = QMUITipDialog.Builder(this)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
            .setTipWord(text)
            .create()
        successDialog.show()
        handler.postDelayed({
            successDialog.dismiss()
        }, Constants.TOAST_LENGTH)
    }

    fun showFail(text: String) {
        val failDialog = QMUITipDialog.Builder(this)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
            .setTipWord(text)
            .create()
        failDialog.show()
        handler.postDelayed({
            failDialog.dismiss()
        }, Constants.TOAST_LENGTH)
    }

    fun showInfo(text: String) {
        val infoDialog = QMUITipDialog.Builder(this)
            .setIconType(QMUITipDialog.Builder.ICON_TYPE_NOTHING)
            .setTipWord(text)
            .create()
        infoDialog.show()
        handler.postDelayed({
            infoDialog.dismiss()
        }, Constants.TOAST_LENGTH)
    }

    fun showDialog(
        title: String,
        msg: String,
        listener: QMUIDialogAction.ActionListener,
        cancelable: Boolean = true
    ) {
        val dialog = QMUIDialog.MessageDialogBuilder(this)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(cancelable)
            .setCanceledOnTouchOutside(cancelable)
            .addAction("确定", listener)
            .apply {
                if (cancelable) {
                    addAction(0,"取消",QMUIDialogAction.ACTION_PROP_NEUTRAL, listener)
                }
            }
            .create()
        dialog.show()
    }

}