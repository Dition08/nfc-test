package com.example.nfctest

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.nfc.tech.NfcV
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class MainActivity : ComponentActivity() {
    private val decodingUrl = "https://libre-backend.oxton.ru/insert"
    private val decodingPassword = "YP98747cq3MtcdZr2KTdVqfeDmxmMmvV"

    private val JSON: MediaType = "application/json".toMediaType()
    private var client = OkHttpClient()

    private lateinit var textView: TextView
    private lateinit var nfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)
        val adapter = NfcAdapter.getDefaultAdapter(this)

        textView = findViewById(R.id.textView)
        val button: Button = findViewById(R.id.button)

        if (adapter == null) {
            updateState("NFC не поддерживается на данном устройстве.")
        } else {
            nfcAdapter = adapter
            updateState("...")
            button.setOnClickListener { startListeningToNFC() }
        }
    }

    private fun startListeningToNFC() {
        val nfcPendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE)
        val techListsArray = arrayOf(
            arrayOf(NfcA::class.java.name),
            arrayOf(NfcV::class.java.name),
            arrayOf(Ndef::class.java.name),
            arrayOf(NdefFormatable::class.java.name),
        )
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, null, techListsArray)
        updateState("Просканируйте NFC-метку.")
    }

    override fun onResume() {
        super.onResume()

        handleNFCIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleNFCIntent(intent)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun handleNFCIntent(intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            val handle = NfcV.get(tag)
            val received = ByteArray(360)
            for (i in 0 until 43 step 3) {
                val cmd = byteArrayOf(
                    0x02.toByte(),
                    0x23.toByte(),
                    i.toByte(),
                    0x02.toByte()
                )
                val response = sendCmd(handle, cmd)

                if (response.size == 25) {
                    response.copyInto(received, i * 8, 1, response.size)
                } else {
                    Log.d("NFC", "------ Invalid response: " + response.size)
                }
            }
            val receivedHex = received.toHexString()
            Log.d("NFC", "------ Received: $receivedHex")


            val payload = "{\n" +
                    "    \"data\": \"$receivedHex\",\n" +
                    "    \"password\": \"$decodingPassword\"\n" +
                    "}"
            val answer = runBlocking {
                sendToDecode(decodingUrl, payload)
            }
            Log.d("POST", "------ Received: $answer")
            updateState(answer)
        }
    }

    private fun sendCmd(handle: NfcV, cmd: ByteArray): ByteArray {
        val startTime = System.currentTimeMillis()
        while (true) {
            try {
                if (handle.isConnected) {
                    handle.close()
                }
                handle.connect()
                val received = handle.transceive(cmd)
                handle.close()
                return received
            } catch (ioException: IOException) {
                if (System.currentTimeMillis() > startTime + 3000) {
//                    Toast.makeText(mainActivityRef.get(), "Scan timed out!", Toast.LENGTH_SHORT).show()
                    Log.d("NFC", "------ Scan timed out!")
                    return byteArrayOf()
                }
                try {
                    Thread.sleep(100)
                } catch (interruptedException: InterruptedException) {
                    return byteArrayOf()
                }
            }
        }
    }

    @Throws(IOException::class)
    suspend fun sendToDecode(url: String, json: String): String {
        return withContext(Dispatchers.IO) {
            val body: RequestBody = json.toRequestBody(JSON)
            val request: Request = Request.Builder()
                .url(url)
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            response.body?.string() ?: ""
        }
    }

    private fun updateState(text: String) {
        textView.text = text
    }

//    private fun handleNFCIntent(intent: Intent) {
//
//        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
//            intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
////            updateState("Обрабатывается intent.")
////            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
////                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
////                updateState("Messages size: " + messages.size)
////                processNdefMessages(messages)
////            }
//            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
//            if (rawMessages != null) {
//                val ndefMessages = List<NdefMessage>(rawMessages.size) {rawMessages[it] as NdefMessage}
//                processNdefMessages(ndefMessages)
//            } else updateState("Сообщения пусты.")
////            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
//        }
//    }

//    private fun processNdefMessages(ndefMessages: List<NdefMessage>) {
//        var result = "NFC Data:"
//
//        for (ndefMessage in ndefMessages) {
//            for (record in ndefMessage.records) {
//                val text = String(record.payload)
//                result += "\n$text"
//            }
//        }
//
//        updateState(result)
//    }

//    //Saving test
//    data class MyObject(val name: String, val age: Int)
//
//    fun main() {
//        val myObject = MyObject("John", 30)
//        val gson = Gson()
//        val json = gson.toJson(myObject)
//        File("path/to/file.json").writeText(json)
//    }
}