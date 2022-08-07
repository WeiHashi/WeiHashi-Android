package cn.devmeteor.weihashi.activity

import android.graphics.drawable.Drawable
import android.widget.ImageView
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.base.BaseActionBarActivity
import cn.devmeteor.weihashi.databinding.ActivityXiaoLiBinding
import cn.devmeteor.weihashi.inflate
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.wanglu.photoviewerlibrary.PhotoViewer

class XiaoLiActivity : BaseActionBarActivity() {

    private val binding: ActivityXiaoLiBinding by inflate()
    private var loaded = false

    override fun init() {
        setSupportActionBar("校历", binding)
        loadImage()
        binding.xiaoLiImg.setOnClickListener {
            if (!loaded) {
                loadImage()
                return@setOnClickListener
            }
            PhotoViewer.setClickSingleImg(
                Constants.XIAO_LI_URL,
                binding.xiaoLiImg
            ).setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
                override fun show(iv: ImageView, url: String) {
                    iv.setImageDrawable(binding.xiaoLiImg.drawable)
                }
            }).setOnPhotoViewerCreatedListener {
                QMUIStatusBarHelper.setStatusBarDarkMode(this)
            }.setOnPhotoViewerDestroyListener {
                QMUIStatusBarHelper.setStatusBarLightMode(this)
            }.start(this)
        }
    }

    private fun loadImage() {
        showLoading("加载中……")
        Glide.with(this)
            .load(Constants.XIAO_LI_URL)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.banner_placeholder)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    hideLoading()
                    showFail("加载失败")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    hideLoading()
                    loaded = true
                    return false
                }
            })
            .into(binding.xiaoLiImg)
    }



}