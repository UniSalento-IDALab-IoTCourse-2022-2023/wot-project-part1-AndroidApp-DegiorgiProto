package com.polar.androidblesdk

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class DeleteExeActivity : AppCompatActivity() {
    private lateinit var mainLayout: LinearLayout
    data class esercizio( val _id: String, val esercizio: String, val durata: String, val inizio: String, val emailAddress: String)


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_deletexe)

        val email = intent.getStringExtra("email")
        val urlGet = "http://192.168.0.105:3000/exemail?email_address=${email}"
        val myConn = Backend()

        val executor = Executors.newFixedThreadPool(1)
        val future: Future<String> = executor.submit(Callable { myConn.getRequest(urlGet) })
        val responseBody = future.get()
        Log.d("RESPONSE", responseBody)

        val arrString = getArray(responseBody)
        Log.d("ARRAYSTRING", arrString)

        val gson = Gson()
        val array = gson.fromJson(arrString, Array<esercizio>::class.java)
        Log.d("ARRAY", array.toString())

        mainLayout = findViewById(R.id.layout)


        array.forEach {
            val exe = "Esercizio: ${it.esercizio} \n Durata: ${it.durata} \n Inizio: ${it.inizio}"
            if (email != null) {
                addTextView(exe, email, it.inizio)
            }
        }
    }

    fun getArray(users: String): String {
        val startIndex = users.indexOf('[')
        val endIndex = users.indexOf(']')
        val arr = users.substring(startIndex, endIndex + 1)
        return arr
    }

    private fun addTextView(text: String, email: String, start: String) {
        val separator = View(applicationContext)
        separator.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        )
        separator.setBackgroundColor(Color.BLACK)

        val button = Button(this)
        button.text = "elimina"
        button.setBackgroundColor(Color.RED)
        button.setTextColor(Color.WHITE)
//        button.layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            1
//        )

        button.setOnClickListener {
            val url = "http://192.168.0.105:3000/noExe?email_address=$email&start=$start"
            do_delete(url, email, start)
            val intent = Intent(this, DeleteExeActivity:: class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        val textView = TextView(this)
        textView.setTextColor(Color.BLACK)
        textView.text = text
//        textView.layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            1
//        )

        mainLayout.addView(textView)
        mainLayout.addView(button)
        mainLayout.addView(separator)
    }

    fun do_delete(url:String, email: String, start: String) {
        val myConn = Backend()
        Thread{myConn.deleteWithFilter(url)}.start()
    }
}
