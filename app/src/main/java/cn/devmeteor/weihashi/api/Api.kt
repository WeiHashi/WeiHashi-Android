package cn.devmeteor.weihashi.api

import cn.devmeteor.weihashi.response.*
import retrofit2.http.*

interface Api {

    @POST("/login")
    suspend fun jwLogin(
        @Query("openid") openid: String,
        @Query("username") username: String,
        @Query("password") password: String
    ): LoginResultResponse

    @GET("/getDailyLessons")
    suspend fun getDailyLesson(@Query("cookieString") cookieString: String): LessonListResponse

    @GET("/grade")
    suspend fun getGrade(@Query("cookieString") cookieString: String): GradeResponse

    @GET("/getBanner")
    suspend fun getBanner(): BannerListResponse

    @GET("/getBannerContent")
    suspend fun getBannerContent(
        @Query("objId") objId: String,
        @Query("type") type: Int = 1
    ): NoticeResponse

    @FormUrlEncoded
    @POST("/updateAccount")
    suspend fun updateAccount(
        @Field("openid") openid: String,
        @Field("nickname") nickname: String,
        @Field("head") head: String,
        @Field("platform") platform: Int = 2
    ): UserInfoResponse

    @GET("/getMessages")
    suspend fun getMessageP1(@Query("classId") classId: String = ""): MessageP1Response

    @GET("/getMoreMessages")
    suspend fun getMoreMessages(
        @Query("page") page: Int,
        @Query("classId") classId: String = ""
    ): NoticeListResponse

}