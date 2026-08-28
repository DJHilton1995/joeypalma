package com.example.music

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * High-performance low-latency AudioTrack Player for synthesized 16-bit PCM audio.
 * Provides real-time playback synchronization and live waveform amplitude levels.
 */
class MusicPlayer(private val scope: CoroutineScope) {

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<GeneratedMusicTrack?>(null)
    val currentTrack: StateFlow<GeneratedMusicTrack?> = _currentTrack.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _currentPositionSec = MutableStateFlow(0f)
    val currentPositionSec: StateFlow<Float> = _currentPositionSec.asStateFlow()

    // 16-bar live waveform visualizer levels (0.0 to 1.0)
    private val _visualizerAmplitudes = MutableStateFlow(List(16) { 0.1f })
    val visualizerAmplitudes: StateFlow<List<Float>> = _visualizerAmplitudes.asStateFlow()

    fun playTrack(track: GeneratedMusicTrack) {
        stop()

        _currentTrack.value = track
        val pcm = track.audioDataPcm16
        if (pcm.isEmpty()) return

        val sampleRate = track.sampleRate
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val trackBufferSize = (pcm.size * 2).coerceAtLeast(minBufferSize)

        val trackBuilder = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(trackBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = trackBuilder
        trackBuilder.play()
        _isPlaying.value = true

        playbackJob = scope.launch(Dispatchers.Default) {
            val chunkSize = 2048
            var offset = 0
            val totalSamples = pcm.size

            while (isActive && offset < totalSamples && _isPlaying.value) {
                val samplesToWrite = (totalSamples - offset).coerceAtMost(chunkSize)
                val written = trackBuilder.write(pcm, offset, samplesToWrite)

                if (written > 0) {
                    offset += written
                    val progress = offset.toFloat() / totalSamples.toFloat()
                    _playbackProgress.value = progress
                    _currentPositionSec.value = progress * track.durationSeconds

                    // Compute dynamic visualizer amplitudes from the current audio chunk
                    val chunkEnd = (offset + samplesToWrite).coerceAtMost(totalSamples)
                    val amps = MutableList(16) { 0.08f }
                    val step = (chunkEnd - offset) / 16
                    if (step > 0) {
                        for (b in 0 until 16) {
                            var peak = 0
                            val startIdx = offset + b * step
                            for (s in startIdx until (startIdx + step).coerceAtMost(totalSamples)) {
                                val v = abs(pcm[s].toInt())
                                if (v > peak) peak = v
                            }
                            amps[b] = (peak.toFloat() / 32768.0f).coerceIn(0.08f, 1.0f)
                        }
                        _visualizerAmplitudes.value = amps
                    }
                }
                delay(15)
            }

            if (isActive) {
                // Loop or finish
                _isPlaying.value = false
                _playbackProgress.value = 0f
                _currentPositionSec.value = 0f
                _visualizerAmplitudes.value = List(16) { 0.08f }
            }
        }
    }

    fun togglePlayPause() {
        val current = _currentTrack.value ?: return
        if (_isPlaying.value) {
            pause()
        } else {
            playTrack(current)
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
        } catch (_: Exception) {}
        _visualizerAmplitudes.value = List(16) { 0.08f }
    }

    fun stop() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        _playbackProgress.value = 0f
        _currentPositionSec.value = 0f
        _visualizerAmplitudes.value = List(16) { 0.08f }
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    fun release() {
        stop()
        _currentTrack.value = null
    }
}
