package com.polar.androidblesdk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class DayHActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_date)

        val bottone = findViewById<Button>(R.id.btn)
        val email = intent.getStringExtra("email")

        bottone.setOnClickListener {
            val dataTxt = findViewById<EditText>(R.id.giorno)
            val data = dataTxt.text.toString()
            val intent = Intent(this, SeeGrapHActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("datas", data)
            startActivity(intent)
        }


    }
}