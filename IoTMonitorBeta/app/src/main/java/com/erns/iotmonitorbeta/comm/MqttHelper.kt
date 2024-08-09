package com.erns.iotmonitorbeta.comm

import android.util.Log
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory


object MqttHelper {
    private val TAG = "MqttHelper"
    private lateinit var mqttClient: MqttClient

    fun connect(serverURI: String, clientId: String) {
        mqttClient =
            MqttClient(
                serverURI,
                clientId,
                org.eclipse.paho.client.mqttv3.persist.MemoryPersistence()
            )
        val options = MqttConnectOptions()
        // Set any necessary connection options (username, password, etc.)
        mqttClient.connect(options)
    }

    fun connect(
        serverURI: String,
        clientId: String,
        username: String? = null,
        password: CharArray? = null
    ) {
        mqttClient = MqttClient(serverURI, MqttClient.generateClientId(), MemoryPersistence())
        val options = MqttConnectOptions()
        options.userName = username // Set username if provided
        options.password = password // Set password if provided
        // Set other connection options as needed (e.g., keepAliveInterval, cleanSession)

//        if (trustStoreInputStream != null) {
//            val sslSocketFactory = SSLUtil.getSocketFactory(trustStoreInputStream)
//            options.socketFactory = sslSocketFactory
//        }

        try {
            mqttClient.connect(options)
        } catch (e: MqttException) {
            // Handle connection errors appropriately
            Log.d(TAG, e.stackTraceToString())
            e.message?.let { Log.d(TAG, it) }
        }
    }

    fun disconnect() {
        if (MqttHelper::mqttClient.isInitialized && mqttClient.isConnected) {
            mqttClient.disconnect()
        }
    }

    fun publish(topic: String, payload: String, qos: Int = 0, retained: Boolean = false) {
        if (MqttHelper::mqttClient.isInitialized && mqttClient.isConnected) {
            val message = MqttMessage(payload.toByteArray())
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message)
        } else {
            Log.d(TAG, "No publish as wrong connection")
        }
    }

    fun subscribe(topic: String, qos: Int = 0) {
        if (MqttHelper::mqttClient.isInitialized && mqttClient.isConnected) {
            mqttClient.subscribe(topic, qos)
        } else {
            Log.d(TAG, "No publish as wrong connection")
        }
    }

    fun setCallback(callback: MqttCallback) {
        if (MqttHelper::mqttClient.isInitialized) {
            mqttClient.setCallback(callback)
        } else {
            Log.d(TAG, "No isInitialized")
        }
    }

    fun isConnected(): Boolean {
        return mqttClient.isConnected
    }
}

object SSLUtil {
    fun getSocketFactory(trustStoreInputStream: InputStream): SSLSocketFactory {
        // Load trust store
        val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
        trustStore.load(trustStoreInputStream, null)

        // Create TrustManagerFactory
        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(trustStore)

        // Create SSLContext
        val sslContext = SSLContext.getInstance("TLS") // Or another appropriate protocol
        sslContext.init(null, trustManagerFactory.trustManagers, null)

        return sslContext.socketFactory
    }
}