package com.example.nfctest

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.nfc.tech.NfcV
import java.io.IOException

class NFCService(private val activity: Activity, val log: (value: Any) -> Any) {
    companion object {
        private val validIntents = arrayOf(
            NfcAdapter.ACTION_TAG_DISCOVERED,
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED
        )
    }

    private lateinit var nfcAdapter: NfcAdapter

    var nfcIsAvailable = false
//    private var dispatchIsActive = false

    init {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        nfcIsAvailable = adapter != null
        if (nfcIsAvailable) {
            nfcAdapter = adapter
        }
    }

    fun startListening() {
//        log("Dispatch is active: $dispatchIsActive")
//        if (!dispatchIsActive) {
            val nfcPendingIntent = PendingIntent.getActivity(activity, 0,
                Intent(activity, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE)
            val techListsArray = arrayOf(
                arrayOf(NfcA::class.java.name),
                arrayOf(NfcV::class.java.name),
                arrayOf(Ndef::class.java.name),
                arrayOf(NdefFormatable::class.java.name),
            )
            nfcAdapter.enableForegroundDispatch(activity, nfcPendingIntent, null, techListsArray)
//            dispatchIsActive = true

            log("------ NFC is listening.")
//        }
    }

    fun stopListening() {
//        log("Dispatch is active: $dispatchIsActive")
//        if (dispatchIsActive) {
            nfcAdapter.disableForegroundDispatch(activity)
//            dispatchIsActive = false
//        }
    }

    fun checkAvailability(): Boolean {
        return nfcAdapter.isEnabled
    }

    fun checkIntentValidity(intent: Intent): Boolean {
        return validIntents.contains(intent.action)
    }

    fun requestDataFromDevice(intent: Intent): ByteArray {
        log("Scanning...")
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
                log("------ Invalid response: " + response.size)
            }
        }

        return received;
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
                    log("------ Scan timed out!")
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
}