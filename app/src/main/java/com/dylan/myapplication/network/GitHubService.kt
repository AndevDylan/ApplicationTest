package com.dylan.myapplication.network

import com.dylan.myapplication.model.GitHubRepositoryInfo
import com.dylan.myapplication.model.GitHubUserInfo
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Creator: Dylan.
 * Date: 2022/6/16.
 * desc:
 */
interface GitHubService {
    // 获取用户仓库列表
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String): Call<List<GitHubRepositoryInfo?>?>

    // 获取用户信息
    @GET("users/{user}")
    fun getUserInfo(@Path("user") user: String): Call<GitHubUserInfo>

    // 创建用户
    @POST("users/new")
    fun createUser(@Body user: GitHubUserInfo): Call<GitHubUserInfo>

    @GET("group/{id}/users")
    fun groupList(@Path("id") groupId: Int): Call<List<GitHubUserInfo>>

    @Multipart
    @PUT("user/photo")
    fun updateUserPhoto(@Part("photo") photo: RequestBody/*, @Part("description") description: RequestBody*/): Call<GitHubUserInfo>
}