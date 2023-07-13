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
    private lateinit var chart3: LineChart

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

//        chart3 = findViewById(R.id.chart3)

        array.sortBy { stringToFloat(it.hour) }
        val filterArray1 = array.filter { it.heartRate != "0" }
        val entries1 = filterArray1.map {
            Entry(stringToFloat(it.hour), it.heartRate.toFloat())
        }

        array.sortBy { stringToFloat(it.hour) }
        val filterArray2 = array.filter { it.ecg != "0" }
        val entries2 = filterArray2.map {
            Entry(stringToFloat(it.hour), it.ecg.toFloat())
        }

//        array.sortBy { stringToFloat(it.hour) }
//        val entries3 = array.map {
//            Entry(stringToFloat(it.hour), it.rr.toFloat())
//        }

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
//
//        val entries3 = arrayListOf(
//            Entry(0f, 1f),
//            Entry(1f, 4f),
//            Entry(2f, 8f),
//            Entry(3f, 6f),
//            Entry(4f, 2f),
//            Entry(5f, 9f),
//            Entry(3f, 2f)
//        )

        val dataset1 = LineDataSet(entries1, "Heart Rate")
        dataset1.color = Color.RED
        dataset1.valueTextColor = Color.BLACK

        val dataset2 = LineDataSet(entries2, "RRMs")
        dataset2.color = Color.RED
        dataset2.valueTextColor = Color.BLACK

//        val dataset3 = LineDataSet(entries3, "RR")
//        dataset3.color = Color.RED
//        dataset3.valueTextColor = Color.BLACK

        val lineData1 = LineData(dataset1)
        chart1.data = lineData1

        val lineData2 = LineData(dataset2)
        chart2.data = lineData2

//        val lineData3 = LineData(dataset3)
//        chart3.data = lineData3

        chart1.invalidate()

        chart2.invalidate()

//        chart3.invalidate()
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
        val seconds = ((timeArray[2].toFloat() / 10000) + 0.005).toFloat()
        val totalSeconds = hours + minutes + seconds
        return totalSeconds
    }


}