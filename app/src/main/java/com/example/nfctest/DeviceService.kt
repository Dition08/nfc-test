package com.example.nfctest

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import java.io.IOException


class DeviceService(private var context: Context, private var vibrator: Vibrator) {
//    private var toneGenerator: ToneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)

    private var defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


    fun ring() {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(context, defaultRingtoneUri)
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
            mediaPlayer.prepare()
            mediaPlayer.setOnCompletionListener { mp -> mp.release() }
            mediaPlayer.start()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }
}