package com.polar.androidblesdk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val login = findViewById(R.id.btnLogin) as Button
        val register = findViewById(R.id.btnReg) as Button
        //button login
        login.setOnClickListener {
            //credenziali cloud database controllo
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //button register
        register.setOnClickListener {
            val intent = Intent(this, RegActivity::class.java)
            startActivity(intent)
        }
    }
}