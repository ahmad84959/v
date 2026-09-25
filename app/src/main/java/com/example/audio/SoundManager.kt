package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(private val context: Context) {

  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  var isSoundEnabled: Boolean = true
  var isHapticsEnabled: Boolean = true

  private val sampleRate = 22050
  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  fun playPullSound() {
    if (!isSoundEnabled) return
    coroutineScope.launch {
      playTone(startFreq = 220.0, endFreq = 440.0, durationMs = 60, volume = 0.4f)
    }
  }

  fun playLaunchSound() {
    if (!isSoundEnabled) return
    vibrate(40)
    coroutineScope.launch {
      playTone(startFreq = 480.0, endFreq = 950.0, durationMs = 120, volume = 0.6f)
    }
  }

  fun playHitSound() {
    if (!isSoundEnabled) return
    vibrate(25)
    coroutineScope.launch {
      playNoiseBurst(durationMs = 80, decay = 0.8f, volume = 0.5f)
    }
  }

  fun playExplosionSound() {
    if (!isSoundEnabled) return
    vibrate(100)
    coroutineScope.launch {
      playExplosion(durationMs = 260, volume = 0.8f)
    }
  }

  fun playPigPopSound() {
    if (!isSoundEnabled) return
    vibrate(35)
    coroutineScope.launch {
      playTone(startFreq = 523.25, endFreq = 783.99, durationMs = 110, volume = 0.65f)
    }
  }

  fun playSpecialAbilitySound() {
    if (!isSoundEnabled) return
    vibrate(50)
    coroutineScope.launch {
      playTone(startFreq = 600.0, endFreq = 1200.0, durationMs = 140, volume = 0.7f)
    }
  }

  fun playWinSound() {
    if (!isSoundEnabled) return
    vibrate(80)
    coroutineScope.launch {
      // Fanfare notes: C5, E5, G5, C6
      val notes = listOf(523.25, 659.25, 783.99, 1046.50)
      for (freq in notes) {
        playTone(startFreq = freq, endFreq = freq, durationMs = 90, volume = 0.6f)
        Thread.sleep(40)
      }
    }
  }

  fun playLoseSound() {
    if (!isSoundEnabled) return
    vibrate(60)
    coroutineScope.launch {
      val notes = listOf(440.0, 392.0, 349.23, 293.66)
      for (freq in notes) {
        playTone(startFreq = freq, endFreq = freq, durationMs = 100, volume = 0.5f)
        Thread.sleep(40)
      }
    }
  }

  private fun vibrate(durationMs: Long) {
    if (!isHapticsEnabled || vibrator == null) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
          VibrationEffect.createOneShot(
            durationMs,
            VibrationEffect.DEFAULT_AMPLITUDE
          )
        )
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(durationMs)
      }
    } catch (_: Exception) {
    }
  }

  private fun playTone(
    startFreq: Double,
    endFreq: Double,
    durationMs: Int,
    volume: Float
  ) {
    try {
      val numSamples = (durationMs * sampleRate) / 1000
      val buffer = ShortArray(numSamples)
      var currentPhase = 0.0

      for (i in 0 until numSamples) {
        val progress = i.toDouble() / numSamples
        val currentFreq = startFreq + (endFreq - startFreq) * progress
        currentPhase += 2.0 * Math.PI * currentFreq / sampleRate
        // Envelope: quick attack, smooth release
        val envelope = (1.0 - progress).coerceIn(0.0, 1.0)
        val sample = (sin(currentPhase) * envelope * volume * Short.MAX_VALUE).toInt()
        buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
      }

      writeAndPlay(buffer)
    } catch (_: Exception) {
    }
  }

  private fun playNoiseBurst(durationMs: Int, decay: Float, volume: Float) {
    try {
      val numSamples = (durationMs * sampleRate) / 1000
      val buffer = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val progress = i.toFloat() / numSamples
        val envelope = (1.0f - progress * decay).coerceIn(0.0f, 1.0f)
        val noise = (Random.nextFloat() * 2.0f - 1.0f)
        val sample = (noise * envelope * volume * Short.MAX_VALUE).toInt()
        buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
      }

      writeAndPlay(buffer)
    } catch (_: Exception) {
    }
  }

  private fun playExplosion(durationMs: Int, volume: Float) {
    try {
      val numSamples = (durationMs * sampleRate) / 1000
      val buffer = ShortArray(numSamples)
      var phase = 0.0

      for (i in 0 until numSamples) {
        val progress = i.toDouble() / numSamples
        val currentFreq = 160.0 * (1.0 - progress * 0.7)
        phase += 2.0 * Math.PI * currentFreq / sampleRate
        val tone = sin(phase)
        val noise = Random.nextDouble() * 2.0 - 1.0
        val mix = (tone * 0.5 + noise * 0.5)
        val envelope = (1.0 - progress).coerceIn(0.0, 1.0)
        val sample = (mix * envelope * volume * Short.MAX_VALUE).toInt()
        buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
      }

      writeAndPlay(buffer)
    } catch (_: Exception) {
    }
  }

  private fun writeAndPlay(buffer: ShortArray) {
    var audioTrack: AudioTrack? = null
    try {
      val minBufSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
      )
      val bufSize = maxOf(minBufSize, buffer.size * 2)

      val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

      val format = AudioFormat.Builder()
        .setSampleRate(sampleRate)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .build()

      audioTrack = AudioTrack(
        attributes,
        format,
        bufSize,
        AudioTrack.MODE_STATIC,
        android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
      )

      audioTrack.write(buffer, 0, buffer.size)
      audioTrack.play()

      // Allow static buffer playback
      Thread.sleep((buffer.size * 1000L / sampleRate) + 20)
    } catch (_: Exception) {
    } finally {
      try {
        audioTrack?.stop()
        audioTrack?.release()
      } catch (_: Exception) {
      }
    }
  }
}
