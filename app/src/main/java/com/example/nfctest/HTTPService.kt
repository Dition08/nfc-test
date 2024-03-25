package com.example.nfctest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Date

class HttpService() {
    private val decodingUrl = "https://gmbackend.gutagroup.ru/insert"
    private val decodingPassword = "YP98747cq3MtcdZr2KTdVqfeDmxmMmvV"
    private val JSON: MediaType = "application/json".toMediaType()
    private val client = OkHttpClient()

    fun sendToDecode(receivedHex: String, date: Date): String {
        val payload = "{\n" +
                "    \"data\": \"$receivedHex\",\n" +
                "    \"timestamp\": \"${date.time}\",\n" +
                "    \"password\": \"$decodingPassword\"\n" +
                "}"

        return runBlocking {post(decodingUrl, payload)}
    }
    @Throws(IOException::class)
    suspend fun post(url: String, json: String): String {
        return withContext(Dispatchers.IO) {
            val body: RequestBody = json.toRequestBody(JSON)
            val request: Request = Request.Builder()
                .url(url)
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            response.body?.string() ?: ""
        }
    }
}