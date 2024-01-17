package com.example.nfctest

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
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

class MainActivity : ComponentActivity() {
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
        updateState("Получен новый intent, " + intent.action)

        handleNFCIntent(intent)
    }

    private fun handleNFCIntent(intent: Intent) {

        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
//            updateState("Обрабатывается intent.")
//            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
//                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
//                updateState("Messages size: " + messages.size)
//                processNdefMessages(messages)
//            }
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMessages != null) {
                val ndefMessages = List<NdefMessage>(rawMessages.size) {rawMessages[it] as NdefMessage}
                processNdefMessages(ndefMessages)
            } else updateState("Сообщения пусты.")
//            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        }
    }

    private fun processNdefMessages(ndefMessages: List<NdefMessage>) {
        var result = "NFC Data:"

        for (ndefMessage in ndefMessages) {
            for (record in ndefMessage.records) {
                val text = String(record.payload)
                result += "\n$text"
            }
        }

        updateState(result)
    }

    private fun updateState(text: String) {
        textView.text = text
    }

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