package com.example.safeher

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val animatedText = findViewById<TextView>(R.id.animatedText)

// Start alpha at 0 (invisible)
        animatedText.alpha = 0f

// Fade-in animation
        animatedText.animate()
            .alpha(1f) // Fade to fully visible
            .setDuration(4000) // 4 seconds
            .withEndAction {
                // After animation, move to MainActivity
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
            .start()
    }
}