package com.dylan.myapplication.network

import com.dylan.myapplication.global.GlobalConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Creator: Dylan.
 * Date: 2022/6/16.
 * desc:
 */
object NetClient {
    private var retrofit: Retrofit? = null

    fun getGitHubService(): GitHubService {
        return getRetrofit(GlobalConfig.GIT_HUB_API_BASE_URL).create(GitHubService::class.java)
    }

    private fun getRetrofit(baseUrl: String): Retrofit {
        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(getOkHttpClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
        }
        return retrofit!!
    }

    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(getInterceptor())
            .build()
    }

    private fun getInterceptor(): Interceptor {
        return HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }
}