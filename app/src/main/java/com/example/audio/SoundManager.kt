package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    private val sampleRate = 44100
    private var laserTrack: AudioTrack? = null
    private var explosionTrack: AudioTrack? = null
    private var powerupTrack: AudioTrack? = null
    private var bombTrack: AudioTrack? = null
    private var hitTrack: AudioTrack? = null

    init {
        try {
            laserTrack = createStaticTrack(generateLaserPcm())
            explosionTrack = createStaticTrack(generateExplosionPcm())
            powerupTrack = createStaticTrack(generatePowerupPcm())
            bombTrack = createStaticTrack(generateBombPcm())
            hitTrack = createStaticTrack(generateHitPcm())
        } catch (_: Exception) {
            // AudioTrack fallback
        }
    }

    private fun createStaticTrack(pcmData: ShortArray): AudioTrack? {
        return try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcmData.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(pcmData, 0, pcmData.size)
            track
        } catch (_: Exception) {
            null
        }
    }

    private fun generateLaserPcm(): ShortArray {
        val durationMs = 90
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 900.0 - (progress * 550.0) // 900Hz down to 350Hz
            val angle = 2.0 * Math.PI * freq * i / sampleRate
            val envelope = (1.0 - progress)
            pcm[i] = (sin(angle) * Short.MAX_VALUE * 0.45 * envelope).toInt().toShort()
        }
        return pcm
    }

    private fun generateExplosionPcm(): ShortArray {
        val durationMs = 220
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        val random = Random(42)
        var lastVal = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val noise = (random.nextDouble() * 2.0 - 1.0)
            // Low-pass filter noise for bassy explosion
            lastVal = lastVal * 0.7 + noise * 0.3
            val envelope = (1.0 - progress) * (1.0 - progress)
            pcm[i] = (lastVal * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()
        }
        return pcm
    }

    private fun generatePowerupPcm(): ShortArray {
        val durationMs = 200
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        val notes = doubleArrayOf(440.0, 554.37, 659.25, 880.0)
        val step = numSamples / notes.size
        for (i in 0 until numSamples) {
            val noteIdx = (i / step).coerceIn(0, notes.size - 1)
            val freq = notes[noteIdx]
            val angle = 2.0 * Math.PI * freq * i / sampleRate
            val envelope = 0.8
            pcm[i] = (sin(angle) * Short.MAX_VALUE * 0.4 * envelope).toInt().toShort()
        }
        return pcm
    }

    private fun generateBombPcm(): ShortArray {
        val durationMs = 450
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        val random = Random(99)
        var filter = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 120.0 - (progress * 70.0)
            val sinWave = sin(2.0 * Math.PI * freq * i / sampleRate)
            val noise = (random.nextDouble() * 2.0 - 1.0)
            filter = filter * 0.85 + noise * 0.15
            val combined = (sinWave * 0.6 + filter * 0.4)
            val envelope = (1.0 - progress)
            pcm[i] = (combined * Short.MAX_VALUE * 0.8 * envelope).toInt().toShort()
        }
        return pcm
    }

    private fun generateHitPcm(): ShortArray {
        val durationMs = 60
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 200.0 - (progress * 80.0)
            val angle = 2.0 * Math.PI * freq * i / sampleRate
            val envelope = (1.0 - progress)
            pcm[i] = (sin(angle) * Short.MAX_VALUE * 0.5 * envelope).toInt().toShort()
        }
        return pcm
    }

    fun playLaser() {
        if (!soundEnabled) return
        playTrack(laserTrack)
    }

    fun playExplosion() {
        if (soundEnabled) {
            playTrack(explosionTrack)
        }
        vibrate(35, 120)
    }

    fun playPowerup() {
        if (soundEnabled) {
            playTrack(powerupTrack)
        }
        vibrate(20, 90)
    }

    fun playBomb() {
        if (soundEnabled) {
            playTrack(bombTrack)
        }
        vibrate(80, 255)
    }

    fun playHit() {
        if (soundEnabled) {
            playTrack(hitTrack)
        }
        vibrate(25, 150)
    }

    private fun playTrack(track: AudioTrack?) {
        scope.launch {
            try {
                track?.let {
                    if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        it.stop()
                    }
                    it.setPlaybackHeadPosition(0)
                    it.play()
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs,
                        amplitude.coerceIn(1, 255)
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    fun release() {
        try {
            laserTrack?.release()
            explosionTrack?.release()
            powerupTrack?.release()
            bombTrack?.release()
            hitTrack?.release()
        } catch (_: Exception) {
        }
    }
}
