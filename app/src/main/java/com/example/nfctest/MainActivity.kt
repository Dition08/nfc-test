package com.example.nfctest

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.nfc.tech.NfcV
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.io.IOException
import java.util.Date

class MainActivity : ComponentActivity() {
    companion object {
        private val validIntents = arrayOf(
            NfcAdapter.ACTION_TAG_DISCOVERED,
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED
        )
    }
    private var nfcAdapter: NfcAdapter? = null
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var deviceService: DeviceService
    private lateinit var textView: TextView

    //    private lateinit var logView: TextView
    private val httpService = HttpService()
//    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout)
        textView = findViewById(R.id.textView)
//        logView = findViewById(R.id.logs)
//        button = findViewById(R.id.button)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        deviceService = DeviceService(this, getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)

//        val bytes = byteArrayOf(0x00.toByte(),0x47.toByte(),0x08.toByte(),0x7b.toByte(),0x06.toByte(),0x00.toByte(),0x03.toByte(),0xb9.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x13.toByte(),0x76.toByte(),0x21.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x9b.toByte(),0xaa.toByte(),0x05.toByte(),0x1a.toByte(),0x95.toByte(),0x04.toByte(),0xc8.toByte(),0x94.toByte(),0x5a.toByte(),0x00.toByte(),0xa0.toByte(),0x04.toByte(),0xc8.toByte(),0x90.toByte(),0x5a.toByte(),0x00.toByte(),0xb1.toByte(),0x04.toByte(),0xc8.toByte(),0x64.toByte(),0x9a.toByte(),0x00.toByte(),0xba.toByte(),0x04.toByte(),0xc8.toByte(),0x5c.toByte(),0x5a.toByte(),0x00.toByte(),0xc2.toByte(),0x04.toByte(),0xc8.toByte(),0x74.toByte(),0x5a.toByte(),0x00.toByte(),0x28.toByte(),0x04.toByte(),0xc8.toByte(),0xf0.toByte(),0x59.toByte(),0x00.toByte(),0x30.toByte(),0x04.toByte(),0xc8.toByte(),0xfc.toByte(),0x59.toByte(),0x00.toByte(),0x2e.toByte(),0x04.toByte(),0xc8.toByte(),0x34.toByte(),0x5a.toByte(),0x00.toByte(),0x34.toByte(),0x04.toByte(),0xc8.toByte(),0x50.toByte(),0x9a.toByte(),0x00.toByte(),0x3d.toByte(),0x04.toByte(),0xc8.toByte(),0x60.toByte(),0x5a.toByte(),0x00.toByte(),0x48.toByte(),0x04.toByte(),0xc8.toByte(),0x6c.toByte(),0x5a.toByte(),0x00.toByte(),0x57.toByte(),0x04.toByte(),0xc8.toByte(),0x78.toByte(),0x5a.toByte(),0x00.toByte(),0x62.toByte(),0x04.toByte(),0xc8.toByte(),0x84.toByte(),0x5a.toByte(),0x00.toByte(),0x6d.toByte(),0x04.toByte(),0xc8.toByte(),0x8c.toByte(),0x1a.toByte(),0x80.toByte(),0x79.toByte(),0x04.toByte(),0xc8.toByte(),0x90.toByte(),0x5a.toByte(),0x00.toByte(),0x87.toByte(),0x04.toByte(),0xc8.toByte(),0x94.toByte(),0x5a.toByte(),0x00.toByte(),0xfe.toByte(),0x03.toByte(),0xc8.toByte(),0x38.toByte(),0x5b.toByte(),0x00.toByte(),0xf4.toByte(),0x03.toByte(),0xc8.toByte(),0xbc.toByte(),0x5a.toByte(),0x00.toByte(),0xf9.toByte(),0x03.toByte(),0xc8.toByte(),0x88.toByte(),0x5a.toByte(),0x00.toByte(),0xfd.toByte(),0x03.toByte(),0xc8.toByte(),0x08.toByte(),0x5b.toByte(),0x00.toByte(),0x0e.toByte(),0x04.toByte(),0xc8.toByte(),0x58.toByte(),0x5a.toByte(),0x00.toByte(),0x18.toByte(),0x04.toByte(),0xc8.toByte(),0xa0.toByte(),0x5a.toByte(),0x00.toByte(),0x25.toByte(),0x04.toByte(),0xc8.toByte(),0x48.toByte(),0x5a.toByte(),0x00.toByte(),0xc1.toByte(),0x04.toByte(),0xc8.toByte(),0xb4.toByte(),0x5a.toByte(),0x00.toByte(),0x94.toByte(),0x05.toByte(),0xc8.toByte(),0x2c.toByte(),0x5b.toByte(),0x00.toByte(),0x43.toByte(),0x06.toByte(),0xc8.toByte(),0x0c.toByte(),0x9a.toByte(),0x00.toByte(),0x1f.toByte(),0x06.toByte(),0xc8.toByte(),0x9c.toByte(),0x59.toByte(),0x00.toByte(),0x16.toByte(),0x06.toByte(),0xc8.toByte(),0x48.toByte(),0x99.toByte(),0x00.toByte(),0xbd.toByte(),0x05.toByte(),0xc8.toByte(),0xb0.toByte(),0x58.toByte(),0x00.toByte(),0x68.toByte(),0x05.toByte(),0xc8.toByte(),0x98.toByte(),0x97.toByte(),0x00.toByte(),0xde.toByte(),0x04.toByte(),0xc8.toByte(),0x7c.toByte(),0x98.toByte(),0x00.toByte(),0x05.toByte(),0x05.toByte(),0xc8.toByte(),0xb0.toByte(),0x98.toByte(),0x00.toByte(),0x96.toByte(),0x04.toByte(),0xc8.toByte(),0x34.toByte(),0x99.toByte(),0x00.toByte(),0x77.toByte(),0x04.toByte(),0xc8.toByte(),0xb0.toByte(),0x99.toByte(),0x00.toByte(),0xa4.toByte(),0x04.toByte(),0xc8.toByte(),0xc8.toByte(),0x99.toByte(),0x00.toByte(),0x63.toByte(),0x04.toByte(),0xc8.toByte(),0x34.toByte(),0xda.toByte(),0x00.toByte(),0x7d.toByte(),0x04.toByte(),0xc8.toByte(),0x64.toByte(),0x5a.toByte(),0x00.toByte(),0x8b.toByte(),0x04.toByte(),0xc8.toByte(),0x3c.toByte(),0x9a.toByte(),0x00.toByte(),0xc4.toByte(),0x04.toByte(),0xc8.toByte(),0x44.toByte(),0x5b.toByte(),0x00.toByte(),0x4e.toByte(),0x04.toByte(),0xc8.toByte(),0x50.toByte(),0x98.toByte(),0x00.toByte(),0x87.toByte(),0x03.toByte(),0xc8.toByte(),0x34.toByte(),0x5a.toByte(),0x00.toByte(),0x1a.toByte(),0x04.toByte(),0xc8.toByte(),0xf0.toByte(),0x59.toByte(),0x00.toByte(),0xe8.toByte(),0x04.toByte(),0xc8.toByte(),0x90.toByte(),0x5b.toByte(),0x00.toByte(),0x51.toByte(),0x04.toByte(),0xc8.toByte(),0xf0.toByte(),0x5a.toByte(),0x00.toByte(),0xdd.toByte(),0x03.toByte(),0xc8.toByte(),0xe8.toByte(),0x5a.toByte(),0x00.toByte(),0xb5.toByte(),0x03.toByte(),0xc8.toByte(),0xbc.toByte(),0x5a.toByte(),0x00.toByte(),0xf4.toByte(),0x03.toByte(),0xc8.toByte(),0xa8.toByte(),0x5a.toByte(),0x00.toByte(),0x01.toByte(),0x04.toByte(),0xc8.toByte(),0xc4.toByte(),0x5a.toByte(),0x00.toByte(),0x76.toByte(),0x21.toByte(),0x00.toByte(),0x00.toByte(),0xe0.toByte(),0xef.toByte(),0x00.toByte(),0x08.toByte(),0xef.toByte(),0x0e.toByte(),0xb9.toByte(),0x50.toByte(),0x14.toByte(),0x07.toByte(),0x96.toByte(),0x80.toByte(),0x5a.toByte(),0x00.toByte(),0xed.toByte(),0xa6.toByte(),0x0a.toByte(),0x70.toByte(),0x1a.toByte(),0xc8.toByte(),0x04.toByte(),0xc6.toByte(),0x99.toByte(),0x66.toByte(),0xae.toByte(),0xf9.toByte(),0x21.toByte(),0x83.toByte(),0xf2.toByte(),0x90.toByte(),0x07.toByte(),0x00.toByte(),0x06.toByte(),0x08.toByte(),0x02.toByte(),0x24.toByte(),0x0c.toByte(),0x43.toByte(),0x17.toByte(),0x3c.toByte())
//        button.setOnClickListener { notifyServer( bytes, Date() ) }

        if (nfcAdapter != null) {
            updateState(Status.SCAN)
            handleNFCIntent(intent)
        } else {
            updateState(Status.NOTSUPPORTED)
        }
    }

//    override fun onStart() {
//        super.onStart()
//
//        log("Starting...")
//    }

    override fun onResume() {
        super.onResume()

//        log("Resuming...")
        if (nfcAdapter != null) {
            if (!nfcAdapter!!.isEnabled) openNfcSettings()
            startListening(nfcAdapter!!)
        }
    }

    override fun onPause() {
        super.onPause()

//        log("Pausing...")

        if (nfcAdapter != null) stopListening(nfcAdapter!!)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

//        log("Processing new intent...")

        setIntent(intent)
        handleNFCIntent(intent)
    }

    private fun startListening(adapter: NfcAdapter) {
//        log("Dispatch is active: $dispatchIsActive")
//        if (!dispatchIsActive) {
        val nfcPendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE)
        val techListsArray = arrayOf(
            arrayOf(NfcA::class.java.name),
            arrayOf(NfcV::class.java.name),
            arrayOf(Ndef::class.java.name),
            arrayOf(NdefFormatable::class.java.name),
        )
        adapter.enableForegroundDispatch(this, nfcPendingIntent, null, techListsArray)
//            dispatchIsActive = true

        Log.d("NFC", "------ NFC is listening.")
//        }
    }

    private fun stopListening(adapter: NfcAdapter) {
//        log("Dispatch is active: $dispatchIsActive")
//        if (dispatchIsActive) {
        adapter.disableForegroundDispatch(this)
//            dispatchIsActive = false
//        }
    }

    private fun openNfcSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_NFC)
        } else {
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        }
        startActivity(intent)
    }

    private fun handleNFCIntent(intent: Intent) {
//        log("Processing intent:\n${intent.action}")
//        updateState(Status)
        if (validIntents.contains(intent.action)) {
            val received: ByteArray = requestDataFromDevice(intent)
//            try {
//            } catch (error: Error) {
//                notifyUser("${Status.SCANFAILURE}\n$error")
//                log("------ NFC error:\n$error")
//                return
//            }
            val measureDate = Date()
            notifyServer(received, measureDate)
        }
    }

    private fun requestDataFromDevice(intent: Intent): ByteArray {
        Log.d("NFC", "Scanning...")
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
                    notifyUser("${Notification.SCANFAILURE}")
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

    @OptIn(ExperimentalStdlibApi::class)
    private fun notifyServer(received: ByteArray, measureDate: Date) {
        val receivedHex = received.toHexString()
        val onResolve: (String) -> Unit = {result: String ->
            handler.post {
                notifyUser(Notification.SUCСESS)
//                log("------ Received: $result")
            }
        }
        val onError: (String) -> Unit = {err: String ->
            handler.post {
                notifyUser("${Notification.HTTPFAILURE}\n$err")
//                log("------ HTTP request error: $err")
            }
        }

//        log("Making HTTP request...")
        httpService.sendToDecode(receivedHex, measureDate, onResolve, onError)
    }

    private fun notifyUser(value: Any, callback: (() -> Unit)? = null) {
        val dialog = NotificationDialogFragment()
        dialog.message = value.toString()
        dialog.callback = callback
        val fragmentManager = this.getFragmentManager()
        dialog.show(fragmentManager, "Notification")
        deviceService.vibrate()
        deviceService.ring()
    }

    private fun updateState(value: Any) {
        textView.text = value.toString()
    }

    private fun log(value: Any) {
//        logView.text = logView.text.toString().plus("\n$value")
        Log.d("APP", "$value")
    }
}

class NotificationDialogFragment : DialogFragment() {
    var callback: (() -> Unit)? = null
    var message = ""
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(message)
                .setPositiveButton("OK") { dialog, id ->
                    if (callback != null) {
                        callback?.invoke()
                    }
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}