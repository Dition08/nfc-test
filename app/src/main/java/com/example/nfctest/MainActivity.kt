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

    private val states = arrayOf(
        null,
        "sensor not yet started",
        "sensor in warm up phase",
        "sensor ready and working (up to 14 days and twelve hours)",
        "sensor expired (for the following twelve hours, FRAM data section content does not change any more)",
        "sensor shutdown",
        "sensor failure"
    )
    private val dataBlockLength = 6
    private val trendDataBlocksAmount = 16
    private val historyDataBlocksAmount = 32
    private val modifier = 180f
    private interface DataBody {
        var crc: ByteArray
        var nextTrendBlock: Int
        var nextHistoryBlock: Int
        var trendDataBlocks: Array<ByteArray>
        var historyDataBlocks: Array<ByteArray>
    }

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
            showReport(received)
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

    //----------------------------------------------------------------------------------------------

    private fun showReport(hex: ByteArray) {
        val hexFragmented = fragmentHex(hex)
        val sensorState = hexFragmented.sliceArray(IntRange(0, 3));
        val dataBody = hexFragmented.sliceArray(IntRange(3, 40));
        val footer = hexFragmented.sliceArray(IntRange(40, 42));
        val unknown = hexFragmented.sliceArray(IntRange(42, 44));
        val bodyParsed = parseBody(dataBody);

        var report = ""
        report += "\nsensorState: $sensorState"
        report += "\nOUTPUT:"
        report += prepareReport(sensorState, bodyParsed);
        updateState(report)
    }

    private fun translateBytesToNumber(bytes: ByteArray): Int {
        return (256 * (bytes[1].toInt() and 0xFF) + (bytes[0].toInt() and 0xFF)) and 0x1FFF;
    }

    private fun fragmentHex(hex: ByteArray): Array<ByteArray> {
        val result: ArrayList<ByteArray> = arrayListOf()
        var i = 0
        var j = 0
        while (i < hex.size) {
            val fragment = hex.sliceArray(IntRange(i, i + 7))
            result.add(fragment)
            j++
            i += 8
        }

        return result.toTypedArray();
    }

    private fun extractSensorState(sensorState: Array<ByteArray>): String {
        var result = states[sensorState[0][4].toInt()];
        if (result == null) result = "Unknown state"
        return result;
    }

    private fun parseBody(body: Array<ByteArray>): DataBody {
        val result: DataBody = object : DataBody {
            override var crc: ByteArray = byteArrayOf()
            override var nextTrendBlock: Int = 0
            override var nextHistoryBlock: Int = 0
            override var trendDataBlocks: Array<ByteArray> = arrayOf()
            override var historyDataBlocks: Array<ByteArray> = arrayOf()
        };

        result.crc = body[0].sliceArray(IntRange(0, 2))
        result.nextTrendBlock = body[0][2].toInt()
        result.nextHistoryBlock = body[0][3].toInt()
        result.trendDataBlocks = extractDataBlocks(body, 0, 4, dataBlockLength, trendDataBlocksAmount);
        result.historyDataBlocks = extractDataBlocks(body, 12, 4, dataBlockLength, historyDataBlocksAmount);

        return result;
    }

    private fun extractDataBlocks(body: Array<ByteArray>, startingBlock: Int, startingByte: Int, blockLength: Int, blockAmount: Int): Array<ByteArray> {
        val result: ArrayList<ByteArray> = arrayListOf()

        var i = startingBlock
        var j = startingByte
        var k = 0
        while (k < blockAmount) {
            var l = 0
            val dataBlock = arrayListOf<Byte>()
            while (l < blockLength) {
                dataBlock.add(body[i][j])
                if (++j >= body[i].size) {
                    i++
                    j = 0
                }
                l++
            }
            result.add(dataBlock.toByteArray())
            k++
        }

        return result.toTypedArray();
    }

    private fun prepareReport(sensorState: Array<ByteArray>, body: DataBody): String {
        var result = "\nDevice readings:"
        result += "\n Current sensor state is: " + extractSensorState(sensorState)

        result += "\n Trend data blocks, bytes:" + showDataBlocks(body.trendDataBlocks)
        result += "\n History data blocks, bytes:" + showDataBlocks(body.historyDataBlocks)
        result += "\n Trend data blocks, raw values:" + convertDataBlocksToString(body.trendDataBlocks)
        result += "\n History data blocks, raw values:" + convertDataBlocksToString(body.historyDataBlocks)
        result += "\n Trend data blocks, mmol/L (supposedly):" + convertDataBlocksTommolString(body.trendDataBlocks)
        result += "\n History data blocks, mmol/L (supposedly):" + convertDataBlocksTommolString(body.historyDataBlocks)

        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun showDataBlocks(dataBlocks: Array<ByteArray>): String {
        var result = ""

        for ((i, dataBlock) in dataBlocks.withIndex()) {
            result += "\n  $i: ${dataBlock.toHexString()}"
        }

        return result
    }

    private fun convertDataBlocksToString(dataBlocks: Array<ByteArray>): String {
        var result = ""

        for ((i, dataBlock) in dataBlocks.withIndex()) {
            result += "\n  $i: " + translateBytesToNumber(dataBlock)
        }

        return result
    }

    private fun convertDataBlocksTommolString(dataBlocks: Array<ByteArray>): String {
        var result = ""

        for ((i, dataBlock) in dataBlocks.withIndex()) {
            result += "\n  $i: " + translateBytesToNumber(dataBlock)/modifier
        }

        return result
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