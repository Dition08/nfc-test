package com.example.nfctest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Date

class MainActivity : ComponentActivity() {
    private val httpService = HttpService()
    private lateinit var nfcService: NFCService
    private lateinit var deviceService: DeviceService

    private lateinit var textView: TextView
    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)
        textView = findViewById(R.id.textView)
        logView = findViewById(R.id.logs)
//        val button: Button = findViewById(R.id.button)

        nfcService = NFCService(this) { value: Any -> log(value) }
        deviceService = DeviceService(this, getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)

//        val bytes = byteArrayOf(0x00.toByte(),0x47.toByte(),0x08.toByte(),0x7b.toByte(),0x06.toByte(),0x00.toByte(),0x03.toByte(),0xb9.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x13.toByte(),0x76.toByte(),0x21.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x9b.toByte(),0xaa.toByte(),0x05.toByte(),0x1a.toByte(),0x95.toByte(),0x04.toByte(),0xc8.toByte(),0x94.toByte(),0x5a.toByte(),0x00.toByte(),0xa0.toByte(),0x04.toByte(),0xc8.toByte(),0x90.toByte(),0x5a.toByte(),0x00.toByte(),0xb1.toByte(),0x04.toByte(),0xc8.toByte(),0x64.toByte(),0x9a.toByte(),0x00.toByte(),0xba.toByte(),0x04.toByte(),0xc8.toByte(),0x5c.toByte(),0x5a.toByte(),0x00.toByte(),0xc2.toByte(),0x04.toByte(),0xc8.toByte(),0x74.toByte(),0x5a.toByte(),0x00.toByte(),0x28.toByte(),0x04.toByte(),0xc8.toByte(),0xf0.toByte(),0x59.toByte(),0x00.toByte(),0x30.toByte(),0x04.toByte(),0xc8.toByte(),0xfc.toByte(),0x59.toByte(),0x00.toByte(),0x2e.toByte(),0x04.toByte(),0xc8.toByte(),0x34.toByte(),0x5a.toByte(),0x00.toByte(),0x34.toByte(),0x04.toByte(),0xc8.toByte(),0x50.toByte(),0x9a.toByte(),0x00.toByte(),0x3d.toByte(),0x04.toByte(),0xc8.toByte(),0x60.toByte(),0x5a.toByte(),0x00.toByte(),0x48.toByte(),0x04.toByte(),0xc8.toByte(),0x6c.toByte(),0x5a.toByte(),0x00.toByte(),0x57.toByte(),0x04.toByte(),0xc8.toByte(),0x78.toByte(),0x5a.toByte(),0x00.toByte(),0x62.toByte(),0x04.toByte(),0xc8.toByte(),0x84.toByte(),0x5a.toByte(),0x00.toByte(),0x6d.toByte(),0x04.toByte(),0xc8.toByte(),0x8c.toByte(),0x1a.toByte(),0x80.toByte(),0x79.toByte(),0x04.toByte(),0xc8.toByte(),0x90.toByte(),0x5a.toByte(),0x00.toByte(),0x87.toByte(),0x04.toByte(),0xc8.toByte(),0x94.toByte(),0x5a.toByte(),0x00.toByte(),0xfe.toByte(),0x03.toByte(),0xc8.toByte(),0x38.toByte(),0x5b.toByte(),0x00.toByte(),0xf4.toByte(),0x03.toByte(),0xc8.toByte(),0xbc.toByte(),0x5a.toByte(),0x00.toByte(),0xf9.toByte(),0x03.toByte(),0xc8.toByte(),0x88.toByte(),0x5a.toByte(),0x00.toByte(),0xfd.toByte(),0x03.toByte(),0xc8.toByte(),0x08.toByte(),0x5b.toByte(),0x00.toByte(),0x0e.toByte(),0x04.toByte(),0xc8.toByte(),0x58.toByte(),0x5a.toByte(),0x00.toByte(),0x18.toByte(),0x04.toByte(),0xc8.toByte(),0xa0.toByte(),0x5a.toByte(),0x00.toByte(),0x25.toByte(),0x04.toByte(),0xc8.toByte(),0x48.toByte(),0x5a.toByte(),0x00.toByte(),0xc1.toByte(),0x04.toByte(),0xc8.toByte(),0xb4.toByte(),0x5a.toByte(),0x00.toByte(),0x94.toByte(),0x05.toByte(),0xc8.toByte(),0x2c.toByte(),0x5b.toByte(),0x00.toByte(),0x43.toByte(),0x06.toByte(),0xc8.toByte(),0x0c.toByte(),0x9a.toByte(),0x00.toByte(),0x1f.toByte(),0x06.toByte(),0xc8.toByte(),0x9c.toByte(),0x59.toByte(),0x00.toByte(),0x16.toByte(),0x06.toByte(),0xc8.toByte(),0x48.toByte(),0x99.toByte(),0x00.toByte(),0xbd.toByte(),0x05.toByte(),0xc8.toByte(),0xb0.toByte(),0x58.toByte(),0x00.toByte(),0x68.toByte(),0x05.toByte(),0xc8.toByte(),0x98.toByte(),0x97.toByte(),0x00.toByte(),0xde.toByte(),0x04.toByte(),0xc8.toByte(),0x7c.toByte(),0x98.toByte(),0x00.toByte(),0x05.toByte(),0x05.toByte(),0xc8.toByte(),0xb0.toByte(),0x98.toByte(),0x00.toByte(),0x96.toByte(),0x04.toByte(),0xc8.toByte(),0x34.toByte(),0x99.toByte(),0x00.toByte(),0x77.toByte(),0x04.toByte(),0xc8.toByte(),0xb0.toByte(),0x99.toByte(),0x00.toByte(),0xa4.toByte(),0x04.toByte(),0xc8.toByte(),0xc8.toByte(),0x99.toByte(),0x00.toByte(),0x63.toByte(),0x04.toByte(),0xc8.toByte(),0x34.toByte(),0xda.toByte(),0x00.toByte(),0x7d.toByte(),0x04.toByte(),0xc8.toByte(),0x64.toByte(),0x5a.toByte(),0x00.toByte(),0x8b.toByte(),0x04.toByte(),0xc8.toByte(),0x3c.toByte(),0x9a.toByte(),0x00.toByte(),0xc4.toByte(),0x04.toByte(),0xc8.toByte(),0x44.toByte(),0x5b.toByte(),0x00.toByte(),0x4e.toByte(),0x04.toByte(),0xc8.toByte(),0x50.toByte(),0x98.toByte(),0x00.toByte(),0x87.toByte(),0x03.toByte(),0xc8.toByte(),0x34.toByte(),0x5a.toByte(),0x00.toByte(),0x1a.toByte(),0x04.toByte(),0xc8.toByte(),0xf0.toByte(),0x59.toByte(),0x00.toByte(),0xe8.toByte(),0x04.toByte(),0xc8.toByte(),0x90.toByte(),0x5b.toByte(),0x00.toByte(),0x51.toByte(),0x04.toByte(),0xc8.toByte(),0xf0.toByte(),0x5a.toByte(),0x00.toByte(),0xdd.toByte(),0x03.toByte(),0xc8.toByte(),0xe8.toByte(),0x5a.toByte(),0x00.toByte(),0xb5.toByte(),0x03.toByte(),0xc8.toByte(),0xbc.toByte(),0x5a.toByte(),0x00.toByte(),0xf4.toByte(),0x03.toByte(),0xc8.toByte(),0xa8.toByte(),0x5a.toByte(),0x00.toByte(),0x01.toByte(),0x04.toByte(),0xc8.toByte(),0xc4.toByte(),0x5a.toByte(),0x00.toByte(),0x76.toByte(),0x21.toByte(),0x00.toByte(),0x00.toByte(),0xe0.toByte(),0xef.toByte(),0x00.toByte(),0x08.toByte(),0xef.toByte(),0x0e.toByte(),0xb9.toByte(),0x50.toByte(),0x14.toByte(),0x07.toByte(),0x96.toByte(),0x80.toByte(),0x5a.toByte(),0x00.toByte(),0xed.toByte(),0xa6.toByte(),0x0a.toByte(),0x70.toByte(),0x1a.toByte(),0xc8.toByte(),0x04.toByte(),0xc6.toByte(),0x99.toByte(),0x66.toByte(),0xae.toByte(),0xf9.toByte(),0x21.toByte(),0x83.toByte(),0xf2.toByte(),0x90.toByte(),0x07.toByte(),0x00.toByte(),0x06.toByte(),0x08.toByte(),0x02.toByte(),0x24.toByte(),0x0c.toByte(),0x43.toByte(),0x17.toByte(),0x3c.toByte())
//        button.setOnClickListener { notifyServer( bytes, Date() ) }
        if (nfcService.nfcIsAvailable) {
            updateState(Status.SCAN)
        } else {
            updateState(Status.NOTSUPPORTED)
        }
    }

    override fun onStop() {
        super.onStop()

        log("Stopping...")

        if (nfcService.nfcIsAvailable) {
            nfcService.stopListening()
        }
    }

    override fun onResume() {
        super.onResume()

        log("Resuming...")

        if (nfcService.nfcIsAvailable) {
            nfcService.startListening()
        }

        handleNFCIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleNFCIntent(intent)
    }

    private fun handleNFCIntent(intent: Intent) {
        log("Processing intent:\n${intent.action}")
        if (nfcService.checkIntentValidity(intent)) {
            val received: ByteArray
//            try {
                log("Scanning...")
                received = nfcService.requestDataFromDevice(intent)
//            } catch (error: Error) {
//                notifyUser("${Status.SCANFAILURE}\n$error")
//                log("------ NFC error:\n$error")
//                return
//            }
            val measureDate = Date()
            notifyServer(received, measureDate)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun notifyServer(received: ByteArray, measureDate: Date) {
        val receivedHex = received.toHexString()
        var answer = "Failed to receive data from server."
//        try {
            log("Making HTTP request...")
            answer = httpService.sendToDecode(receivedHex, measureDate)
//        } catch (error: Error) {
//            notifyUser("${Status.HTTPFAILURE}\n$error")
//            log("------ HTTP request error: $error")
//        }
        log("------ Received: $answer")

        notifyUser(Status.SUCСESS)
    }

    private fun notifyUser(value: Any) {
        updateState(value)
        deviceService.vibrate()
        deviceService.ring()
    }

    private fun updateState(value: Any) {
        textView.text = value.toString()
    }

    private fun log(value: Any) {
        logView.text = logView.text.toString().plus("\n$value")
        Log.d("APP", "$value")
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