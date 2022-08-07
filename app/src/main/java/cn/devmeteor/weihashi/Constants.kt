package cn.devmeteor.weihashi

import cn.devmeteor.weihashi.channel.ShareContent

object Constants {

    const val TIMEOUT_LIMIT = 15 * 1000L
    const val TOAST_LENGTH = 1500L
    const val MINUTE_LEN = 60 * 1000L
    const val HOUR_LEN = 60 * MINUTE_LEN
    const val DAY_LEN = 24 * HOUR_LEN
    const val WEEK_LEN = 7 * DAY_LEN
    const val MAP_ZOOM = 19F

    const val QQ_APP_ID = BuildConfig.QQ_APP_ID
    const val WX_APP_ID = BuildConfig.WX_APP_ID
    const val WEIBO_APP_KEY = BuildConfig.WEIBO_APP_KEY
    const val BUGLY_APP_ID = BuildConfig.BUGLY_APP_ID
    const val STORE_ENC_KEY = BuildConfig.STORE_ENC_KEY

    const val TEST_APPLY_URL = "https://devmeteor.cn:8080/testApply"
    const val POI_REPORT_URL = "https://devmeteor.cn:8080/poiReport"
    const val FEEDBACK_URL = "https://support.qq.com/product/310295"
    const val ABOUT_URL = "https://devmeteor.cn/whs/about/"
    const val AGREEMENT_URL = "https://devmeteor.cn/whs/about/agreement.html"
    const val PRIVACY_URL = "https://devmeteor.cn/whs/about/privacy.html"
    const val XIAO_LI_URL = "https://image.devmeteor.cn/whs/img/xiaoli.jpg"
    const val WEIBO_REDIRECT_URL = "https://api.weibo.com/oauth2/default.html"
    const val WEIBO_SCOPE =
        "email,direct_messages_read,direct_messages_write," +
                "friendships_groups_read,friendships_groups_write,statuses_to_me_read," +
                "follow_app_official_microblog," + "invitation_write"
    const val FILE_PROVIDER_AUTHORITIES = "cn.devmeteor.weihashi.fileprovider"
    const val X5_DEBUG_URL = "http://debugx5.qq.com"

    const val KEY_OPENID = "openid"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_EXPIRES_IN = "expires_in"
    const val KEY_NICKNAME = "nickname"
    const val KEY_AVATAR = "avatar"
    const val KEY_STUDENT_ID = "student_id"
    const val KEY_PASSWORD = "password"
    const val KEY_NAME = "name"
    const val KEY_COOKIES = "cookies"
    const val KEY_LESSON_DATA = "lesson_data"
    const val KEY_LESSONS = "lessons"
    const val KEY_WEEK_TOTAL = "weekTotal"
    const val KEY_GRADE = "grade"
    const val KEY_MAINTENANCE_START = "maintenanceStart"
    const val KEY_MAINTENANCE_END = "maintenanceEnd"
    const val KEY_TERM = "term"
    const val KEY_TERM_START = "term_start"
    const val KEY_FIRST_OPEN = "first_open"
    const val KEY_POI_TREE = "poi_tree"
    const val KEY_POI_STORE_VERSION = "poi_store_version"
    const val KEY_POI_LAST_REFRESH_TIME = "poi_last_refresh_time"
    const val KEY_GUEST = "guest"

    const val ACTION_LESSON_DATA_CHANGED = "cn.devmeteor.weihashi.action.LESSON_DATA_CHANGED"
    const val ACTION_NEED_LOGIN = "cn.devmeteor.weihashi.action.NEED_LOGIN"
    const val ACTION_LAST_PAGE = "cn.devmeteor.weihashi.action.LAST_PAGE"
    const val ACTION_NEXT_PAGE = "cn.devmeteor.weihashi.action.NEXT_PAGE"

    val SHARE_APP = ShareContent(
        ShareContent.TYPE_WEB,
        "微哈师",
        "师大人的专属APP",
        "https://devmeteor.cn",
        "https://devmeteor.cn/whs/logo_radius.png"
    )
}