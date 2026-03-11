package com.shade.app.network

import com.shade.app.model.RegisterRequest
import com.shade.app.model.RegisterResponse
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object AuthApi {

    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/auth"

    fun register(request: RegisterRequest): RegisterResponse {
        val url = URL("$BASE_URL/register")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val jsonBody = JSONObject().apply {
                put("public_key", request.public_key)
                put("encrypted_private_key", request.encrypted_private_key)
                put("salt", request.salt)
                put("device_model", request.device_model)
                put("fcm_token", request.fcm_token)
            }

            BufferedWriter(OutputStreamWriter(connection.outputStream)).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseText = stream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode !in 200..299) {
                throw Exception(json.optString("error", "register failed"))
            }

            return RegisterResponse(
                core_guard_id = json.getString("core_guard_id"),
                message = json.getString("message")
            )
        } finally {
            connection.disconnect()
        }
    }
}