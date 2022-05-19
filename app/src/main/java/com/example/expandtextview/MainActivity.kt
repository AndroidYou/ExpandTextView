package com.example.expandtextview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.expandtextview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val content ="今年3月份以来，受国际地缘政治复杂演变、国内疫情起伏反复等超预期变化影响，我国经济发展环境的复杂性、严峻性、不确定性上升，稳增长、稳就业、稳物价面临新挑战。从最新发布的4月份经济数据看，一些主要经济指标明显下滑，经济下行压力进一步加大，也引发了各方对我国经济发展前景的担忧。"
    private val list = mutableListOf<String>("国际地缘","国内疫情","经济发展","新挑战")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = DataBindingUtil.setContentView<ActivityMainBinding>(this,R.layout.activity_main)

        binding.expandTextView.setCloseText(content)

        binding.expandLayout.setTextContent(content)

        binding.expandLayout2.setTextContent(content,list)



    }
}