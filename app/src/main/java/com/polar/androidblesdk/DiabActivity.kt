package com.polar.androidblesdk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DiabActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add)

        val aggiungi = findViewById(R.id.btnadd) as Button
        //button register
        aggiungi.setOnClickListener {
            //aggiunta nel cloud database
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}