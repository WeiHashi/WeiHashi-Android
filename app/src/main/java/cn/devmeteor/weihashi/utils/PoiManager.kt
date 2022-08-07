package cn.devmeteor.weihashi.utils

import android.content.Context
import cn.devmeteor.weihashi.Constants
import cn.devmeteor.weihashi.api.ApiClient.configApi
import cn.devmeteor.weihashi.logd
import cn.devmeteor.weihashi.model.LngLatNode
import cn.devmeteor.weihashi.model.PoiPatch
import cn.devmeteor.weihashi.utils.Util.gson
import cn.devmeteor.weihashi.utils.Util.kv
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader

object PoiManager {

    private const val DEFAULT_VERSION = 1L
    private const val DEFAULT_REFRESH_TIME = "2021-12-04"

    private var version = DEFAULT_VERSION
    private var refreshTime = DEFAULT_REFRESH_TIME

    fun getPoiTree(context: Context): LngLatNode =
        if (kv.containsKey(Constants.KEY_POI_TREE)
            && (kv.decodeLong(Constants.KEY_POI_STORE_VERSION, DEFAULT_VERSION) > DEFAULT_VERSION)
        ) {
            gson.fromJson(kv.decodeString(Constants.KEY_POI_TREE), LngLatNode::class.java)
        } else {
            logd(kv.containsKey(Constants.KEY_POI_TREE).toString())
            logd((kv.decodeLong(Constants.KEY_POI_STORE_VERSION, DEFAULT_VERSION) ).toString(), version.toString())
            gson.fromJson(
                BufferedReader(InputStreamReader(context.assets.open("map_data.json"))),
                LngLatNode::class.java
            )
        }

    fun getLastRefreshTime(): String = kv.decodeString(Constants.KEY_POI_LAST_REFRESH_TIME, refreshTime)!!

    fun refreshPoiTree(callback: (success: Boolean, new: Boolean) -> Unit) {
        configApi.getPoiData().enqueue(object : Callback<PoiPatch> {
            override fun onResponse(call: Call<PoiPatch>, response: Response<PoiPatch>) {
                val patch = response.body()
                if (patch == null || patch.version <= version) {
                    callback(true, false)
                    return
                }
                val todayDateStr = Util.getDateStrToday()
                version = patch.version
                refreshTime = todayDateStr
                kv.encode(Constants.KEY_POI_STORE_VERSION, patch.version)
                kv.encode(Constants.KEY_POI_TREE, gson.toJson(patch.data))
                kv.encode(Constants.KEY_POI_LAST_REFRESH_TIME, todayDateStr)
                callback(true, true)
            }

            override fun onFailure(call: Call<PoiPatch>, t: Throwable) {
                callback(false, false)
            }

        })
    }
}