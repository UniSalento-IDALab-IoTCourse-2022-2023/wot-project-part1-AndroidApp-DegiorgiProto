package com.polar.androidblesdk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RegActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        val register = findViewById(R.id.btnReg) as Button
        //button register
        register.setOnClickListener {
            //aggiunta nel cloud database
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}