package cn.devmeteor.weihashi.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.adapter.GradeAdapter
import cn.devmeteor.weihashi.api.ApiException
import cn.devmeteor.weihashi.base.BaseActionBarActivity
import cn.devmeteor.weihashi.databinding.ActivityGradeBinding
import cn.devmeteor.weihashi.databinding.ViewDialogGradeInfoBinding
import cn.devmeteor.weihashi.inflate
import cn.devmeteor.weihashi.viewmodel.GradeViewModel
import cn.devmeteor.weihashi.viewmodel.UserViewModel
import cn.devmeteor.weihashi.widget.Dialog
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import java.net.ConnectException
import java.net.SocketTimeoutException

class GradeActivity : BaseActionBarActivity(), OnRefreshListener {

    private val binding: ActivityGradeBinding by inflate()
    private val gradeVM: GradeViewModel by viewModels()
    private val userVM: UserViewModel by viewModels()
    private val gradeInfoDialog: Dialog by lazy { Dialog(this, "成绩信息") }

    override fun init(): Unit = with(binding) {
        setSupportActionBar("我的成绩", binding)
        lifecycleOwner = this@GradeActivity
        gradeVM.handleException(this@GradeActivity,
                { handleOtherException(it) },
                { handleApiException(it) })
        binding.vm = gradeVM
        userVM.updateJwInfo()
        val gradeAdapter = GradeAdapter(this@GradeActivity, gradeVM.getCjs().value!!)
        gradeList.layoutManager = LinearLayoutManager(this@GradeActivity)
        gradeList.adapter = gradeAdapter
        gradeRefresh.setOnRefreshListener(this@GradeActivity)
        initGradeInfoDialog()
        gradeVM.getCjs().observe(this@GradeActivity, { gradeAdapter.notifyDataSetChanged() })
        gradeVM.initData()
    }

    private fun initGradeInfoDialog() {
        val contentView = LayoutInflater.from(this).inflate(R.layout.view_dialog_grade_info, null)
        val binding = DataBindingUtil.bind<ViewDialogGradeInfoBinding>(contentView)!!
        binding.lifecycleOwner = this
        binding.vm = gradeVM
        gradeInfoDialog.setContentView(contentView)
    }

    private fun handleApiException(e: ApiException) {
        if (binding.gradeRefresh.isRefreshing)
            binding.gradeRefresh.finishRefresh(false)
        showFail(e.msg)
    }

    private fun handleOtherException(e: Exception) {
        if (binding.gradeRefresh.isRefreshing)
            binding.gradeRefresh.finishRefresh(false)
        if (e is SocketTimeoutException || e is ConnectException) {
            toastLong("连接服务器失败")
            return
        }
        toastLong(e.toString())
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        if (!userVM.isJwBound().value!!) {
            refreshLayout.finishRefresh(false)
            showFail("请先绑定教务平台")
            Handler(Looper.getMainLooper()).postDelayed({
                goAndFinish(
                        MainActivity::class.java,
                        Bundle().apply { putInt(MainActivity.EXTRA_SPECIFIC_ITEM, 2) }
                )
            }, 1500)
            return
        }
        gradeVM.updateGrade().invokeOnCompletion {
            refreshLayout.finishRefresh(it == null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_grade, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.grade_info_btn) {
            gradeInfoDialog.show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}