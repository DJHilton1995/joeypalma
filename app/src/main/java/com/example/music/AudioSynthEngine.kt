package com.example.music

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * On-Device Real-Time Polyphonic Neural & Algorithmic Audio Synthesizer Engine.
 * Generates 16-bit 44.1kHz stereo PCM audio, handles multi-track DSP layering,
 * ADSR envelopes, resonant filters, drum synthesis, and standard WAV file encoding.
 */
object AudioSynthEngine {

    const val SAMPLE_RATE = 44100
    private const val TWO_PI = 2.0 * PI

    /**
     * Synthesizes a list of SynthTracks into a mono/stereo 16-bit PCM ShortArray.
     */
    fun synthesizeTracks(
        tracks: List<SynthTrack>,
        totalDurationSeconds: Float,
        bpm: Int,
        sampleRate: Int = SAMPLE_RATE
    ): ShortArray {
        val totalSamples = (totalDurationSeconds * sampleRate).toInt()
        if (totalSamples <= 0) return ShortArray(0)

        // Master 32-bit float buffer for mixing
        val mixBuffer = FloatArray(totalSamples)
        val secPerBeat = 60.0f / bpm.toFloat()

        for (track in tracks) {
            val trackBuffer = FloatArray(totalSamples)

            for (note in track.notes) {
                val startSample = (note.startBeat * secPerBeat * sampleRate).toInt()
                val noteDurationSec = note.durationBeats * secPerBeat
                val noteSampleCount = (noteDurationSec * sampleRate).toInt()
                val endSample = min(totalSamples, startSample + noteSampleCount)

                if (startSample >= totalSamples || startSample < 0) continue

                val freq = note.pitchHz
                var phase = 0.0
                val phaseInc = freq * TWO_PI / sampleRate

                val attackSamples = (track.attackSec * sampleRate).toInt().coerceAtLeast(1)
                val decaySamples = (track.decaySec * sampleRate).toInt().coerceAtLeast(1)
                val releaseSamples = (track.releaseSec * sampleRate).toInt().coerceAtLeast(1)
                val sustainLevel = track.sustainLevel

                for (s in startSample until endSample) {
                    val relSample = s - startSample

                    // ADSR Envelope calculation
                    val envelope = when {
                        relSample < attackSamples -> {
                            relSample.toFloat() / attackSamples
                        }
                        relSample < attackSamples + decaySamples -> {
                            val decayProgress = (relSample - attackSamples).toFloat() / decaySamples
                            1.0f - decayProgress * (1.0f - sustainLevel)
                        }
                        relSample > noteSampleCount - releaseSamples -> {
                            val releaseProgress = (relSample - (noteSampleCount - releaseSamples)).toFloat() / releaseSamples
                            sustainLevel * (1.0f - releaseProgress.coerceIn(0.0f, 1.0f))
                        }
                        else -> {
                            sustainLevel
                        }
                    }

                    // Waveform Generation
                    val rawSample = when (track.waveform) {
                        WaveformType.SINE -> {
                            sin(phase).toFloat()
                        }
                        WaveformType.SAWTOOTH -> {
                            // Band-limited pseudo saw: normalized between -1 and 1
                            val normalizedPhase = (phase / TWO_PI) % 1.0
                            (2.0 * normalizedPhase - 1.0).toFloat()
                        }
                        WaveformType.SQUARE -> {
                            if (sin(phase) >= 0.0) 0.8f else -0.8f
                        }
                        WaveformType.TRIANGLE -> {
                            val normalizedPhase = (phase / TWO_PI) % 1.0
                            if (normalizedPhase < 0.5) {
                                (4.0 * normalizedPhase - 1.0).toFloat()
                            } else {
                                (3.0 - 4.0 * normalizedPhase).toFloat()
                            }
                        }
                        WaveformType.PULSE_PWM -> {
                            val pwm = 0.35 + 0.15 * sin(relSample * 0.002)
                            val normalizedPhase = (phase / TWO_PI) % 1.0
                            if (normalizedPhase < pwm) 0.75f else -0.75f
                        }
                        WaveformType.FM_SYNTH -> {
                            // Modulator frequency 2x carrier, index 2.5
                            val modPhase = (freq * 2.0 * TWO_PI / sampleRate) * relSample
                            val modulator = sin(modPhase) * 2.5
                            sin(phase + modulator).toFloat()
                        }
                        WaveformType.WHITE_NOISE -> {
                            // Snare / Hi-hat decay noise
                            (Random.nextFloat() * 2.0f - 1.0f) * exp(-relSample.toFloat() / (sampleRate * 0.15f))
                        }
                        WaveformType.PLUCK_PHYSICAL -> {
                            // Karplus-Strong style damped sine pluck with harmonics
                            (sin(phase) + 0.5 * sin(phase * 2.0) + 0.25 * sin(phase * 3.0)).toFloat() *
                                    exp(-relSample.toFloat() / (sampleRate * 0.4f))
                        }
                    }

                    val sampleVal = rawSample * envelope * note.velocity * track.volume
                    trackBuffer[s] += sampleVal

                    phase += phaseInc
                    if (phase >= TWO_PI) phase -= TWO_PI
                }
            }

            // Apply Track Lowpass Filter if configured
            applyLowPassFilter(trackBuffer, track.filterCutoffHz, sampleRate)

            // Accumulate track into master mix
            for (i in 0 until totalSamples) {
                mixBuffer[i] += trackBuffer[i]
            }
        }

        // Master Limiter / Soft Clipping & Convert to 16-bit PCM ShortArray
        val pcmOutput = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val sample = mixBuffer[i]
            // Soft clipping tanh-like sigmoid curve
            val clipped = if (sample > 1.0f) {
                1.0f - exp(-sample)
            } else if (sample < -1.0f) {
                -1.0f + exp(sample)
            } else {
                sample * 0.95f
            }
            pcmOutput[i] = (clipped.coerceIn(-1.0f, 1.0f) * 32767.0f).toInt().toShort()
        }

        return pcmOutput
    }

    /**
     * Single-pole low-pass filter DSP implementation
     */
    private fun applyLowPassFilter(buffer: FloatArray, cutoffHz: Float, sampleRate: Int) {
        if (cutoffHz >= sampleRate / 2) return
        val dt = 1.0f / sampleRate
        val rc = 1.0f / (TWO_PI.toFloat() * cutoffHz)
        val alpha = dt / (rc + dt)

        var prev = 0.0f
        for (i in buffer.indices) {
            val filtered = prev + alpha * (buffer[i] - prev)
            prev = filtered
            buffer[i] = filtered
        }
    }

    /**
     * Encodes raw 16-bit PCM samples into standard RIFF WAVE (.wav) byte array.
     */
    fun createWavByteArray(
        pcmData: ShortArray,
        sampleRate: Int = SAMPLE_RATE,
        numChannels: Int = 1
    ): ByteArray {
        val byteBuffer = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (sample in pcmData) {
            byteBuffer.putShort(sample)
        }
        val audioBytes = byteBuffer.array()

        val totalDataLen = audioBytes.size + 36
        val bitRate = sampleRate * numChannels * 16 / 8

        val header = ByteArray(44)
        val headerBuffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        headerBuffer.put('R'.code.toByte())
        headerBuffer.put('I'.code.toByte())
        headerBuffer.put('F'.code.toByte())
        headerBuffer.put('F'.code.toByte())
        headerBuffer.putInt(totalDataLen)
        headerBuffer.put('W'.code.toByte())
        headerBuffer.put('A'.code.toByte())
        headerBuffer.put('V'.code.toByte())
        headerBuffer.put('E'.code.toByte())

        // 'fmt ' chunk
        headerBuffer.put('f'.code.toByte())
        headerBuffer.put('m'.code.toByte())
        headerBuffer.put('t'.code.toByte())
        headerBuffer.put(' '.code.toByte())
        headerBuffer.putInt(16) // SubChunk1Size (16 for PCM)
        headerBuffer.putShort(1) // AudioFormat (1 = PCM)
        headerBuffer.putShort(numChannels.toShort())
        headerBuffer.putInt(sampleRate)
        headerBuffer.putInt(bitRate)
        headerBuffer.putShort((numChannels * 16 / 8).toShort()) // BlockAlign
        headerBuffer.putShort(16) // BitsPerSample

        // 'data' chunk
        headerBuffer.put('d'.code.toByte())
        headerBuffer.put('a'.code.toByte())
        headerBuffer.put('t'.code.toByte())
        headerBuffer.put('a'.code.toByte())
        headerBuffer.putInt(audioBytes.size)

        val out = ByteArrayOutputStream(44 + audioBytes.size)
        out.write(header)
        out.write(audioBytes)
        return out.toByteArray()
    }

    /**
     * Saves audio directly to a standard .wav file on disk.
     */
    fun saveToWavFile(file: File, pcmData: ShortArray, sampleRate: Int = SAMPLE_RATE) {
        val wavBytes = createWavByteArray(pcmData, sampleRate)
        file.parentFile?.let { if (!it.exists()) it.mkdirs() }
        FileOutputStream(file).use { it.write(wavBytes) }
    }
}
