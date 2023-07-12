package com.polar.androidblesdk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ExerciseActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_addex)

        val aggiungi = findViewById<Button>(R.id.btnadd)

        aggiungi.setOnClickListener {
            val exeText = findViewById<EditText>(R.id.esercizi)
            val durTxt = findViewById<EditText>(R.id.durata)
            val inizioTxt = findViewById<EditText>(R.id.inizio)

            val esercizio = exeText.text.toString()
            val durata = durTxt.text.toString()
            val inizio = inizioTxt.text.toString()
            val email = intent.getStringExtra("email")
            val conn = Backend()

            val urlPost = "http://192.168.0.105:3000/aggiungiEsercizio"
            val req =
                "{\"esercizio\": \"$esercizio\", \"durata\": \"$durata\", \"inizio\":\"$inizio\", \"emailAddress\": \"$email\" }"
            Thread { conn.post_request(urlPost, req) }.start()

            val intent = Intent(this, OptionHActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }
    }
}