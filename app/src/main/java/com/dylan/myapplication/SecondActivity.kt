package com.dylan.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.dylan.myapplication.model.GitHubUserInfo
import com.dylan.myapplication.network.NetClient
import com.google.android.material.button.MaterialButton
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.ArrayList
import kotlin.concurrent.thread

class SecondActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        findViewById<MaterialButton>(R.id.getRepositoryMb).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.getUserInfoMb).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.createUserInfoMb).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.getGroupListMb).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.updateUserHeadMb).setOnClickListener(this)
    }

    override fun onClick(p0: View) {
        when (p0.id) {
            R.id.getRepositoryMb -> {
                thread {
                    NetClient.getGitHubService().listRepos(getUserName()).execute().body()
                        ?.let { setContent(it) }
                }
            }
            R.id.getUserInfoMb -> {
                thread {
                    NetClient.getGitHubService().getUserInfo(getUserName()).execute().body()
                        ?.let { setContent(it) }
                }
            }
            R.id.createUserInfoMb -> {
                thread {
                    NetClient.getGitHubService().createUser(GitHubUserInfo()).execute().body()
                        ?.let { setContent(it) }
                }
            }

            R.id.getGroupListMb -> {
                thread {
                    NetClient.getGitHubService().groupList(getUserId()).execute().body()
                        ?.let { setContent(it) }
                }
            }
            R.id.updateUserHeadMb -> {
                thread {
                    PictureSelector.create(this).openCamera(SelectMimeType.ofImage()).forResult(object : OnResultCallbackListener<LocalMedia?> {
                        override fun onResult(result: ArrayList<LocalMedia?>?) {
                            val localMedia = result?.get(0)
                            val path = localMedia?.path ?: ""
                            thread {
                                NetClient.getGitHubService().updateUserPhoto(path.toRequestBody("image/jpeg".toMediaType())).execute().body()
                                    ?.let { setContent(it) }
                            }
                        }

                        override fun onCancel() {
                        }
                    })
                }
            }
        }
    }

    private fun getUserName() = "octocat"

    private fun getUserId() = 583231

    private fun setContent(it: Any) {
        runOnUiThread {
            findViewById<AppCompatTextView>(R.id.contentAtv).text = it.toString()
        }
    }
}