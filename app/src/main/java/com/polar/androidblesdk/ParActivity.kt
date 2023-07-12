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

        val email = intent.getStringExtra("parEmail")
        val pression = findViewById<Button>(R.id.pression)
        val diabete = findViewById<Button>(R.id.diabete)
        val grafici = findViewById<Button>(R.id.graphics)
        //button register
        pression.setOnClickListener {
            //aggiunta nel cloud database
            val intent = Intent(this, PresActivity::class.java)
            intent.putExtra("parEmail", email)
            startActivity(intent)
        }

        diabete.setOnClickListener {
            //aggiunta nel cloud database
            val intent = Intent(this, DiabActivity::class.java)
            intent.putExtra("parEmail", email)
            startActivity(intent)
        }

        grafici.setOnClickListener {
            //vedere i grafici con i valori
            val intent = Intent(this, DayParActivity::class.java)
            intent.putExtra("parEmail", email)
            startActivity(intent)
        }
    }
}