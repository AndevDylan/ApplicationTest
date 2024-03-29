package com.dylan.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.dylan.coroutine.CoroutineActivity
import com.dylan.customview.CustomViewActivity
import com.dylan.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initView()
    }

    private fun initView() {
        binding.run {
            netTestMb.setOnClickListener {
                startActivity(Intent(this@MainActivity, SecondActivity::class.java))
            }
            coroutineTestMb.setOnClickListener {
                startActivity(Intent(this@MainActivity, CoroutineActivity::class.java))
            }
            customViewTestMb.setOnClickListener {
                startActivity(Intent(this@MainActivity, CustomViewActivity::class.java))
            }
        }
    }
}