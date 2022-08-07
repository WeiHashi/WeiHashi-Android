package cn.devmeteor.weihashi.api

import cn.devmeteor.weihashi.model.PoiPatch
import cn.devmeteor.weihashi.model.Update
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ConfigApi {

    @GET("update.php")
    fun checkUpdate(@Query("versionName") versionName: String): Call<Update>

    @GET("maintenance.json")
    fun checkMaintenance(): Call<Map<String, String>>

    @GET("poi.json")
    fun getPoiData(): Call<PoiPatch>

    @GET("guest.json")
    fun getGuestData():Call<Map<String,String>>

}