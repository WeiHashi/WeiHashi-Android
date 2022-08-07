package cn.devmeteor.weihashi.activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import cn.devmeteor.weihashi.base.BaseActionBarActivity
import cn.devmeteor.weihashi.databinding.ActivityMessageDetailBinding
import cn.devmeteor.weihashi.inflate
import cn.devmeteor.weihashi.model.Message
import cn.devmeteor.weihashi.utils.GlideUtil
import cn.devmeteor.weihashi.utils.Util
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.RegexUtils
import com.google.zxing.Result
import com.mylhyl.zxing.scanner.decode.QRDecode
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.wanglu.photoviewerlibrary.OnLongClickListener
import com.wanglu.photoviewerlibrary.PhotoViewer
import org.json.JSONArray
import org.json.JSONObject

class MessageDetailActivity : BaseActionBarActivity() {

    companion object {
        const val PARAM_MESSAGE = "message"
    }

    private val binding: ActivityMessageDetailBinding by inflate()
    private val imageUrls = ArrayList<String>()

    override fun init() {
        setSupportActionBar("消息详情", binding)
        binding.lifecycleOwner = this
        val message: Message = intent.getParcelableExtra(PARAM_MESSAGE)!!
        binding.message = message
        JSONArray(message.detail).apply {
            for (i in 0 until length()) {
                handleDetailItem(getJSONObject(i))
            }
        }
    }

    private fun handleDetailItem(obj: JSONObject) {
        val content = obj.getString("content")
        binding.messageContainer.addView(
            when (obj.getInt("type")) {
                1 -> createParagraph(content)
                2 -> {
                    imageUrls.add(content)
                    createImage(content)
                }
                else -> null
            }
        )
    }

    @SuppressLint("SetTextI18n")
    private fun createParagraph(text: String): TextView = TextView(this).apply {
        setText(text)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                0,
                QMUIDisplayHelper.dp2px(this@MessageDetailActivity, 5),
                0,
                QMUIDisplayHelper.dp2px(this@MessageDetailActivity, 5)
            )
        }
        layoutParams = lp
    }

    private fun createImage(url: String): ImageView = ImageView(this).apply {
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                0,
                QMUIDisplayHelper.dp2px(this@MessageDetailActivity, 5),
                0,
                QMUIDisplayHelper.dp2px(this@MessageDetailActivity, 5)
            )
        }
        layoutParams = lp
        GlideUtil.loadMessageImage(this@MessageDetailActivity, url, this)
        setOnClickListener {
            PhotoViewer.setClickSingleImg(
                url,
                this
            ).setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
                override fun show(iv: ImageView, url: String) {
                    iv.setImageDrawable(drawable)
                }
            }).setOnLongClickListener(object : OnLongClickListener {
                override fun onLongClick(view: View) {
                    QRDecode.decodeQR(
                        ImageUtils.drawable2Bitmap(drawable)
                    ) { rawResult, _, _ ->
                        showImageOperations(rawResult, drawable)
                    }
                }
            }).setOnPhotoViewerCreatedListener {
                QMUIStatusBarHelper.setStatusBarDarkMode(this@MessageDetailActivity)
            }.setOnPhotoViewerDestroyListener {
                QMUIStatusBarHelper.setStatusBarLightMode(this@MessageDetailActivity)
            }.start(this@MessageDetailActivity)
        }
    }

    private fun showImageOperations(
        rawResult: Result?,
        drawable: Drawable
    ) {
        QMUIBottomSheet.BottomListSheetBuilder(this@MessageDetailActivity).apply {
            if (rawResult != null) {
                addItem("识别图中的二维码")
            }
            addItem("保存到本地")
            setOnSheetItemClickListener { dialog, _, _, tag ->
                when (tag) {
                    "识别图中的二维码" -> handleQRCodeResult(rawResult!!.text)
                    "保存到本地" -> handleSaveImage(drawable)
                }
                dialog.cancel()
            }
            setAddCancelBtn(true)
            build().show()
        }
    }

    private fun handleSaveImage(drawable: Drawable) {
        val file =
            ImageUtils.save2Album(ImageUtils.drawable2Bitmap(drawable), Bitmap.CompressFormat.PNG)
        if (file != null && file.exists()) {
            toastShort("已保存")
        } else {
            toastLong("保存失败")
        }
    }

    private fun handleQRCodeResult(content: String) =
        if (RegexUtils.isURL(content)) {
            if (content.contains(".html")
                || content.contains(".php")
                || content.contains(".jsp")
                || content.contains(".asp")
                || content.contains(".do")
                || content.endsWith("/")
                || !content.contains(Regex("\\S+/\\S+"))
            ) {
                go(
                    WebActivity::class.java, bundleOf(
                        WebActivity.PARAM_URL to content
                    )
                )
            } else {
                Util.loadUrlWithBrowser(this@MessageDetailActivity, content)
            }
        } else {
            ClipboardUtils.copyText(content)
            toastLong("识别结果已复制到剪贴板")
        }



}