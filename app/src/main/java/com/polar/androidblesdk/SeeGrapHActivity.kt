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


class SeeGrapHActivity : AppCompatActivity() {
    private lateinit var chart1: LineChart
    private lateinit var chart2: LineChart

    data class fascia(val _id: String, val heartRate: String, val ecg: String, val email_address: String, val date: String, val hour: String)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        val email = intent.getStringExtra("email")
        val data = intent.getStringExtra("datas")

        val myConn = Backend();
        val url = "http://192.168.0.105:3000/h10Grafico?email_address=$email&date=$data"

        val executor = Executors.newFixedThreadPool(1)
        val future: Future<String> = executor.submit(Callable { myConn.getRequest(url) })
        val responseBody = future.get()
        Log.d("RESPONSE", responseBody)

        val arrh10 = getArray(responseBody)
        Log.d("ARRAYSTRING", arrh10)

        val gson = Gson()
        val array = gson.fromJson(arrh10, Array<fascia>::class.java)
        Log.d("ARRAY", array.toString())

        chart1 = findViewById(R.id.chart1)

        chart2 = findViewById(R.id.chart2)


        array.sortBy { stringToFloat(it.hour) }
        val filterArray1 = array.filter { it.heartRate != "0" }
        val entries1 = filterArray1.map {
            Entry(stringToFloat(it.hour), it.heartRate.toFloat())
        }

        array.sortBy { stringToFloat(it.hour) }
        val filterArray2 = array.filter { it.ecg != "0" }
        var i = 0;
        val entries2 = filterArray2.map {
            i += 1
            val data = addDist(stringToFloat(it.hour), i)
            Entry(data, it.ecg.toFloat())
        }

        val dataset1 = LineDataSet(entries1, "Heart Rate")
        dataset1.color = Color.RED
        dataset1.valueTextColor = Color.BLACK

        val dataset2 = LineDataSet(entries2, "ECG")
        dataset2.color = Color.RED
        dataset2.valueTextColor = Color.BLACK


        val lineData1 = LineData(dataset1)
        chart1.data = lineData1

        val lineData2 = LineData(dataset2)
        chart2.data = lineData2


        chart1.invalidate()

        chart2.invalidate()

    }

    fun getArray(users: String): String {
        val startIndex = users.indexOf('[')
        val endIndex = users.indexOf(']')
        val arr = users.substring(startIndex, endIndex + 1)
        return arr
    }

    fun stringToFloat(timeString: String): Float {
        val timeArray = timeString.split(":")
        val hours = timeArray[0].toFloat()
        val minutes = timeArray[1].toFloat() / 100
        val seconds = timeArray[2].toFloat() / 10000
        val totalSeconds = hours + minutes + seconds
        return totalSeconds
    }

    fun addDist(value: Float, i: Int): Float {
        val j = i.toFloat() / 10000
        return value + j
    }

}