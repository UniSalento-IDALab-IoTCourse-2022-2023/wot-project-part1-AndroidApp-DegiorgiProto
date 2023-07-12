package com.polar.androidblesdk

import android.annotation.SuppressLint
import android.app.Person
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
import com.google.gson.Gson
import java.security.MessageDigest
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future


class RegActivity : AppCompatActivity() {

    data class utente(
        val _id: String,
        val name: String,
        val surname: String,
        val emailAddress: String,
        val password: String,
        val doctor: String
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        val register = findViewById<Button>(R.id.btnReg)


        val myConn = Backend();

        //button register
        register.setOnClickListener {
            val nomeTxt = findViewById<EditText>(R.id.etNome)
            val cognomeTxt = findViewById<EditText>(R.id.etCognome)
            val emailTxt = findViewById<EditText>(R.id.etEmail)
            val passTxt = findViewById<EditText>(R.id.etPassword)
            val doctorTxt = findViewById<EditText>(R.id.etDoctor)

            val nome = nomeTxt.text.toString()
            val cognome = cognomeTxt.text.toString()
            val email = emailTxt.text.toString()
            val pass = passTxt.text.toString()
            val doct = doctorTxt.text.toString()

            val passEnc = encryptPassword(pass)

            val urlGet = "http://192.168.0.105:3000/lista"

            val executor = Executors.newFixedThreadPool(1)
            val future: Future<String> = executor.submit(Callable { myConn.getRequest(urlGet) })
            val responseBody = future.get()
            Log.d("RESPONSE", responseBody)

            val arrString = getArray(responseBody)
            Log.d("ARRAYSTRING", arrString)


            val gson = Gson()
            val array = gson.fromJson(arrString, Array<utente>::class.java)
            Log.d("ARRAY", array.toString())

//            val sep = "},{"
//            val arrString = getArray(utenti).split(sep)
//            val arr = get_dict(arrString)

            val isPres = emailControll(array, email)
            if (!isPres) {
             //aggiunta nel cloud database
                val urlPost = "http://192.168.0.105:3000/aggiungiUtente"
                val req =
                    "{ \"name\": \"$nome\", \"surname\": \"$cognome\", \"emailAddress\": \"$email\", \"password\": \"$passEnc\", \"doctor\": \"$doct\" }"
                Thread { myConn.post_request(urlPost, req) }.start()

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            else {
                val intent = Intent(this, RegActivity::class.java)
                startActivity(intent)
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    Toast.makeText(
                        applicationContext,
                        "Email già presente, scegline un'altra",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    fun encryptPassword(password: String): String {
        val bytes = password.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun getArray(users: String): String {
        val startIndex = users.indexOf('[')
        val endIndex = users.indexOf(']')
        val arr = users.substring(startIndex, endIndex + 1)
        return arr
    }

//    fun get_dict(entries: List<String>): Array<Map<String, String>>{
//        val array: Array<Map<String, String>> = arrayOf()
//        val dict = mutableMapOf<String, String>()
//        for(elem in entries) {
//            val rslts = elem.split(",")
//            for(rslt in rslts) {
//                val keyValue = rslt.split(":")
//                val key = keyValue[0]
//                val value = keyValue[1]
//                dict[key] = value
//            }
//            array.plus(dict)
//        }
//        return array
//    }


//    fun emailControll(arrayelem: Array<Map<String, String>>, mail: String): Boolean {
//        for (elem in arrayelem) {
//            if(elem["emailAddress"].equals(mail)) {
//                return true
//            }
//        }
//        return false
//    }

    fun emailControll(arrayelem: Array<utente>, mail: String): Boolean {
        arrayelem.forEach {
            if (it.emailAddress.equals(mail)) {
                return true
            }
        }
        return false
    }

}
//class ReqTask(private val url: String, private val filter: String) : Callable<Response> {
//    override fun call(): Response {
//        // Effettua la richiesta e ottieni la risposta
//        val myConn = Backend()
//        val result = myConn.sendGetRequest(url)
//
//        return Response(result.statusCode, result.body)
//    }
//}