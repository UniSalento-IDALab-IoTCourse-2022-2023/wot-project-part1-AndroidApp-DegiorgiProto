package com.polar.androidblesdk

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class NotifyActivity : AppCompatActivity() {
    //get delle notifiche

    private lateinit var mainLayout: LinearLayout

    data class notifica( val _id: String, val notifica: String, val emailAddress: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_notify)

        val email = intent.getStringExtra("parEmail")
        val urlGet = "http://192.168.0.105:3000/notificaEmail?email_address=${email}"
        val myConn = Backend()

        val executor = Executors.newFixedThreadPool(1)
        val future: Future<String> = executor.submit(Callable { myConn.getRequest(urlGet) })
        val responseBody = future.get()
        Log.d("RESPONSE", responseBody)

        val arrString = getArray(responseBody)
        Log.d("ARRAYSTRING", arrString)

        val gson = Gson()
        val array = gson.fromJson(arrString, Array<notifica>::class.java)
        Log.d("ARRAY", array.toString())

        mainLayout = findViewById(R.id.mainLayout)


        array.forEach {
            addTextView(it.notifica)
        }
    }

    fun getArray(users: String): String {
        val startIndex = users.indexOf('[')
        val endIndex = users.indexOf(']')
        val arr = users.substring(startIndex, endIndex + 1)
        return arr
    }

    private fun addTextView(text: String) {
        val separator = View(applicationContext)
        separator.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        )
        separator.setBackgroundColor(Color.BLACK)

        val textView = TextView(this)
        textView.setTextColor(Color.BLACK)
        textView.text = text

        mainLayout.addView(textView)
        mainLayout.addView(separator)
    }

}