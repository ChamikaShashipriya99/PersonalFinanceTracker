package com.example.finance.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.finance.R

//class SplashActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_splash)
//
//        // Navigate to MainActivity after 2 seconds
//        Handler(Looper.getMainLooper()).postDelayed({
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        }, 3000)
//    }
//}

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.videoView)
        val videoPath = "android.resource://" + packageName + "/" + R.raw.investaria
        val uri = videoPath.toUri()

        videoView.setVideoURI(uri)
        videoView.setOnCompletionListener {
            // After the video finishes, move to the next screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        videoView.start()
    }
}