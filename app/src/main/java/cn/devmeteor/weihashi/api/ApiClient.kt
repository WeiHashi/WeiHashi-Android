package cn.devmeteor.weihashi.api

import cn.devmeteor.weihashi.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

object ApiClient {
    private const val baseUrl = "https://devmeteor.cn:8080"

    //    private const val baseUrl = "https://192.168.113.97:8080"
    private const val configUrl = "https://devmeteor.cn/whs/"
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .apply {
                if (BuildConfig.DEBUG) {
                    hostnameVerifier { _, _ -> true }
                    sslSocketFactory(SSLSocketClient.SSLSocketFactory, object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

                        }

                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {

                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    })
                }
            }
            .build()
    }
    private val clientBuilder by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
    }

    val api: Api = clientBuilder.baseUrl(baseUrl).build().create(Api::class.java)
    val configApi: ConfigApi = clientBuilder.baseUrl(configUrl).build().create(ConfigApi::class.java)

}