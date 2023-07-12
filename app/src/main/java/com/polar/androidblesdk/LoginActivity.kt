package com.polar.androidblesdk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.Klaxon
import java.security.MessageDigest
import kotlinx.coroutines.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class LoginActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val login = findViewById<Button>(R.id.btnLogin)
        val register = findViewById<Button>(R.id.btnReg)

        val myConn = Backend();

        //button login
        login.setOnClickListener {
            //credenziali cloud database controllo
            val emailTxT = findViewById<EditText>(R.id.etEmail)
            val passTxt = findViewById<EditText>(R.id.etPassword)

            val email = emailTxT.text.toString()
            val pass = passTxt.text.toString()

            val passEnc = encryptPassword(pass)

            //getbyEmail + confronto con la pass criptata

            val url = "http://192.168.0.105:3000/elemento?email_address=${email}"


//            val executor = Executors.newSingleThreadExecutor()
//            val future: Future<Response> = executor.submit(RequestTask(url, filter))
//            val response = future.get().body

//            var responseBody = ""
//
//            GlobalScope.launch {
//                try {
//                    val response = myConn.getFilteredData(url, filter)
//                    responseBody = response.body?.string().toString()
//                    println(responseBody)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }

            val executor = Executors.newFixedThreadPool(1)
            val future : Future<String> = executor.submit(Callable { myConn.getRequest(url) })
            val responseBody = future.get()
            Log.d("RESPONSE", responseBody)


            val risultato = get_dict(responseBody)
            Log.d("Risposta", risultato)

            if(risultato != "{\"email\":\"no email\"}") {
                data class utente_response(val _id: String, val name: String, val surname: String, val emailAddress : String, val password : String, val doctor : String)
                val result = Klaxon().parse<utente_response>(risultato)
                Log.d("Utente_Response", result.toString())

                val isGood = controll(email, passEnc, result!!.emailAddress, result.password)


                if(isGood) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("parEmail", email)
                    startActivity(intent)
                }
            }
            else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    Toast.makeText(
                        applicationContext,
                        "Non sei registrato",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

            //button register
            register.setOnClickListener {
                val intent = Intent(this, RegActivity::class.java)
                startActivity(intent)
            }
    }

    fun encryptPassword(password: String): String {
        val bytes = password.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun get_dict(resp: String): String {
        val startIndex = (resp.indexOf(':') + 1)
        val endIndex = (resp.indexOf('}'))
        val result = resp.substring(startIndex, endIndex + 1)

//        val entries = result.split(",")
//
//        val dict = entries.associate {
//            val (key, value) = it.split(":")
//            key to value
//        }
        return result
    }

    fun controll(mail: String, pass: String, email: String?, password: String?): Boolean {
        if (mail.equals(email)) {
            if (pass.equals(password)) {
                return true
            }
            return false
        }
        return false
    }
}


//class RequestTask(private val url: String, private val filter: String) : Callable<Response> {
//    override fun call(): Response {
//        // Effettua la richiesta e ottieni la risposta
//        val myConn = Backend()
//        val result = myConn.get_request_with_filter(url, filter)
//
//        return Response(result.statusCode, result.body)
//    }
//}


