package cn.devmeteor.weihashi.activity

import android.view.Menu
import android.view.MenuItem
import androidx.core.os.bundleOf
import cn.devmeteor.treepicker.TreePicker
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.base.BaseActionBarActivity
import cn.devmeteor.weihashi.databinding.ActivityMapBinding
import cn.devmeteor.weihashi.inflate
import cn.devmeteor.weihashi.model.LngLatNode
import cn.devmeteor.weihashi.utils.MyLocationHelper
import cn.devmeteor.weihashi.utils.PoiManager
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationConfiguration
import com.lkdont.widget.BottomDrawerContainer

class MapActivity : BaseActionBarActivity(), TreePicker.OnPickListener,
    BottomDrawerContainer.ContentStateListener {

    val binding: ActivityMapBinding by inflate()
    private val drawerLocation by lazy { IntArray(2) }
    private val myLocationHelper: MyLocationHelper by lazy { MyLocationHelper(this, map) }
    private val map: BaiduMap by lazy {
        binding.map.map.apply {
            isMyLocationEnabled = true
            setViewPadding(0, 0, 0, resources.getDimensionPixelSize(R.dimen.height_closed))
            setMyLocationConfiguration(
                MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.NORMAL,
                    true,
                    null
                )
            )
            setMapStatus(MapStatusUpdateFactory.newMapStatus(with(MapStatus.Builder()) {
                zoom(Constants.MAP_ZOOM)
                build()
            }))
            uiSettings.apply {
                isCompassEnabled = true
                isRotateGesturesEnabled = false
                setEnlargeCenterWithDoubleClickEnable(true)
            }
        }
    }
    private var mapData:LngLatNode?=null

    override fun init() {
        setSupportActionBar("校园地图", binding)
        binding.map.showZoomControls(false)
        myLocationHelper.initPosition()
        mapData=PoiManager.getPoiTree(this)
        binding.picker.setData(mapData!!)
        binding.picker.setOnPickListener(this)
        binding.drawerContainer.setContentStateListener(this)
        binding.drawerContainer.setGrip(findViewById(R.id.grip))
        binding.backMyLocation.setOnClickListener {
            myLocationHelper.clearOverlays()
            myLocationHelper.initPosition()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        myLocationHelper.onDestroy()
        map.isMyLocationEnabled = false
        binding.map.onDestroy()
    }

    override fun onPick(node: Int, parents: List<Int>, isFinalPick: Boolean) {
        var selected = mapData!!
        var parentNodes = ""
        for (i in parents) {
            selected = selected.next!![i]
            parentNodes += selected
        }
        selected = selected.next!![node]
        if (isFinalPick) {
            binding.drawerContainer.closeDrawer()
            myLocationHelper.clearOverlays()
            myLocationHelper.searchRoute(selected.lat, selected.lng)
            myLocationHelper.changePosition(selected.lng, selected.lat)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerContainer.isOpened()) {
            binding.drawerContainer.closeDrawer()
            return
        }
        super.onBackPressed()
    }

    override fun contentOnTop(): Boolean = binding.picker.isListOnTop()

    override fun contentOnBottom(): Boolean = binding.picker.isListOnBottom()
    override fun contentRawY(): Float {
        binding.bottomDrawer.getLocationInWindow(drawerLocation)
        return drawerLocation[1].toFloat()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.map_report_poi) {
            go(WebActivity::class.java, bundleOf(WebActivity.PARAM_URL to Constants.POI_REPORT_URL))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}