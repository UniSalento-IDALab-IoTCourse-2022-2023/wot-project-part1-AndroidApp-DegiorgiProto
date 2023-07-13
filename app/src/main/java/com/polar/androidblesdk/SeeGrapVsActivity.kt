package com.polar.androidblesdk

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future


class SeeGrapVsActivity : AppCompatActivity() {

    private lateinit var chart1: LineChart
    private lateinit var chart2: LineChart
    private lateinit var chart3: LineChart
    private lateinit var chart4: LineChart
    private lateinit var chart5: LineChart
    private lateinit var chart6: LineChart

    private lateinit var grafppg: Button

    data class orologio(val _id: String, val heartRate: String, val ecg: String, val acc: String, val gyro: String, val magnet: String, val ppg1: String, val ppg2: String, val ppg3: String, val ppi: String, val email_address: String, val date: String, val hour: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grapvs)

        val email = intent.getStringExtra("email")
        val data = intent.getStringExtra("datae")

        val myConn = Backend();
        val url = "http://192.168.0.105:3000/vsGrafico?email_address=$email&date=$data"

        val executor = Executors.newFixedThreadPool(1)
        val future: Future<String> = executor.submit(Callable { myConn.getRequest(url) })
        val responseBody = future.get()
        Log.d("RESPONSE", responseBody)

        val arrVs = getArray(responseBody)
        Log.d("ARRAYSTRING", arrVs)

        val gson = Gson()
        val array = gson.fromJson(arrVs, Array<orologio>::class.java)
        Log.d("ARRAY", array.toString())

        chart1 = findViewById(R.id.chart1)

        chart2 = findViewById(R.id.chart2)

        chart3 = findViewById(R.id.chart3)

        chart4 = findViewById(R.id.chart4)

        chart5 = findViewById(R.id.chart5)

        chart6 = findViewById(R.id.chart6)

        grafppg = findViewById(R.id.ppg)

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

        array.sortBy { stringToFloat(it.hour) }
        val filterArray3 = array.filter { it.acc != "0" }
        val entries3 = filterArray3.map {
            Entry(stringToFloat(it.hour), it.acc.toFloat())
        }

        array.sortBy { stringToFloat(it.hour) }
        val filterArray4 = array.filter { it.gyro != "0" }
        val entries4 = filterArray4.map {
            Entry(stringToFloat(it.hour), it.gyro.toFloat())
        }

        array.sortBy { stringToFloat(it.hour) }
        val filterArray5 = array.filter { it.magnet != "0" }
        val entries5 = filterArray5.map {
            Entry(stringToFloat(it.hour), it.magnet.toFloat())
        }

        array.sortBy { stringToFloat(it.hour) }
        val filterArray6 = array.filter { it.ppi != "0" }
        val entries6 = filterArray6.map {
            Entry(stringToFloat(it.hour), it.ppi.toFloat())
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
//
//        val entries4 = arrayListOf(
//            Entry(0f, 1f),
//            Entry(1f, 4f),
//            Entry(2f, 8f),
//            Entry(3f, 6f),
//            Entry(4f, 2f),
//            Entry(5f, 9f),
//            Entry(3f, 2f)
//        )
//
//        val entries5 = arrayListOf(
//            Entry(0f, 1f),
//            Entry(1f, 4f),
//            Entry(2f, 8f),
//            Entry(3f, 6f),
//            Entry(4f, 2f),
//            Entry(5f, 9f),
//            Entry(3f, 2f)
//        )
//
//        val entries6 = arrayListOf(
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

        val dataset2 = LineDataSet(entries2, "ECG")
        dataset2.color = Color.RED
        dataset2.valueTextColor = Color.BLACK

        val dataset3 = LineDataSet(entries3, "ACC")
        dataset3.color = Color.RED
        dataset3.valueTextColor = Color.BLACK

        val dataset4 = LineDataSet(entries4, "GYRO")
        dataset4.color = Color.RED
        dataset4.valueTextColor = Color.BLACK

        val dataset5 = LineDataSet(entries5, "MAGNETOMETER")
        dataset5.color = Color.RED
        dataset5.valueTextColor = Color.BLACK

        val dataset6 = LineDataSet(entries6, "PPI")
        dataset6.color = Color.RED
        dataset6.valueTextColor = Color.BLACK


        val lineData1 = LineData(dataset1)
        chart1.data = lineData1

        val lineData2 = LineData(dataset2)
        chart2.data = lineData2

        val lineData3 = LineData(dataset3)
        chart3.data = lineData3

        val lineData4 = LineData(dataset4)
        chart4.data = lineData4

        val lineData5 = LineData(dataset5)
        chart5.data = lineData5

        val lineData6 = LineData(dataset6)
        chart6.data = lineData6

        chart1.invalidate()

        chart2.invalidate()

        chart3.invalidate()

        chart4.invalidate()

        chart5.invalidate()

        chart6.invalidate()

        grafppg.setOnClickListener {
            val intent = Intent(this, PpgActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("datae", data)
            startActivity(intent)
        }
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
