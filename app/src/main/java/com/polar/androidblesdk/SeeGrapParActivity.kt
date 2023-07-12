package com.polar.androidblesdk

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future


class SeeGrapParActivity : AppCompatActivity() {
    private lateinit var chart1: LineChart
    private lateinit var chart2: LineChart
    private lateinit var chart3: LineChart

    data class pressione(val _id: String, val valuemax: String, val valuemin: String, val day: String, val hour: String, val email_address: String, val date: String)
    data class diabete(val _id: String, val value: String, val day: String, val hour: String, val email_address: String, val date: String)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seepar)

        val email = intent.getStringExtra("parEmail")
        val data = intent.getStringExtra("data")

        //Log.d("Email", email.toString())

        val myConn = Backend();
        val urlPres = "http://192.168.0.105:3000/presGrafico?email_address=$email&date=$data"
        val urlDiab = "http://192.168.0.105:3000/diabGrafico?email_address=$email&date=$data"

        val executor1 = Executors.newFixedThreadPool(1)
        val future1: Future<String> = executor1.submit(Callable { myConn.getRequest(urlPres) })
        val responseBody1 = future1.get()
        Log.d("RESPONSE", responseBody1)

        val arrPres = getArray(responseBody1)
        Log.d("ARRAYSTRING", arrPres)

        val gson1 = Gson()
        val array1 = gson1.fromJson(arrPres, Array<pressione>::class.java)
        Log.d("ARRAY", array1.toString())


        val executor2 = Executors.newFixedThreadPool(1)
        val future2: Future<String> = executor2.submit(Callable { myConn.getRequest(urlDiab) })
        val responseBody2 = future2.get()
        Log.d("RESPONSE", responseBody2)

        val arrDiab = getArray(responseBody2)
        Log.d("ARRAYSTRING", arrDiab)

        val gson2 = Gson()
        val array2 = gson2.fromJson(arrDiab, Array<diabete>::class.java)
        Log.d("ARRAY", array2.toString())


        chart1 = findViewById(R.id.chart1)

        chart2 = findViewById(R.id.chart2)

        chart3 = findViewById(R.id.chart3)

        array1.sortBy { stringToFloat(it.hour) }
        val entries1 = array1.map {
            Entry(stringToFloat(it.hour), it.valuemax.toFloat())
        }

        array1.sortBy { stringToFloat(it.hour) }
        val entries2 = array1.map {
            Entry(stringToFloat(it.hour), it.valuemin.toFloat())
        }

//        val entries1 = arrayListOf(
//            Entry(0f, 1f),
//            Entry(1f, 4f),
//            Entry(2f, 8f),
//            Entry(3f, 6f),
//            Entry(4f, 2f),
//            Entry(5f, 9f),
//            Entry(3f, 2f)
//        )
//
//        val entries2 = arrayListOf(
//            Entry(0f, 1f),
//            Entry(1f, 4f),
//            Entry(2f, 8f),
//            Entry(3f, 6f),
//            Entry(4f, 2f),
//            Entry(5f, 9f),
//            Entry(3f, 2f)
//        )


        array2.sortBy { stringToFloat(it.hour) }
        val entries3 = array2.map {
            Entry(stringToFloat(it.hour), it.value.toFloat())
        }

//        val entries3 = arrayListOf(
//            Entry(0f, 1f),
//            Entry(1f, 4f),
//            Entry(2f, 8f),
//            Entry(3f, 6f),
//            Entry(4f, 2f),
//            Entry(5f, 9f),
//            Entry(3f, 2f)
//        )

        val dataset1 = LineDataSet(entries1, "Pressione Max")
        dataset1.color = Color.RED
        dataset1.valueTextColor = Color.BLACK

        val dataset2 = LineDataSet(entries2, "Pressione Min")
        dataset2.color = Color.RED
        dataset2.valueTextColor = Color.BLACK

        val dataset3 = LineDataSet(entries3, "Diabete")
        dataset3.color = Color.RED
        dataset3.valueTextColor = Color.BLACK

        val lineData1 = LineData(dataset1)
        chart1.data = lineData1

        val lineData2 = LineData(dataset2)
        chart2.data = lineData2

        val lineData3 = LineData(dataset3)
        chart3.data = lineData3

        chart1.invalidate()

        chart2.invalidate()

        chart3.invalidate()
    }

    fun getArray(users: String): String {
        val startIndex = users.indexOf('[')
        val endIndex = users.indexOf(']')
        val arr = users.substring(startIndex, endIndex + 1)
        return arr
    }

    fun stringToFloat(timeString: String): Float {
        val timeParts = timeString.split(":")
        val hours = timeParts[0].toFloat()
        val minutes = timeParts[1].toFloat() / 100
        return hours + minutes
    }
}