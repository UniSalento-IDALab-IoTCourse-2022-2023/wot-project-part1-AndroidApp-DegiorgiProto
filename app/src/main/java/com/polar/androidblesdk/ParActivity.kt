package com.polar.androidblesdk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ParActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_par)

        val pression = findViewById(R.id.pression) as Button
        val diabete = findViewById(R.id.diabete) as Button
        //button register
        pression.setOnClickListener {
            //aggiunta nel cloud database
            val intent = Intent(this, PresActivity::class.java)
            startActivity(intent)
        }

        diabete.setOnClickListener {
            //aggiunta nel cloud database
            val intent = Intent(this, DiabActivity::class.java)
            startActivity(intent)
        }
    }
}