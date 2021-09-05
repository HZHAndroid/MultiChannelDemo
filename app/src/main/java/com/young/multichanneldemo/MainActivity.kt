package com.young.multichanneldemo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textTv = findViewById<TextView>(R.id.tvText)

        val stringBuffer = StringBuilder()
        stringBuffer.append(textTv.text)
            .append("\n")
            .append(getString(R.string.log))
            .append("\n")
            .append(BuildConfig.ENVIRONMENT)

        textTv.text = stringBuffer.toString()
    }
}