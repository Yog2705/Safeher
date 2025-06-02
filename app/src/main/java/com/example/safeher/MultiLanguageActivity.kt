package com.example.safeher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MultiLanguageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multilanguage)

        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        findViewById<Button>(R.id.btnEnglish).setOnClickListener { setLanguage("en", sharedPreferences) }
        findViewById<Button>(R.id.btnKannada).setOnClickListener { setLanguage("kn", sharedPreferences) }
        findViewById<Button>(R.id.btnHindi).setOnClickListener { setLanguage("hi", sharedPreferences) }

    }

    private fun setLanguage(languageCode: String, preferences: SharedPreferences) {
        val editor = preferences.edit()
        editor.putString("language", languageCode)
        editor.apply()
        updateLocale(languageCode)
        recreateMainActivity()
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun recreateMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
