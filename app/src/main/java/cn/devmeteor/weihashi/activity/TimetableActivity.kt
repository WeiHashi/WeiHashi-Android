package cn.devmeteor.weihashi.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import cn.devmeteor.tableview.LessonView
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.api.ApiException
import cn.devmeteor.weihashi.base.BaseActivity
import cn.devmeteor.weihashi.databinding.ActivityTimetableBinding
import cn.devmeteor.weihashi.databinding.ViewDialogAddLessonBinding
import cn.devmeteor.weihashi.databinding.ViewDialogLessonDetailBinding
import cn.devmeteor.weihashi.inflate
import cn.devmeteor.weihashi.logd
import cn.devmeteor.weihashi.model.Lesson
import cn.devmeteor.weihashi.model.ServerLesson
import cn.devmeteor.weihashi.utils.AppWidgetUtil
import cn.devmeteor.weihashi.utils.Util
import cn.devmeteor.weihashi.utils.Util.kv
import cn.devmeteor.weihashi.viewmodel.TimetableViewModel
import cn.devmeteor.weihashi.viewmodel.UserViewModel
import cn.devmeteor.weihashi.widget.Dialog
import cn.devmeteor.weihashi.widget.lesson.PartPicker
import cn.devmeteor.weihashi.widget.lesson.WeekBottomSheet
import cn.devmeteor.weihashi.widget.lesson.WeekCheckView
import com.hurryyu.bestchooser.ChooserMode
import com.hurryyu.bestchooser.ChooserView
import com.hurryyu.bestchooser.ChooserViewGroupManager
import com.hurryyu.bestchooser.OnChooseChangeListener
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import java.net.ConnectException
import java.net.SocketTimeoutException

class TimetableActivity : BaseActivity(), OnRefreshListener,
    LessonView.LessonClickListener<Lesson>, View.OnClickListener {

    private val binding: ActivityTimetableBinding by inflate()
    private val userVM: UserViewModel by viewModels()
    private val timetableVM: TimetableViewModel by viewModels()
    private val weekBottomSheet: WeekBottomSheet by lazy {
        WeekBottomSheet(
            this,
            timetableVM.getTotalWeek().value!!,
            weekListener
        )
    }
    private val lessonDetailDialog: Dialog by lazy { Dialog(this, "课程详情") }
    private val addLessonDialog: Dialog by lazy { Dialog(this, "添加课程") }

    override fun init() {
        if (!kv.containsKey(Constants.KEY_OPENID)) {
            goAndFinish(LoginActivity::class.java)
            return
        }
        timetableVM.handleException(this, { handleOtherException(it) }, { handleApiException(it) })
        binding.vm = timetableVM
        binding.lifecycleOwner = this
        userVM.updateJwInfo()
        binding.timetableBack.setOnClickListener { finish() }
        binding.timetableRefreshLayout.setOnRefreshListener(this)
        timetableVM.getLessons().observe(this, lessonObserver)
        timetableVM.getShownLessons().observe(this, shownLessonObserver)
        timetableVM.getSelectedWeek().observe(this, weekObserver)
        timetableVM.getCurrentWeek().observe(this, currentWeekObserver)
        timetableVM.getTerm().observe(this, termObserver)
        timetableVM.initWeek()
        timetableVM.initLessons()
        binding.timetableChooseWeek.setOnClickListener {
            if (timetableVM.getLessons().value.isNullOrEmpty()) {
                return@setOnClickListener
            }
            weekBottomSheet.show()
        }
        initDetailDialog()
        initAddLessonDialog()
    }

    private fun initAddLessonDialog() {
        val addLessonView = LayoutInflater.from(this).inflate(R.layout.view_dialog_add_lesson, null)
        val dBinding: ViewDialogAddLessonBinding = DataBindingUtil.bind(addLessonView)!!
        addLessonDialog.setContentView(addLessonView)
        dBinding.addLessonPart.setOnClickListener {
            PartPicker(this).apply {
                setData(Util.parts)
                setDefault(
                    Util.weekdays.indexOf(timetableVM.getAddLessonWeekday().value),
                    timetableVM.getAddLessonStart().value!! - 1,
                    timetableVM.getAddLessonEnd().value!! - 1
                )
                setOnPartPickListener(object : PartPicker.OnPartPickedListener {
                    override fun onPartPicked(weekdayIndex: Int, startIndex: Int, endIndex: Int) {
                        dBinding.addLessonPart.text = timetableVM.updateAddedParams(
                            Util.weekdays[weekdayIndex],
                            startIndex + 1,
                            endIndex + 1
                        )
                    }
                })
            }.show()
        }
        binding.timetableAddLesson.setOnClickListener { addLessonDialog.show() }
        dBinding.addLessonName.addTextChangedListener {
            timetableVM.getAddLessonName().value = it.toString()
        }
        dBinding.addLessonTeacher.addTextChangedListener {
            timetableVM.getAddLessonTeacher().value = it.toString()
        }
        dBinding.addLessonPlace.addTextChangedListener {
            timetableVM.getAddLessonPlace().value = it.toString()
        }
        timetableVM.getTotalWeek().observe(this) {
            dBinding.addLessonWeek.removeAllViews()
            val manager = ChooserViewGroupManager.Builder(ChooserMode.MODE_MULTIPLE).apply {
                val list = Array(it) { WeekCheckView(this@TimetableActivity) }
                for (i in 0 until it) {
                    dBinding.addLessonWeek.addView(WeekCheckView(this@TimetableActivity).apply {
                        setText("${i + 1}")
                        viewTag = "${i + 1}"
                        list[i] = this
                    })
                }
                addChooserView(chooserView = list)
            }.build()
            manager.setOnChooseChangeListener(object : OnChooseChangeListener() {
                override fun onChanged(
                    chooserView: ChooserView,
                    viewTag: String,
                    groupTag: String,
                    isSelected: Boolean
                ) {
                    timetableVM.addOrRemoveSelected(viewTag, isSelected)
                }
            })
            dBinding.addLessonOdd.setOnClickListener {
                dBinding.addLessonWeek.children.forEachIndexed { index, view ->
                    val item = view as WeekCheckView
                    if ((index % 2 == 0 && !item.isSelected) || (index % 2 == 1 && item.isSelected)) {
                        manager.selected(item)
                    }
                }
            }
            dBinding.addLessonEven.setOnClickListener {
                dBinding.addLessonWeek.children.forEachIndexed { index, view ->
                    val item = view as WeekCheckView
                    if ((index % 2 == 1 && !item.isSelected) || (index % 2 == 0 && item.isSelected)) {
                        manager.selected(item)
                    }
                }
            }
            dBinding.addLessonAll.setOnClickListener {
                dBinding.addLessonWeek.children.forEach { view ->
                    val item = view as WeekCheckView
                    if (!item.isSelected) {
                        manager.selected(item)
                    }
                }
            }
            dBinding.addLessonReset.setOnClickListener {
                dBinding.addLessonWeek.children.forEach {
                    val item = it as WeekCheckView
                    if (item.isSelected) {
                        manager.selected(item)
                    }
                }
            }
        }
        dBinding.addLesson.setOnClickListener {
            val checkForm = timetableVM.checkAddForm()
            if (checkForm.isNotBlank()) {
                showFail(checkForm)
                return@setOnClickListener
            }
            when {
                timetableVM.checkSameLesson() != -1 ->
                    showDialog("", "检测到相同课程，新添加的课程将被合并到相同课程，是否继续？", sameListener)
                timetableVM.checkConflict() ->
                    showDialog("课程冲突", "检测到时间冲突，冲突部分将不被添加，是否继续？", conflictListener)
                else ->
                    timetableVM.doAdd(addCallback)
            }
        }
    }

    private fun initDetailDialog() {
        val lessonDetailView =
            LayoutInflater.from(this).inflate(R.layout.view_dialog_lesson_detail, null)
        val lessonDetailBinding: ViewDialogLessonDetailBinding =
            DataBindingUtil.bind(lessonDetailView)!!
        lessonDetailDialog.setContentView(lessonDetailView)
        lessonDetailBinding.vm = timetableVM
        lessonDetailBinding.lifecycleOwner = this
        lessonDetailBinding.lessonDetailDeleteThis.setOnClickListener(this)
        lessonDetailBinding.lessonDetailDeleteThisTime.setOnClickListener(this)
        lessonDetailBinding.lessonDetailDeleteThisLesson.setOnClickListener(this)
        lessonDetailBinding.lessonDetailChangeColor.setOnClickListener(this)
    }

    private fun handleApiException(apiException: ApiException) {
        if (binding.timetableRefreshLayout.isRefreshing)
            binding.timetableRefreshLayout.finishRefresh(false)
        showFail(apiException.msg)
    }

    private fun handleOtherException(exception: Exception) {
        if (binding.timetableRefreshLayout.isRefreshing)
            binding.timetableRefreshLayout.finishRefresh(false)
        if (exception is SocketTimeoutException || exception is ConnectException) {
            toastLong("连接服务器失败")
            return
        }
        toastLong(exception.toString())
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
        if (timetableVM.checkManual()) {
            showDialog("", "检测到手动添加的课程，更新数据将会被清除，是否继续？", manualListener)
            refreshLayout.finishRefresh(false)
            return
        }
        timetableVM.updateLessons().invokeOnCompletion {
            AppWidgetUtil.sendBroadcast(this, Constants.ACTION_LESSON_DATA_CHANGED)
            refreshLayout.finishRefresh(it == null)
        }
    }

    private val lessonObserver: Observer<ArrayList<ServerLesson>> = Observer {
        if (binding.timetableRefreshLayout.isRefreshing)
            binding.timetableRefreshLayout.finishRefresh()
        timetableVM.updateShownLessons()
        timetableVM.updateIndexLessons()
    }


    private val shownLessonObserver: Observer<ArrayList<Lesson>> = Observer {
        binding.timetable.setLessons(it, timetableVM.getBgMap().value, this)
    }

    private val weekObserver: Observer<Int> = Observer {
        binding.timetable.setWeekStart(
            Util.getWeekStart(
                it,
                timetableVM.getTotalWeek().value!!,
                kv.decodeString(Constants.KEY_TERM_START)
            )
        )
        timetableVM.updateShownLessons()
    }

    private val currentWeekObserver: Observer<Int> = Observer {
        timetableVM.getSelectedWeek().value = it
    }

    private val termObserver: Observer<String> = Observer {
        timetableVM.initLessons()
    }

    private val weekListener =
        QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener { dialog, _, _, tag ->
            timetableVM.getSelectedWeek().value = tag.toInt()
            dialog.dismiss()
        }

    override fun onClick(lesson: Lesson) {
        logd(lesson.toString())
        timetableVM.getDetailLesson().value = lesson
        timetableVM.updateDetailWeeks()
        lessonDetailDialog.show()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.lesson_detail_change_color) {
            timetableVM.changeDetailColor()
            return
        }
        showDialog("提示", "是否删除？", { dialog, index ->
            if (index == 0) {
                when (v.id) {
                    R.id.lesson_detail_delete_this -> timetableVM.deleteThis()
                    R.id.lesson_detail_delete_this_time -> timetableVM.deleteThisTime()
                    R.id.lesson_detail_delete_this_lesson -> timetableVM.deleteThisLesson()
                }
                lessonDetailDialog.dismiss()
                showSuccess("已删除")
            }
            dialog.dismiss()
        })
    }

    private val conflictListener = QMUIDialogAction.ActionListener { dialog, index ->
        if (index == 0) {
            timetableVM.doAdd(addCallback)
        }
        dialog?.dismiss()
    }

    private val sameListener = QMUIDialogAction.ActionListener { dialog, index ->
        if (index == 0) {
            if (timetableVM.checkConflict()) {
                showDialog("课程冲突", "检测到时间冲突，冲突部分将不被添加，是否继续？", conflictListener)
            } else {
                timetableVM.doAdd(addCallback)
            }
        }
        dialog?.dismiss()
    }

    private val manualListener = QMUIDialogAction.ActionListener { dialog, index ->
        if (index == 0) {
            showLoading("正在更新数据")
            timetableVM.updateLessons().invokeOnCompletion {
                AppWidgetUtil.sendBroadcast(this, Constants.ACTION_LESSON_DATA_CHANGED)
                hideLoading()
            }
        }
        dialog?.dismiss()
    }

    private val addCallback: () -> Unit = {
        addLessonDialog.dismiss()
        showSuccess("添加成功")
        timetableVM.saveLessons()
    }

}