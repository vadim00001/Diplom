package com.example.diplov_v1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.diplov_v1.databinding.HelpBinding

class HelpActivity : AppCompatActivity() {
    private lateinit var bg: HelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bg = HelpBinding.inflate(layoutInflater)
        setContentView(bg.root)

        supportActionBar?.title = getString(R.string.title_activity_help)

        val fromKey = intent.getStringExtra("fromKey")
        if (fromKey == "profile") {
            bg.textView39.isVisible = true
            bg.textView39.text = getString(R.string.HelpProfile)
        }
    }
}