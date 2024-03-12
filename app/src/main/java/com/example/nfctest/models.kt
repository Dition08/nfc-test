package com.example.nfctest

import java.io.IOException
import java.util.Date

class DeviceData(bytes: Array<ByteArray>, measureDate: Date) {
    companion object {
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
    }

    val sensor = SensorData(bytes.sliceArray(0 until 4));
    val body = DataBody(bytes.sliceArray(3 until 41), measureDate);
    val footer = bytes.sliceArray(IntRange(40, 42));
    val unknown = bytes.sliceArray(IntRange(42, 44));

    constructor(formlessHex: ByteArray, measureDate: Date) : this(
        fragmentHex(formlessHex),
        measureDate
    )

    fun prepareReport(): String {
        return "Device readings:" +
            "\n ${sensor.prepareSensorReport()}" +
            "\n ${body.prepareDataReport()}";
    }
}

class SensorData(bytes: Array<ByteArray>) {
    companion object {
        private val states = arrayOf(
            null,
            "sensor not yet started",
            "sensor in warm up phase",
            "sensor ready and working (up to 14 days and twelve hours)",
            "sensor expired (for the following twelve hours, FRAM data section content does not change any more)",
            "sensor shutdown",
            "sensor failure"
        )
    }
    private val body: Array<ByteArray> = bytes;
    private val state = bytes[0][4].toInt();

    fun prepareSensorReport(): String {
        var result = states[state];
        if (result == null) result = "Unknown state"
        return "Current sensor state is: $result";
    }
}

class DataBody(bytes: Array<ByteArray>, val measureDate: Date) {
    val crc: ByteArray = bytes[0].sliceArray(IntRange(0, 2));
    val nextTrendBlock: Int = bytes[0][2].toInt();
    val nextHistoryBlock: Int = bytes[0][3].toInt();
    val trendDataBlocks: DataBlocks = DataBlocks(bytes, "trend");
    val historyDataBlocks: DataBlocks = DataBlocks(bytes, "history");

    fun prepareDataReport(): String {
        val measureTime = measureDate.time;

        return " Trend data blocks, bytes: ${trendDataBlocks.showDataBlocks(measureTime)}" +
            "\n History data blocks, bytes: ${historyDataBlocks.showDataBlocks(measureTime)}" +
            "\n Trend data blocks, raw values: ${trendDataBlocks.showRawDataBlocks(measureTime)}" +
            "\n History data blocks, raw values: ${historyDataBlocks.showRawDataBlocks(measureTime)}" +
            "\n Trend data blocks, mmol/L (supposedly): ${trendDataBlocks.showMMolDataBlocks(measureTime)}" +
            "\n History data blocks, mmol/L (supposedly): ${historyDataBlocks.showMMolDataBlocks(measureTime)}";
    }
}

class DataBlocks(body: Array<ByteArray>, type: String) {
    private interface DataBlocksConfig {
        val startingBlock: Int
        val startingByte: Int
        val blockLength: Int
        val blockAmount: Int
        val timeStep: Int
        val timeOffset: Int
    }

    companion object {
        private val configs = mapOf<String, DataBlocksConfig>(
            "trend" to object: DataBlocksConfig {
                override val startingBlock = 0
                override val startingByte = 4
                override val blockLength = 6
                override val blockAmount = 16
                override val timeStep = 5
                override val timeOffset = 0
            },
            "history" to object: DataBlocksConfig {
                override val startingBlock = 12
                override val startingByte = 4
                override val blockLength = 6
                override val blockAmount = 32
                override val timeStep = 15
                override val timeOffset = 80
            }
        )
    }

    val blocks: ArrayList<DataBlock> = arrayListOf();

    init {
        val config = configs[type]
        if (config != null) {
            extractDataBlocks(body, config);
        } else {
            throw IOException("Unknown DataBlocksConfig type: $type");
        };
    }

    private fun extractDataBlocks(body: Array<ByteArray>, info: DataBlocksConfig) {
        var i = info.startingBlock;
        var j = info.startingByte;
        for (k in 0 until info.blockAmount) {
            val block: ArrayList<Byte> = arrayListOf();
            for (l in 0 until info.blockLength) {
                block.add(body[i][j]);
                if (++j == body[i].count()) {
                    i++;
                    j = 0;
                }
            }
            blocks.add(DataBlock(block.toByteArray(), k*info.timeStep + info.timeOffset));
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun showDataBlocks(measureTime: Long): String {
        return compileDataBlocks(measureTime) { block -> block.block.toHexString() }
    }

    fun showRawDataBlocks(measureTime: Long): String {
        return compileDataBlocks(measureTime) { block -> block.getRaw().toString() }
    }

    fun showMMolDataBlocks(measureTime: Long): String {
        return compileDataBlocks(measureTime) { block -> block.getMMol().toString() }
    }

    private fun compileDataBlocks(measureTime: Long, extractData: (DataBlock) -> String): String {
        var result = "";

        for (i in 0 until blocks.count()) {
            result += "\n  $i: ${extractData(blocks[i])}, ${Date(measureTime - blocks[i].timeElapsed)}";
        }

        return result;
    }
}

class DataBlock(bytes: ByteArray, minutes: Int) {
    companion object {
        private const val rawToMMolModifier = 180;
    }

    val block: ByteArray = bytes;
    private var raw: Int = readBits(bytes, 0, 0, 15);
    val timeElapsed: Long = minutes.toLong() * 60000;

    fun getRaw(): Int {
        return raw;
    }

    fun getMMol(): Float {
        return raw.toFloat()/rawToMMolModifier;
    }

//    private fun translateBytesToNumber(bytes: ByteArray): Int {
//        return (256 * (bytes[1].toInt() and 0xFF) + (bytes[0].toInt() and 0xFF)) and 0x1FFF;
//    }

    private fun readBits(bytes: ByteArray, byteOffset: Int, bitOffset: Int, bitCount: Int): Int {
        var res = 0;
        for (i in 0 until bitCount) {
            val totalBitOffset = byteOffset * 8 + bitOffset + i;
            val byte1 = totalBitOffset / 8;
            val bit = totalBitOffset % 8;
            if ((bytes[byte1].toInt() shr bit and 0x1) == 1) {
                res = res or (1 shl i);
            }
        }
        return res;
    }
}