package com.gocation.gocation_android.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.gocation.gocation_android.R
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.activity_messaging.*

/**
 * Created by Billy on 31/05/17.
 */

class EventMap : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.festival_map)

        btn_back.onClick { finish() }

    }

    }

