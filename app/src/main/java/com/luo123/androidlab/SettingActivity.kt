package com.luo123.androidlab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setting_card_view.setOnClickListener {
            Toast.makeText(this,"ο(=•ω＜=)ρ⌒☆",Toast.LENGTH_SHORT).show()
        }
    }
}
