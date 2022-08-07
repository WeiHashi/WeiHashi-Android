package cn.devmeteor.weihashi.base

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import cn.devmeteor.weihashi.R

abstract class BaseActionBarActivity : BaseActivity() {

    private lateinit var toolbar: Toolbar

    protected fun setSupportActionBar(title: String, binding: ViewBinding) {
        toolbar = binding.root.findViewById(R.id.toolbar)
        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected fun setActionBarIcon(resId: Int) {
        toolbar.setNavigationIcon(resId)
    }

    protected fun setActionBarTitle(title: String) {
        toolbar.title = title
    }

    protected fun setSupportArrowActionBar(isShow: Boolean) =
        supportActionBar?.setDisplayHomeAsUpEnabled(isShow)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}