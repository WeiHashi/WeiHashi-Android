package cn.devmeteor.weihashi.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.R
import cn.devmeteor.weihashi.logd
import cn.devmeteor.weihashi.widget.map.WalkingRouteOverlay
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.*
import com.baidu.mapapi.utils.SpatialRelationUtil
import kotlin.math.abs

class MyLocationHelper(val context: Context, val map: BaiduMap) : SensorEventListener {

    private var mLastX = 0f
    private var mDirection = 0f
    private var mLatitude = 0.0
    private var mLongitude = 0.0
    private var mAccuracy = 0f

    private val locationClient = LocationClient(context).apply {
        locOption = LocationClientOption().apply {
            setCoorType("bd09ll")
            scanSpan = 1000
        }
        registerLocationListener(MyLocationListener())
    }

    private val routeSearch by lazy { RoutePlanSearch.newInstance() }

    init {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI
        )
        locationClient.start()
    }

    override fun onSensorChanged(event: SensorEvent) {
        mDirection = event.values[SensorManager.DATA_X]
        if (abs(mDirection - mLastX) > 1.0) {
            val data = MyLocationData.Builder()
                    .accuracy(mAccuracy)
                    .direction(mDirection)
                    .latitude(mLatitude)
                    .longitude(mLongitude)
                    .build()
            map.setMyLocationData(data)
        }
        mLastX = mDirection
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private inner class MyLocationListener : BDAbstractLocationListener() {

        var initial = true

        override fun onReceiveLocation(location: BDLocation?) {
            if (location != null) {
                location.apply {
                    mAccuracy = radius
                    mLatitude = latitude
                    mLongitude = longitude
                }
                if (initial) {
                    initPosition()
                    initial = false
                }
                val data = MyLocationData.Builder()
                        .direction(mDirection)
                        .accuracy(location.radius)
                        .latitude(location.latitude)
                        .longitude(location.longitude).build()
                map.setMyLocationData(data)
            }
        }
    }

    fun initPosition() {
        if (inNorthArea() || inSouthArea()) {
            map.setMapStatus(MapStatusUpdateFactory.newLatLng(LatLng(mLatitude, mLongitude)))
            return
        }
        map.setMapStatus(MapStatusUpdateFactory
                        .newLatLng(LatLng(45.875941, 126.56618))
        )
    }


    private fun inNorthArea(): Boolean {
        val borderPoints = arrayListOf(
                LatLng(45.866681, 126.557325),
                LatLng(45.870697, 126.559624),
                LatLng(45.8777, 126.561852),
                LatLng(45.877901, 126.574608),
                LatLng(45.867258, 126.570332)
        )
        return SpatialRelationUtil.isPolygonContainsPoint(borderPoints, LatLng(mLatitude, mLongitude))
    }

    private fun inSouthArea(): Boolean {
        val borderPoints = arrayListOf(
                LatLng(45.72905, 126.62697),
                LatLng(45.735404, 126.631534),
                LatLng(45.733932, 126.635576),
                LatLng(45.727502, 126.631318)
        )
        return SpatialRelationUtil.isPolygonContainsPoint(
                borderPoints,
                LatLng(mLatitude, mLongitude)
        )
    }

    fun onDestroy() {
        locationClient.stop()
        routeSearch.destroy()
    }

    fun changePosition(lng: Double, lat: Double) {
        if (lng == -1.0 || lat == -1.0) {
            return
        }
        val point = LatLng(lat, lng)
        map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(with(MapStatus.Builder()) {
            target(point)
            zoom(Constants.MAP_ZOOM)
            build()
        }))
    }

    fun clearOverlays() =
            map.clear()

    fun searchRoute(lat: Double, lng: Double) {
        routeSearch.setOnGetRoutePlanResultListener(OnRouteResultListener(map,LatLng(lat,lng)))
        routeSearch.walkingSearch(
                WalkingRoutePlanOption()
                        .from(PlanNode.withLocation(LatLng(mLatitude, mLongitude)))
                        .to(PlanNode.withLocation(LatLng(lat, lng)))
        )
    }

    private class OnRouteResultListener(private val map: BaiduMap,
                                private val destination: LatLng) : OnGetRoutePlanResultListener {

        override fun onGetWalkingRouteResult(result: WalkingRouteResult) {
            val overlay = WalkingRouteOverlay(map)
            if (result.routeLines != null && result.routeLines.isNotEmpty()) {
                logd("TAG", "onGetWalkingRouteResult: ${result.routeLines.size}")
                overlay.setData(result.routeLines[0])
                overlay.addToMap()
                return
            }
            map.addOverlay(MarkerOptions().apply {
                position(destination)
                icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker))
            })
        }

        override fun onGetTransitRouteResult(p0: TransitRouteResult?) {
        }

        override fun onGetMassTransitRouteResult(p0: MassTransitRouteResult?) {
        }

        override fun onGetDrivingRouteResult(p0: DrivingRouteResult?) {
        }

        override fun onGetIndoorRouteResult(p0: IndoorRouteResult?) {
        }

        override fun onGetBikingRouteResult(p0: BikingRouteResult?) {
        }
    }

}