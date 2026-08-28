package com.example.music

/**
 * Fundamental data structures for AI-driven music composition and synthesis.
 */

enum class WaveformType {
    SINE,
    SAWTOOTH,
    TRIANGLE,
    SQUARE,
    PULSE_PWM,
    FM_SYNTH,
    WHITE_NOISE,
    PLUCK_PHYSICAL
}

enum class MusicGenre(
    val displayName: String,
    val emoji: String,
    val defaultBpm: Int,
    val keyScale: String,
    val description: String
) {
    SYNTHWAVE("Cyberpunk Synthwave", "⚡", 128, "D Minor", "Driving 80s basslines, neon supersaw leads, and punchy gated drums"),
    LO_FI("Lo-Fi Chillhop", "☕", 84, "F Major (7th Chords)", "Warm nostalgic vinyl textures, jazzy chords, and laid-back beat"),
    AMBIENT("Deep Space Ambient", "🌌", 65, "A Minor", "Evolving harmonic pads, sub-bass resonance, and floating textures"),
    CHIPTUNE("8-Bit Retro Chiptune", "👾", 140, "C Major", "Vintage pulse-width arpeggios, square-wave melodies, and noise percussion"),
    EDM_FUTURE_BASS("Future Bass / EDM", "🔥", 132, "G Minor", "Detuned sawtooth drops, energetic sidechain rhythms, and bright chords"),
    NEO_CLASSICAL("Neo-Classical Piano", "🎹", 92, "E Minor", "Gentle polyphonic acoustic decay, emotive melodies, and arpeggios"),
    MATRIX_GLITCH("Neural Matrix Glitch", "🧬", 120, "Dorian Mode", "Algorithmic FM bells, micro-tonal modulation, and complex polyrhythms")
}

data class SynthNote(
    val pitchHz: Float,       // Frequency in Hz (e.g. 440.0f for A4)
    val startBeat: Float,     // Beat position in track
    val durationBeats: Float, // Duration in beats
    val velocity: Float = 0.8f // Amplitude (0.0 to 1.0)
)

data class SynthTrack(
    val name: String,
    val waveform: WaveformType,
    val volume: Float = 0.8f,
    val attackSec: Float = 0.01f,
    val decaySec: Float = 0.1f,
    val sustainLevel: Float = 0.7f,
    val releaseSec: Float = 0.2f,
    val filterCutoffHz: Float = 8000f,
    val resonance: Float = 0.2f,
    val notes: List<SynthNote> = emptyList()
)

data class GeneratedMusicTrack(
    val id: String,
    val title: String,
    val prompt: String,
    val genre: MusicGenre,
    val bpm: Int,
    val keyScale: String,
    val durationSeconds: Float,
    val aiModelProvenance: String,
    val audioDataPcm16: ShortArray,
    val sampleRate: Int = 44100,
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GeneratedMusicTrack
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
