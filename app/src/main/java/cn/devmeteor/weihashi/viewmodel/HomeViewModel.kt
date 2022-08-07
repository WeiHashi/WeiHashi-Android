package cn.devmeteor.weihashi.viewmodel

import androidx.lifecycle.MutableLiveData
import cn.devmeteor.weihashi.api.Repository
import cn.devmeteor.weihashi.base.BaseViewModel
import cn.devmeteor.weihashi.model.Banner
import cn.devmeteor.weihashi.model.Message

class HomeViewModel : BaseViewModel() {
    private val repo = Repository()
    private val banners = MutableLiveData(
        arrayListOf(
            Banner("", "", 0, "", 1, true)
        )
    )

    fun getBanners(): ArrayList<Banner> = banners.value!!

    fun updateBanners(update: (ArrayList<Banner>) -> Unit) =
        handleResult {
            banners.value?.apply {
                clear()
                addAll(repo.getBanner().data)
                update(this)
            }
        }

    fun getBannerContent(objId: String, callback: (message: Message) -> Unit) =
        handleResult {
            callback(Message.fromNotice(repo.getBannerContent(objId).data, true))
        }

}