package com.erns.iotmonitorbeta

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.erns.iotmonitorbeta.comm.MqttHelper
import com.erns.iotmonitorbeta.customview.HeartRateMonitor
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val MQTT_TOPIC_IOT = "topic/iot"
    private val MQTT_TOPIC_MOBILE = "topic/mobile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val heartRateMonitor = findViewById<HeartRateMonitor>(R.id.heartRateMonitor)
        val btnCmdOn= findViewById<Button>(R.id.btnCmdOn)
        val btnCmdOff= findViewById<Button>(R.id.btnCmdOff)
        val txtValue = findViewById<TextView>(R.id.txtValue)
        val txtMqttStatus = findViewById<TextView>(R.id.txtMqttStatus)
        val switchMqtt = findViewById<SwitchMaterial>(R.id.switch1)

        heartRateMonitor.setVerticalRange(100f)

//        btnSendCommand.setOnClickListener {
//            val value = Random.nextInt(0, 100)
//            heartRateMonitor.setValue(value * 1f)
//            txtValue.text = value.toString()
//        }

        btnCmdOn.setOnClickListener {
            val value = Random.nextInt(0, 100)
            MqttHelper.publish(MQTT_TOPIC_MOBILE, "ON", 0, false)
            Log.d("TAG", "publishing ... $MQTT_TOPIC_MOBILE")
        }

        btnCmdOff.setOnClickListener {
            val value = Random.nextInt(0, 100)
            MqttHelper.publish(MQTT_TOPIC_MOBILE, "OFF", 0, false)
            Log.d("TAG", "publishing ... $MQTT_TOPIC_MOBILE")
        }

        val callbackStatus: (String) -> Unit = {
            txtMqttStatus.text = it
        }

        val callbackResponse: (String) -> Unit = {
            txtValue.text = it
        }

        switchMqtt.isChecked = false
        switchMqtt.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                startMqtt(callbackStatus, callbackResponse)
            } else {
                stopMqtt()
            }
        }


    }

    fun stopMqtt() {
        MqttHelper.disconnect()
    }

    fun startMqtt(callbackStatus: (String) -> Unit, callbackResponse: (String) -> Unit) {
        val job = Job()
        val scope = CoroutineScope(job)
        lifecycleScope.launch {
            try {

                MqttHelper.connect(
                    "tcp://broker.emqx.io:1883",
                    "UsertestID2",
                    "emqx",
                    "public".toCharArray()
                )
                MqttHelper.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) { //connectionState = "Disconnected"
                        callbackStatus("Disconnected")
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        val messageReceived = message?.payload?.toString(Charsets.UTF_8) ?: ""
                        Log.d("TAG", "messageArrived: $messageReceived")
                        callbackResponse(messageReceived)
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        // Handle delivery completion if needed
                    }
                })

                callbackStatus("Connected")
            } catch (e: MqttException) {
                val connectionState = "Connection failed: ${e.message}"
                callbackStatus(connectionState)
            }
        }

        if (MqttHelper.isConnected()) {
            MqttHelper.subscribe(MQTT_TOPIC_IOT, 0)
            Log.d("TAG", "subscribing ... $MQTT_TOPIC_IOT")
        }

    }


}