package com.example.nfctest

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
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
            PendingIntent.FLAG_IMMUTABLE)
        val intentFilter = arrayOf(arrayOf(NfcAdapter.ACTION_NDEF_DISCOVERED))
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, null, intentFilter)
        updateState("Просканируйте NFC-метку.")
    }

    override fun onResume() {
        super.onResume()

        handleNFCIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            handleNFCIntent(intent)
        }
    }

    private fun handleNFCIntent(intent: Intent) {
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (rawMessages != null) {
            val ndefMessages = Array<NdefMessage>(rawMessages.size) {rawMessages[it] as NdefMessage}
            processNdefMessages(ndefMessages)
        }
    }

    private fun processNdefMessages(ndefMessages: Array<NdefMessage>) {
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
}