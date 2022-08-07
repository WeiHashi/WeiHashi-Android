package cn.devmeteor.weihashi.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.activity.*
import cn.devmeteor.weihashi.api.ApiException
import cn.devmeteor.weihashi.base.BaseFragment
import cn.devmeteor.weihashi.databinding.FragmentHomeBinding
import cn.devmeteor.weihashi.logd
import cn.devmeteor.weihashi.loge
import cn.devmeteor.weihashi.model.Banner
import cn.devmeteor.weihashi.model.Lesson
import cn.devmeteor.weihashi.utils.AppWidgetUtil
import cn.devmeteor.weihashi.utils.GlideUtil
import cn.devmeteor.weihashi.utils.Util
import cn.devmeteor.weihashi.viewmodel.HomeViewModel
import cn.devmeteor.weihashi.viewmodel.TimetableViewModel
import com.ms.banner.BannerConfig
import com.ms.banner.holder.BannerViewHolder
import com.permissionx.guolindev.PermissionX

class HomeFragment : BaseFragment(R.layout.fragment_home), View.OnClickListener {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by activityViewModels()
    private val timetableVM: TimetableViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            binding = FragmentHomeBinding.bind(this)
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.handleException(this, { handleOtherException(it) }, { handleApiException(it) })
        binding.homeBanner.setPages(viewModel.getBanners(), BannerVH())
            .start().updateBannerStyle(BannerConfig.CUSTOM_INDICATOR)
        viewModel.updateBanners { binding.homeBanner.update(it) }
        timetableVM.initTerm()
        timetableVM.initWeek()
        timetableVM.initLessons()
        timetableVM.updateIndexLessons()
        timetableVM.getIndexLessons().observe(requireActivity(), lessonObserver)
        binding.timetableVM = timetableVM
        binding.homeFunGrade.setOnClickListener(this)
        binding.homeFunKcb.setOnClickListener(this)
        binding.homeFunXl.setOnClickListener(this)
        binding.homeFunMap.setOnClickListener(this)
    }

    private fun handleApiException(apiException: ApiException) {
        loge("handleApiException: $apiException")
        hideLoading()
        showFail(apiException.msg)
    }

    override fun handleOtherException(e: Exception) {
        loge("handleOtherException: ${e.message}")
        hideLoading()
    }

    override fun onResume() {
        super.onResume()
        binding.homeBanner.startAutoPlay()
    }

    override fun onPause() {
        super.onPause()
        binding.homeBanner.stopAutoPlay()
    }


    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }


    private inner class BannerVH : BannerViewHolder<Banner> {

        @SuppressLint("InflateParams")
        override fun createView(context: Context, position: Int, data: Banner?): View {
            viewModel.handleException(this@HomeFragment, { handleOtherException(it) })
            val view = LayoutInflater.from(context).inflate(R.layout.item_banner, null)
            val img: ImageView = view.findViewById(R.id.banner_img)
            if (data == null || data.url.isBlank()) {
                img.setImageResource(R.drawable.banner_placeholder)
            } else {
                GlideUtil.loadBanner(requireContext(), data.url, img)
                if (data.content_id.isNotBlank()) {
                    img.setOnClickListener {
                        showLoading()
                        viewModel.getBannerContent(data.content_id) {
                            hideLoading()
                            go(MessageDetailActivity::class.java, Bundle().apply {
                                putParcelable(MessageDetailActivity.PARAM_MESSAGE, it)
                            })
                        }
                    }
                }
            }
            return view
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.home_fun_grade -> go(GradeActivity::class.java)
            R.id.home_fun_kcb -> go(TimetableActivity::class.java)
            R.id.home_fun_xl -> go(XiaoLiActivity::class.java)
            R.id.home_fun_map -> checkMapPermission()
        }
    }

    private fun checkMapPermission() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "该权限为APP必需权限，请允许",
                    "授权"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "请前往设置页面开启权限",
                    "确定",
                    "取消"
                )
            }
            .request { all, grantList, _ ->
                logd(all.toString(), grantList.toString())
                if (all) go(MapActivity::class.java)
            }
    }

    private val lessonObserver: Observer<ArrayList<Lesson>> = Observer {
        binding.homeIndexLessons.removeAllViews()
        for (i in it.indices) {
            binding.homeIndexLessons.addView(createIndexLessonItem(it[i], i))
        }
        AppWidgetUtil.sendBroadcast(requireActivity(),Constants.ACTION_LESSON_DATA_CHANGED)
    }

    private fun createIndexLessonItem(lesson: Lesson, position: Int): View =
        LayoutInflater.from(requireContext())
            .inflate(R.layout.item_index_lesson, binding.homeIndexLessons, false).apply {
                setBackgroundColor(if (position % 2 == 0) Color.parseColor("#f2f2f2") else Color.WHITE)
                setOnClickListener {
                    go(TimetableActivity::class.java)
                }
                val name: TextView = findViewById(R.id.index_lesson_name)
                name.text = lesson.name
                val time: TextView = findViewById(R.id.index_lesson_time)
                time.text = Util.getLessonTime(lesson.start, lesson.end)
                val place: TextView = findViewById(R.id.index_lesson_place)
                place.text = lesson.place
                name.viewTreeObserver.addOnDrawListener {
                    name.gravity = if (name.lineCount <= 1) Gravity.CENTER else Gravity.START
                }
                time.viewTreeObserver.addOnDrawListener {
                    time.gravity = if (time.lineCount <= 1) Gravity.CENTER else Gravity.START
                }
                place.viewTreeObserver.addOnDrawListener {
                    place.gravity = if (place.lineCount <= 1) Gravity.CENTER else Gravity.START
                }
            }

}