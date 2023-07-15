package com.polar.androidblesdk

import WsBack
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.snackbar.Snackbar
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.sqrt

class OptionVsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "OptionVsActivity"
        private const val API_LOGGER_TAG = "API LOGGER"
        private const val PERMISSION_REQUEST_CODE = 1
    }

    // ATTENTION! Replace with the device ID from your device.
    private var veritySenseId = "C2DCFB27"

    private val api: PolarBleApi by lazy {
        // Notice all features are enabled
        PolarBleApiDefaultImpl.defaultImplementation(
            applicationContext,
            setOf(
                PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
                PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_H10_EXERCISE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP,
                PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
            )
        )
    }

    private lateinit var broadcastDisposable: Disposable
    private var scanDisposable: Disposable? = null
    private var autoConnectDisposable: Disposable? = null
    private var hrDisposable: Disposable? = null
    private var ecgDisposable: Disposable? = null
    private var accDisposable: Disposable? = null
    private var gyrDisposable: Disposable? = null
    private var magDisposable: Disposable? = null
    private var ppgDisposable: Disposable? = null
    private var ppiDisposable: Disposable? = null
    private var sdkModeEnableDisposable: Disposable? = null


    private var sdkModeEnabledStatus = false
    private var deviceConnected = false
    private var bluetoothEnabled = false

    private lateinit var broadcastButton: Button
    private lateinit var autoConnectButton: Button
    private lateinit var scanButton: Button

    private lateinit var connectvs: Button
    private lateinit var hrButton: Button
    private lateinit var ecgButton: Button
    private lateinit var accButton: Button
    private lateinit var gyrButton: Button
    private lateinit var magButton: Button
    private lateinit var ppgButton: Button
    private lateinit var ppiButton: Button

    private lateinit var graphicButton: Button

    private lateinit var toggleSdkModeButton: Button

    private lateinit var countDownTimer: CountDownTimer

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vs)
        Log.d(TAG, "version: " + PolarBleApiDefaultImpl.versionInfo())
        broadcastButton = findViewById(R.id.broadcast_button)
        autoConnectButton = findViewById(R.id.auto_connect_button)
        scanButton = findViewById(R.id.scan_button)

        connectvs = findViewById(R.id.connect_vs)
        hrButton = findViewById(R.id.hr_button)
        ecgButton = findViewById(R.id.ecg_button)
        accButton = findViewById(R.id.acc_button)
        gyrButton = findViewById(R.id.gyr_button)
        magButton = findViewById(R.id.mag_button)
        ppgButton = findViewById(R.id.ohr_ppg_button)
        ppiButton = findViewById(R.id.ohr_ppi_button)

        graphicButton = findViewById(R.id.graphics)

        toggleSdkModeButton = findViewById(R.id.toggle_SDK_mode)


        val email = intent.getStringExtra("email")

        val myConn = Backend()
        val urls = "ws://192.168.0.105:8000"
        val ws = WsBack(urls, applicationContext, email.toString());

        api.setPolarFilter(false)

        // If there is need to log what is happening inside the SDK, it can be enabled like this:
        val enableSdkLogs = false
        if(enableSdkLogs) {
            api.setApiLogger { s: String -> Log.d(API_LOGGER_TAG, s) }
        }

        api.setApiCallback(object : PolarBleApiCallback() {
            override fun blePowerStateChanged(powered: Boolean) {
                Log.d(TAG, "BLE power: $powered")
                bluetoothEnabled = powered
                if (powered) {
                    enableAllButtons()
                    showToast("Phone Bluetooth on")
                } else {
                    disableAllButtons()
                    showToast("Phone Bluetooth off")
                }
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTED: ${polarDeviceInfo.deviceId}")
                veritySenseId = polarDeviceInfo.deviceId
                deviceConnected = true
                val buttonText = getString(R.string.disconnect_from_veritysense, veritySenseId)
                toggleButtonDown(connectvs, buttonText)
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTING: ${polarDeviceInfo.deviceId}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "DISCONNECTED: ${polarDeviceInfo.deviceId}")
                deviceConnected = false
                val buttonText = getString(R.string.connect_to_veritysense, veritySenseId)
                toggleButtonUp(connectvs, buttonText)
                toggleButtonUp(toggleSdkModeButton, R.string.enable_sdk_mode)
            }

            override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
                Log.d(TAG, "DIS INFO uuid: $uuid value: $value")
            }

            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d(TAG, "BATTERY LEVEL: $level")
            }

            override fun hrNotificationReceived(identifier: String, data: PolarHrData.PolarHrSample) {
                // deprecated
            }
        })

        broadcastButton.setOnClickListener {
            if (!this::broadcastDisposable.isInitialized || broadcastDisposable.isDisposed) {
                toggleButtonDown(broadcastButton, R.string.listening_broadcast)
                broadcastDisposable = api.startListenForPolarHrBroadcasts(null)
                    .subscribe(
                        { polarBroadcastData: PolarHrBroadcastData ->
                            Log.d(TAG, "HR BROADCAST ${polarBroadcastData.polarDeviceInfo.deviceId} HR: ${polarBroadcastData.hr} batt: ${polarBroadcastData.batteryStatus}")
                        },
                        { error: Throwable ->
                            toggleButtonUp(broadcastButton, R.string.listen_broadcast)
                            Log.e(TAG, "Broadcast listener failed. Reason $error")
                        },
                        { Log.d(TAG, "complete") }
                    )
            } else {
                toggleButtonUp(broadcastButton, R.string.listen_broadcast)
                broadcastDisposable.dispose()
            }
        }


        autoConnectButton.setOnClickListener {
            if (autoConnectDisposable != null) {
                autoConnectDisposable?.dispose()
            }
            autoConnectDisposable = api.autoConnectToDevice(-60, "180D", null)
                .subscribe(
                    { Log.d(TAG, "auto connect search complete") },
                    { throwable: Throwable -> Log.e(TAG, "" + throwable.toString()) }
                )
        }

        scanButton.setOnClickListener {
            val isDisposed = scanDisposable?.isDisposed ?: true
            if (isDisposed) {
                toggleButtonDown(scanButton, R.string.scanning_devices)
                scanDisposable = api.searchForDevice()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { polarDeviceInfo: PolarDeviceInfo ->
                            Log.d(TAG, "polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable)
                        },
                        { error: Throwable ->
                            toggleButtonUp(scanButton, "Scan devices")
                            Log.e(TAG, "Device scan failed. Reason $error")
                        },
                        {
                            toggleButtonUp(scanButton, "Scan devices")
                            Log.d(TAG, "complete")
                        }
                    )
            } else {
                toggleButtonUp(scanButton, "Scan devices")
                scanDisposable?.dispose()
            }
        }

        connectvs.text = getString(R.string.connect_to_veritysense, veritySenseId)
        connectvs.setOnClickListener {
            try {
                if (deviceConnected) {
                    api.disconnectFromDevice(veritySenseId)
                } else {
                    api.connectToDevice(veritySenseId)
                }
            } catch (polarInvalidArgument: PolarInvalidArgument) {
                val attempt = if (deviceConnected) {
                    "disconnect"
                } else {
                    "connect"
                }
                Log.e(TAG, "Failed to $attempt. Reason $polarInvalidArgument ")
            }
        }

        hrButton.setOnClickListener {
            val isDisposed = hrDisposable?.isDisposed ?: true
            if (isDisposed) {
                toggleButtonDown(hrButton, R.string.stop_hr_stream)
                hrDisposable = api.startHrStreaming(veritySenseId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { hrData: PolarHrData ->
                            for (sample in hrData.samples) {
                                Log.d(TAG, "HR     bpm: ${sample.hr} rrs: ${sample.rrsMs} rrAvailable: ${sample.rrAvailable} contactStatus: ${sample.contactStatus} contactStatusSupported: ${sample.contactStatusSupported}")
                                val urlPost = "http://192.168.0.105:3000/aggiungiVS"
                                val req =
                                    "{ \"heartRate\": \"${sample.hr}\", \"ecg\": \"0\", \"acc\" : \"0\", \"gyro\" : \"0\", \"magnet\" : \"0\", \"ppg1\" : \"0\", \"ppg2\" : \"0\", \"ppg3\" : \"0\", \"ppi\" : \"0\", \"emailAddress\": \"$email\" }"
                                Thread { myConn.post_request(urlPost, req) }.start()
                                //Log.d(TAG, "HR     bpm: ${sample.hr} rrs: ${sample.rrsMs} rrAvailable: ${sample.rrAvailable} contactStatus: ${sample.contactStatus} contactStatusSupported: ${sample.contactStatusSupported}")
                            }
                            ws.start()
                            //val intent = Intent(this, MainActivity::class.java)
                            countDownTimer = object : CountDownTimer(15000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    // Avvia il conteggio alla rovescia

                                    Log.d("TIMER", "Il timer è partito")
                                }

                                @RequiresApi(Build.VERSION_CODES.O)
                                override fun onFinish() {
                                    // Termina il conteggio alla rovescia e fa camminare l'immagine
                                    Log.d("TIMER", "Il timer ha finito")
                                    ws.sendMessage("Non misuri l'heart rate del polar vs dal ${nowTime()}")
                                    ws.disconnect()
                                }
                            }
                            // Avvia il contatore
                            countDownTimer.start()
                            //startActivity(intent)
                        },
                        { error: Throwable ->
                            toggleButtonUp(hrButton, R.string.start_hr_stream)
                            Log.e(TAG, "HR stream failed. Reason $error")
                        },
                        { Log.d(TAG, "HR stream complete") }
                    )
            } else {
                toggleButtonUp(hrButton, R.string.start_hr_stream)
                // NOTE dispose will stop streaming if it is "running"
                hrDisposable?.dispose()
            }
        }

        ecgButton.setOnClickListener {
            val isDisposed = ecgDisposable?.isDisposed ?: true
            if (isDisposed) {
                toggleButtonDown(ecgButton, R.string.stop_ecg_stream)
                ecgDisposable = requestStreamSettings(veritySenseId, PolarBleApi.PolarDeviceDataType.ECG)
                    .flatMap { settings: PolarSensorSetting ->
                        api.startEcgStreaming(veritySenseId, settings)
                    }
                    .subscribe(
                        { polarEcgData: PolarEcgData ->
                            for (data in polarEcgData.samples) {
                                Log.d(TAG, "    yV: ${data.voltage} timeStamp: ${data.timeStamp}")
                                val urlPost = "http://192.168.0.105:3000/aggiungiVS"
                                val req =
                                    "{ \"heartRate\": \"0\", \"ecg\": \"${data.voltage}\", \"acc\" : \"0\", \"gyro\" : \"0\", \"magnet\" : \"0\", \"ppg1\" : \"0\", \"ppg2\" : \"0\", \"ppg3\" : \"0\", \"ppi\" : \"0\", \"emailAddress\": \"$email\" }"
                                Thread { myConn.post_request(urlPost, req) }.start()
                                //Log.d(TAG, "HR     bpm: ${sample.hr} rrs: ${sample.rrsMs} rrAvailable: ${sample.rrAvailable} contactStatus: ${sample.contactStatus} contactStatusSupported: ${sample.contactStatusSupported}")
                            }
                            ws.start()
                            //val intent = Intent(this, MainActivity::class.java)
                            countDownTimer = object : CountDownTimer(15000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    // Avvia il conteggio alla rovescia

                                    Log.d("TIMER", "Il timer è partito")
                                }

                                @RequiresApi(Build.VERSION_CODES.O)
                                override fun onFinish() {
                                    // Termina il conteggio alla rovescia e fa camminare l'immagine
                                    Log.d("TIMER", "Il timer ha finito")
                                    ws.sendMessage("Non misuri l'ecg del polar vs dal ${nowTime()}")
                                    ws.disconnect()
                                }
                            }
                            // Avvia il contatore
                            countDownTimer.start()
                            //startActivity(intent)

                        },
                        { error: Throwable ->
                            toggleButtonUp(ecgButton, R.string.start_ecg_stream)
                            Log.e(TAG, "ECG stream failed. Reason $error")
                            showToast("il dispositivo Polar Verity Sense non è in grado di leggere o impostare le impostazioni per il tipo di misurazione specificato.")
                        },
                        { Log.d(TAG, "ECG stream complete") }
                    )
            } else {
                toggleButtonUp(ecgButton, R.string.start_ecg_stream)
                // NOTE stops streaming if it is "running"
                ecgDisposable?.dispose()
            }
        }

        accButton.setOnClickListener {
            val isDisposed = accDisposable?.isDisposed ?: true
            if (isDisposed) {
                toggleButtonDown(accButton, R.string.stop_acc_stream)
                accDisposable = requestStreamSettings(veritySenseId, PolarBleApi.PolarDeviceDataType.ACC)
                    .flatMap { settings: PolarSensorSetting ->
                        api.startAccStreaming(veritySenseId, settings)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { polarAccelerometerData: PolarAccelerometerData ->
                            for (data in polarAccelerometerData.samples) {
                                Log.d(TAG, "ACC    x: ${data.x} y: ${data.y} z: ${data.z} timeStamp: ${data.timeStamp}")
                                val acc = sqrt((((data.x)*(data.x)) + ((data.y)*(data.y)) + ((data.z)*(data.z))).toDouble())
                                val urlPost = "http://192.168.0.105:3000/aggiungiVS"
                                val req =
                                    "{ \"heartRate\": \"0\", \"ecg\": \"0\", \"acc\" : \"$acc\", \"gyro\" : \"0\", \"magnet\" : \"0\", \"ppg1\" : \"0\", \"ppg2\" : \"0\", \"ppg3\" : \"0\", \"ppi\" : \"0\", \"emailAddress\": \"$email\" }"
                                Thread { myConn.post_request(urlPost, req) }.start()
                                //Log.d(TAG, "HR     bpm: ${sample.hr} rrs: ${sample.rrsMs} rrAvailable: ${sample.rrAvailable} contactStatus: ${sample.contactStatus} contactStatusSupported: ${sample.contactStatusSupported}")
                            }
                            ws.start()
                            //val intent = Intent(this, MainActivity::class.java)
                            countDownTimer = object : CountDownTimer(15000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    // Avvia il conteggio alla rovescia

                                    Log.d("TIMER", "Il timer è partito")
                                }

                                @RequiresApi(Build.VERSION_CODES.O)
                                override fun onFinish() {
                                    // Termina il conteggio alla rovescia e fa camminare l'immagine
                                    Log.d("TIMER", "Il timer ha finito")
                                    ws.sendMessage("Non misuri l'acc del polar vs dal ${nowTime()}")
                                    ws.disconnect()
                                }
                            }
                            // Avvia il contatore
                            countDownTimer.start()
                            //startActivity(intent)
                        },
                        { error: Throwable ->
                            toggleButtonUp(accButton, R.string.start_acc_stream)
                            Log.e(TAG, "ACC stream failed. Reason $error")
                            showToast("il dispositivo Polar Verity Sense non è in grado di leggere o impostare le impostazioni per il tipo di misurazione specificato.")
                        },
                        {
                            showToast("ACC stream complete")
                            Log.d(TAG, "ACC stream complete")
                        }
                    )
            } else {
                toggleButtonUp(accButton, R.string.start_acc_stream)
                // NOTE dispose will stop streaming if it is "running"
                accDisposable?.dispose()
            }
        }

        gyrButton.setOnClickListener {
            val isDisposed = gyrDisposable?.isDisposed ?: true
            if (isDisposed) {
                toggleButtonDown(gyrButton, R.string.stop_gyro_stream)
                gyrDisposable =
                    requestStreamSettings(veritySenseId, PolarBleApi.PolarDeviceDataType.GYRO)
                        .flatMap { settings: PolarSensorSetting ->
                            api.startGyroStreaming(veritySenseId, settings)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { polarGyroData: PolarGyroData ->
                                for (data in polarGyroData.samples) {
                                    Log.d(TAG, "GYR    x: ${data.x} y: ${data.y} z: ${data.z} timeStamp: ${data.timeStamp}")
                                    val gyro = sqrt((((data.x)*(data.x)) + ((data.y)*(data.y)) + ((data.z)*(data.z))).toDouble())

                                    val urlPost = "http://192.168.0.105:3000/aggiungiVS"
                                    val req =
                                        "{ \"heartRate\": \"0\", \"ecg\": \"0\", \"acc\" : \"0\", \"gyro\" : \"$gyro\", \"magnet\" : \"0\", \"ppg1\" : \"0\", \"ppg2\" : \"0\", \"ppg3\" : \"0\", \"ppi\" : \"0\", \"emailAddress\": \"$email\" }"
                                    Thread { myConn.post_request(urlPost, req) }.start()
                                }
                                ws.start()
                                //val intent = Intent(this, MainActivity::class.java)
                                countDownTimer = object : CountDownTimer(15000, 1000) {
                                    override fun onTick(millisUntilFinished: Long) {
                                        // Avvia il conteggio alla rovescia

                                        Log.d("TIMER", "Il timer è partito")
                                    }

                                    @RequiresApi(Build.VERSION_CODES.O)
                                    override fun onFinish() {
                                        // Termina il conteggio alla rovescia e fa camminare l'immagine
                                        Log.d("TIMER", "Il timer ha finito")
                                        ws.sendMessage("Non usi il gyroscopio del polar vs dal ${nowTime()}")
                                        ws.disconnect()
                                    }
                                }
                                // Avvia il contatore
                                countDownTimer.start()
                                //startActivity(intent)

                            },
                            { error: Throwable ->
                                toggleButtonUp(gyrButton, R.string.start_gyro_stream)
                                Log.e(TAG, "GYR stream failed. Reason $error")
                                showToast("il dispositivo Polar Verity Sense non è in grado di leggere o impostare le impostazioni per il tipo di misurazione specificato.")
                            },
                            { Log.d(TAG, "GYR stream complete") }
                        )
            } else {
                toggleButtonUp(gyrButton, R.string.start_gyro_stream)
                // NOTE dispose will stop streaming if it is "running"
                gyrDisposable?.dispose()
            }
        }

        magButton.setOnClickListener {
            val isDisposed = magDisposable?.isDisposed ?: true
            if (isDisposed) {
                toggleButtonDown(magButton, R.string.stop_mag_stream)
                magDisposable =
                    requestStreamSettings(veritySenseId, PolarBleApi.PolarDeviceDataType.MAGNETOMETER)
                        .flatMap { settings: PolarSensorSetting ->
                            api.startMagnetometerStreaming(veritySenseId, settings)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { polarMagData: PolarMagnetometerData ->
                                for (data in polarMagData.samples) {
                                    Log.d(TAG, "MAG    x: ${data.x} y: ${data.y} z: ${data.z} timeStamp: ${data.timeStamp}")
                                    val mag = sqrt((((data.x)*(data.x)) + ((data.y)*(data.y)) + ((data.z)*(data.z))).toDouble())

                                    val urlPost = "http://192.168.0.105:3000/aggiungiVS"
                                    val req =
                                        "{ \"heartRate\": \"0\", \"ecg\": \"0\", \"acc\" : \"0\", \"gyro\" : \"0\", \"magnet\" : \"$mag\", \"ppg1\" : \"0\", \"ppg2\" : \"0\", \"ppg3\" : \"0\", \"ppi\" : \"0\", \"emailAddress\": \"$email\" }"
                                    Thread { myConn.post_request(urlPost, req) }.start()
                                }
                                ws.start()
                                //val intent = Intent(this, MainActivity::class.java)
                                countDownTimer = object : CountDownTimer(15000, 1000) {
                                    override fun onTick(millisUntilFinished: Long) {
                                        // Avvia il conteggio alla rovescia

                                        Log.d("TIMER", "Il timer è partito")
                                    }

                                    @RequiresApi(Build.VERSION_CODES.O)
                                    override fun onFinish() {
                                        // Termina il conteggio alla rovescia e fa camminare l'immagine
                                        Log.d("TIMER", "Il timer ha finito")
                                        ws.sendMessage("Non usi il magnetometro del polar vs dal ${nowTime()}")
                                        ws.disconnect()
                                    }
                                }
                                // Avvia il contatore
                                countDownTimer.start()
                                //startActivity(intent)

                            },
                            { error: Throwable ->
                                toggleButtonUp(magButton, R.string.start_mag_stream)
                                Log.e(TAG, "MAGNETOMETER stream failed. Reason $error")
                                showToast("il dispositivo Polar Verity Sense non è in grado di leggere o impostare le impostazioni per il tipo di misurazione specificato.")
                            },
                            { Log.d(TAG, "MAGNETOMETER stream complete") }
                        )
            } else {
                toggleButtonUp(magButton, R.string.start_mag_stream)
                // NOTE dispose will stop streaming if it is "running"
                magDisposable!!.dispose()
            }
        }

        ppgButton.setOnClickListener {
            val isDisposed = ppgDisposable?.isDisposed ?: true
            if (isDisposed) {
                toggleButtonDown(ppgButton, R.string.stop_ppg_stream)
                ppgDisposable =
                    requestStreamSettings(veritySenseId, PolarBleApi.PolarDeviceDataType.PPG)
                        .flatMap { settings: PolarSensorSetting ->
                            api.startPpgStreaming(veritySenseId, settings)
                        }
                        .subscribe(
                            { polarPpgData: PolarPpgData ->
                                if (polarPpgData.type == PolarPpgData.PpgDataType.PPG3_AMBIENT1) {
                                    for (data in polarPpgData.samples) {
                                        Log.d(TAG, "PPG    ppg0: ${data.channelSamples[0]} ppg1: ${data.channelSamples[1]} ppg2: ${data.channelSamples[2]} ambient: ${data.channelSamples[3]} timeStamp: ${data.timeStamp}")

                                        val urlPost = "http://192.168.0.105:3000/aggiungiVS"
                                        val req =
                                            "{ \"heartRate\": \"0\", \"ecg\": \"0\", \"acc\" : \"0\", \"gyro\" : \"0\", \"magnet\" : \"0\", \"ppg1\" : \"${data.channelSamples[0]}\", \"ppg2\" : \"${data.channelSamples[1]}\", \"ppg3\" : \"${data.channelSamples[2]}\", \"ppi\" : \"0\", \"emailAddress\": \"$email\" }"
                                        Thread { myConn.post_request(urlPost, req) }.start()
                                    }
                                    ws.start()
                                    //val intent = Intent(this, MainActivity::class.java)
                                    countDownTimer = object : CountDownTimer(15000, 1000) {
                                        override fun onTick(millisUntilFinished: Long) {
                                            // Avvia il conteggio alla rovescia

                                            Log.d("TIMER", "Il timer è partito")
                                        }

                                        @RequiresApi(Build.VERSION_CODES.O)
                                        override fun onFinish() {
                                            // Termina il conteggio alla rovescia e fa camminare l'immagine
                                            Log.d("TIMER", "Il timer ha finito")
                                            ws.sendMessage("Non misuri il ppg del polar vs dal ${nowTime()}")
                                            ws.disconnect()
                                        }
                                    }
                                    // Avvia il contatore
                                    countDownTimer.start()
                                    //startActivity(intent)

                                }
                            },
                            { error: Throwable ->
                                toggleButtonUp(ppgButton, R.string.start_ppg_stream)
                                Log.e(TAG, "PPG stream failed. Reason $error")
                                showToast("il dispositivo Polar Verity Sense non è in grado di leggere o impostare le impostazioni per il tipo di misurazione specificato.")
                            },
                            { Log.d(TAG, "PPG stream complete") }
                        )
            } else {
                toggleButtonUp(ppgButton, R.string.start_ppg_stream)
                // NOTE dispose will stop streaming if it is "running"
                ppgDisposable?.dispose()
            }
        }

        ppiButton.setOnClickListener {
            val isDisposed = ppiDisposable?.isDisposed ?: true
            if (isDisposed) {
                toggleButtonDown(ppiButton, R.string.stop_ppi_stream)
                ppiDisposable = api.startPpiStreaming(veritySenseId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { ppiData: PolarPpiData ->
                            for (sample in ppiData.samples) {
                                Log.d(TAG + "ppi", "PPI    ppi: ${sample.ppi} blocker: ${sample.blockerBit} errorEstimate: ${sample.errorEstimate}")

                                val urlPost = "http://192.168.0.105:3000/aggiungiVS"
                                val req =
                                    "{ \"heartRate\": \"0\", \"ecg\": \"0\", \"acc\" : \"0\", \"gyro\" : \"0\", \"magnet\" : \"0\", \"ppg1\" : \"0\", \"ppg2\" : \"0\", \"ppg3\" : \"0\", \"ppi\" : \"${sample.ppi}\", \"emailAddress\": \"$email\" }"
                                Thread { myConn.post_request(urlPost, req) }.start()
                                //Log.d(TAG, "HR     bpm: ${sample.hr} rrs: ${sample.rrsMs} rrAvailable: ${sample.rrAvailable} contactStatus: ${sample.contactStatus} contactStatusSupported: ${sample.contactStatusSupported}")
                            }
                            ws.start()
                            //val intent = Intent(this, MainActivity::class.java)
                            countDownTimer = object : CountDownTimer(15000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    // Avvia il conteggio alla rovescia

                                    Log.d("TIMER", "Il timer è partito")
                                }

                                @RequiresApi(Build.VERSION_CODES.O)
                                override fun onFinish() {
                                    // Termina il conteggio alla rovescia e fa camminare l'immagine
                                    Log.d("TIMER", "Il timer ha finito")
                                    ws.sendMessage("Non misuri il ppi del polar vs dal ${nowTime()}")
                                    ws.disconnect()
                                }
                            }
                            // Avvia il contatore
                            countDownTimer.start()
                            //startActivity(intent)
                        },
                        { error: Throwable ->
                            toggleButtonUp(ppiButton, R.string.start_ppi_stream)
                            Log.e(TAG, "PPI stream failed. Reason $error")
                        },
                        { Log.d(TAG, "PPI stream complete") }
                    )
            } else {
                toggleButtonUp(ppiButton, R.string.start_ppi_stream)
                // NOTE dispose will stop streaming if it is "running"
                ppiDisposable?.dispose()
            }
        }

        graphicButton.setOnClickListener {
            val intent = Intent(this, DayVsActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }


        toggleSdkModeButton.setOnClickListener {
            toggleSdkModeButton.isEnabled = false
            if (!sdkModeEnabledStatus) {
                sdkModeEnableDisposable = api.enableSDKMode(veritySenseId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            Log.d(TAG, "SDK mode enabled")
                            // at this point dispose all existing streams. SDK mode enable command
                            // stops all the streams but client is not informed. This is workaround
                            // for the bug.
                            disposeAllStreams()
                            toggleSdkModeButton.isEnabled = true
                            sdkModeEnabledStatus = true
                            toggleButtonDown(toggleSdkModeButton, R.string.disable_sdk_mode)
                        },
                        { error ->
                            toggleSdkModeButton.isEnabled = true
                            val errorString = "SDK mode enable failed: $error"
                            showToast(errorString)
                            Log.e(TAG, errorString)
                        }
                    )
            } else {
                sdkModeEnableDisposable = api.disableSDKMode(veritySenseId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            Log.d(TAG, "SDK mode disabled")
                            toggleSdkModeButton.isEnabled = true
                            sdkModeEnabledStatus = false
                            toggleButtonUp(toggleSdkModeButton, R.string.enable_sdk_mode)
                        },
                        { error ->
                            toggleSdkModeButton.isEnabled = true
                            val errorString = "SDK mode disable failed: $error"
                            showToast(errorString)
                            Log.e(TAG, errorString)
                        }
                    )
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), PERMISSION_REQUEST_CODE)
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (index in 0..grantResults.lastIndex) {
                if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                    disableAllButtons()
                    Log.w(TAG, "No sufficient permissions")
                    showToast("No sufficient permissions")
                    return
                }
            }
            Log.d(TAG, "Needed permissions are granted")
            enableAllButtons()
        }
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        api.foregroundEntered()
    }

    public override fun onDestroy() {
        super.onDestroy()
        api.shutDown()
    }

    private fun toggleButtonDown(button: Button, text: String? = null) {
        toggleButton(button, true, text)
    }

    private fun toggleButtonDown(button: Button, @StringRes resourceId: Int) {
        toggleButton(button, true, getString(resourceId))
    }

    private fun toggleButtonUp(button: Button, text: String? = null) {
        toggleButton(button, false, text)
    }

    private fun toggleButtonUp(button: Button, @StringRes resourceId: Int) {
        toggleButton(button, false, getString(resourceId))
    }

    private fun toggleButton(button: Button, isDown: Boolean, text: String? = null) {
        if (text != null) button.text = text

        var buttonDrawable = button.background
        buttonDrawable = DrawableCompat.wrap(buttonDrawable!!)
        if (isDown) {
            DrawableCompat.setTint(buttonDrawable, resources.getColor(R.color.primaryDarkColor))
        } else {
            DrawableCompat.setTint(buttonDrawable, resources.getColor(R.color.primaryColor))
        }
        button.background = buttonDrawable
    }

    private fun requestStreamSettings(identifier: String, feature: PolarBleApi.PolarDeviceDataType): Flowable<PolarSensorSetting> {
        val availableSettings = api.requestStreamSettings(identifier, feature)
        val allSettings = api.requestFullStreamSettings(identifier, feature)
            .onErrorReturn { error: Throwable ->
                Log.w(TAG, "Full stream settings are not available for feature $feature. REASON: $error")
                PolarSensorSetting(emptyMap())
            }
        return Single.zip(availableSettings, allSettings) { available: PolarSensorSetting, all: PolarSensorSetting ->
            if (available.settings.isEmpty()) {
                throw Throwable("Settings are not available")
            } else {
                Log.d(TAG, "Feature " + feature + " available settings " + available.settings)
                Log.d(TAG, "Feature " + feature + " all settings " + all.settings)
                return@zip android.util.Pair(available, all)
            }
        }
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable()
            .flatMap { sensorSettings: android.util.Pair<PolarSensorSetting, PolarSensorSetting> ->
                DialogUtility.showAllSettingsDialog(
                    this@OptionVsActivity,
                    sensorSettings.first.settings,
                    sensorSettings.second.settings
                ).toFlowable()
            }
    }

    private fun showToast(message: String) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()}
    }

    private fun showSnackbar(message: String) {
        val contextView = findViewById<View>(R.id.buttons_container)
        Snackbar.make(contextView, message, Snackbar.LENGTH_LONG)
            .show()
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                // Respond to positive button press
            }
            .show()
    }

    private fun disableAllButtons() {
        broadcastButton.isEnabled = false
        connectvs.isEnabled = false
        autoConnectButton.isEnabled = false
        scanButton.isEnabled = false
        ecgButton.isEnabled = false
        accButton.isEnabled = false
        gyrButton.isEnabled = false
        magButton.isEnabled = false
        ppgButton.isEnabled = false
        ppiButton.isEnabled = false
        toggleSdkModeButton.isEnabled = false
    }

    private fun enableAllButtons() {
        broadcastButton.isEnabled = true
        connectvs.isEnabled = true
        autoConnectButton.isEnabled = true
        scanButton.isEnabled = true
        ecgButton.isEnabled = true
        accButton.isEnabled = true
        gyrButton.isEnabled = true
        magButton.isEnabled = true
        ppgButton.isEnabled = true
        ppiButton.isEnabled = true

        toggleSdkModeButton.isEnabled = true
    }

    private fun disposeAllStreams() {
        ecgDisposable?.dispose()
        accDisposable?.dispose()
        gyrDisposable?.dispose()
        magDisposable?.dispose()
        ppgDisposable?.dispose()
        ppgDisposable?.dispose()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nowTime(): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        val formattedDateTime = now.format(formatter)
        return formattedDateTime.toString()
    }
}