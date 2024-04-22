package com.example.nfctest

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Date

enum class Endpoints(val url: String) {
    PING("ping") {},
    INSERT("insert") {},
    ;
}

class HttpService() {
    private val url = "https://gmbackend.gutagroup.ru/"
//    private val url = "https://libre-backend.oxton.ru/"
    private val decodingPassword = "YP98747cq3MtcdZr2KTdVqfeDmxmMmvV"
    private val JSON: MediaType = "application/json".toMediaType()
    private val client = OkHttpClient()

    fun sendToDecode(receivedHex: String, date: Date, onResolve: (result: String) -> Unit, onError: (result: String) -> Unit): Unit {
        val payload = "{\n" +
                "    \"data\": \"$receivedHex\",\n" +
                "    \"timestamp\": \"${date.time}\",\n" +
                "    \"password\": \"$decodingPassword\"\n" +
                "}"

        post(Endpoints.INSERT, payload, onResolve, onError)
    }

    @Throws(IOException::class)
    private fun post(endpoint: Endpoints, json: String, onResolve: (result: String) -> Unit, onError: (result: String) -> Unit) {
        val url = url + endpoint.url
        val body: RequestBody = json.toRequestBody(JSON)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
//        Log.d("HTTP", "Sending...")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        val message = "Запрос к серверу не был успешен:\n" +
                                " ${response.code} ${response.message}"
                        onError(message)
                        throw IOException(message)
                    }
                    onResolve(response.body?.string() ?: "")
                }
            }
        })
    }
}