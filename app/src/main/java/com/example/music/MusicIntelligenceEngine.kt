package com.example.music

import java.util.UUID
import kotlin.math.pow
import kotlin.random.Random

/**
 * AI Music Intelligence Engine:
 * Converts natural language prompts into musical parameters, chord progressions,
 * multi-track polyphony, and synthesizes audio using the local DSP synth engine.
 */
object MusicIntelligenceEngine {

    // Standard Note Frequency calculation (A4 = 440 Hz)
    private fun noteToFreq(midiNote: Int): Float {
        return (440.0 * 2.0.pow((midiNote - 69).toDouble() / 12.0)).toFloat()
    }

    // Common MIDI Note Definitions
    private const val C2 = 36
    private const val D2 = 38
    private const val E2 = 40
    private const val F2 = 41
    private const val G2 = 43
    private const val A2 = 45
    private const val B2 = 47
    private const val C3 = 48
    private const val D3 = 50
    private const val E3 = 52
    private const val F3 = 53
    private const val G3 = 55
    private const val A3 = 57
    private const val B3 = 59
    private const val C4 = 60
    private const val D4 = 62
    private const val E4 = 64
    private const val F4 = 65
    private const val G4 = 67
    private const val A4 = 69
    private const val B4 = 71
    private const val C5 = 72
    private const val D5 = 74
    private const val E5 = 76
    private const val F5 = 77
    private const val G5 = 79
    private const val A5 = 81

    /**
     * Interprets a user's prompt and composes a full multi-track song.
     */
    fun composeFromPrompt(prompt: String, forcedGenre: MusicGenre? = null): GeneratedMusicTrack {
        val lower = prompt.lowercase()

        val genre = forcedGenre ?: when {
            lower.contains("synth") || lower.contains("cyberpunk") || lower.contains("retro") || lower.contains("wave") || lower.contains("80s") -> MusicGenre.SYNTHWAVE
            lower.contains("lo-fi") || lower.contains("lofi") || lower.contains("chill") || lower.contains("relax") || lower.contains("study") || lower.contains("coffee") -> MusicGenre.LO_FI
            lower.contains("ambient") || lower.contains("space") || lower.contains("meditat") || lower.contains("drone") || lower.contains("sleep") -> MusicGenre.AMBIENT
            lower.contains("8-bit") || lower.contains("chiptune") || lower.contains("arcade") || lower.contains("game boy") || lower.contains("mario") -> MusicGenre.CHIPTUNE
            lower.contains("edm") || lower.contains("future bass") || lower.contains("dance") || lower.contains("drop") || lower.contains("club") || lower.contains("techno") -> MusicGenre.EDM_FUTURE_BASS
            lower.contains("piano") || lower.contains("classical") || lower.contains("acoustic") || lower.contains("orchestra") || lower.contains("interstellar") -> MusicGenre.NEO_CLASSICAL
            lower.contains("glitch") || lower.contains("matrix") || lower.contains("cyber") || lower.contains("idm") || lower.contains("ai") -> MusicGenre.MATRIX_GLITCH
            else -> MusicGenre.SYNTHWAVE
        }

        val bpm = genre.defaultBpm
        val totalBeats = 32f // 8 bars of 4/4
        val durationSeconds = (totalBeats * 60f) / bpm.toFloat()

        val tracks = when (genre) {
            MusicGenre.SYNTHWAVE -> composeSynthwave(totalBeats)
            MusicGenre.LO_FI -> composeLofi(totalBeats)
            MusicGenre.AMBIENT -> composeAmbient(totalBeats)
            MusicGenre.CHIPTUNE -> composeChiptune(totalBeats)
            MusicGenre.EDM_FUTURE_BASS -> composeEdm(totalBeats)
            MusicGenre.NEO_CLASSICAL -> composeNeoClassical(totalBeats)
            MusicGenre.MATRIX_GLITCH -> composeMatrixGlitch(totalBeats)
        }

        val pcmData = AudioSynthEngine.synthesizeTracks(
            tracks = tracks,
            totalDurationSeconds = durationSeconds,
            bpm = bpm
        )

        val bestModels = MusicModelRegistry.findBestModelsForGenre(genre)
        val provenanceSummary = bestModels.joinToString(", ") { "${it.name} (${it.organization})" }

        val trackTitle = generateTrackTitle(genre, prompt)

        return GeneratedMusicTrack(
            id = UUID.randomUUID().toString(),
            title = trackTitle,
            prompt = prompt,
            genre = genre,
            bpm = bpm,
            keyScale = genre.keyScale,
            durationSeconds = durationSeconds,
            aiModelProvenance = provenanceSummary,
            audioDataPcm16 = pcmData
        )
    }

    private fun generateTrackTitle(genre: MusicGenre, prompt: String): String {
        val prefixes = when (genre) {
            MusicGenre.SYNTHWAVE -> listOf("Neon Circuit", "Midnight Highway", "Cyber Sunset", "Retrofuturistic Echo")
            MusicGenre.LO_FI -> listOf("Rainy Cafe", "Late Night Study", "Warm Memories", "Vinyl Morning")
            MusicGenre.AMBIENT -> listOf("Stellar Drift", "Nebula Reverie", "Cosmic Horizon", "Silent Abyss")
            MusicGenre.CHIPTUNE -> listOf("Pixel Quest", "Level 99 Boss", "Arcade Odyssey", "Bit Hopper")
            MusicGenre.EDM_FUTURE_BASS -> listOf("Neon Pulse", "Velocity Surge", "Hyper Beam", "Euphoria Drop")
            MusicGenre.NEO_CLASSICAL -> listOf("Nocturne in E Minor", "Winter Solitude", "Celestial Étude", "Aura of Time")
            MusicGenre.MATRIX_GLITCH -> listOf("Quantum Singularity", "Neural Feedback", "Synaptic Pulse", "Zero-Day Echo")
        }
        val prefix = prefixes.random()
        return if (prompt.length in 3..25) "$prefix - ${prompt.trim().replaceFirstChar { it.uppercase() }}" else prefix
    }

    // ==========================================
    // GENRE COMPOSERS
    // ==========================================

    private fun composeSynthwave(totalBeats: Float): List<SynthTrack> {
        val bassNotes = mutableListOf<SynthNote>()
        val leadNotes = mutableListOf<SynthNote>()
        val padNotes = mutableListOf<SynthNote>()
        val drumNotes = mutableListOf<SynthNote>()

        // 4-chord progression in D Minor: Dm -> Bb -> F -> C (each 8 beats = 2 bars)
        val chordRoots = listOf(D2, 34 /* Bb1 */, F2, C2)
        val chordTriads = listOf(
            listOf(D3, F3, A3),
            listOf(34 + 24 /* Bb3 */, D4, F4),
            listOf(F3, A3, C4),
            listOf(C3, E3, G3)
        )

        // 1. Driving 16th-note Bassline (0.25 beat per step)
        var beat = 0f
        var chordIdx = 0
        while (beat < totalBeats) {
            val root = chordRoots[chordIdx % chordRoots.size]
            // Octave bounce
            val pitch = if ((beat * 4).toInt() % 2 == 0) root else root + 12
            bassNotes.add(SynthNote(noteToFreq(pitch), beat, 0.22f, 0.9f))
            beat += 0.25f
            if (beat % 8f == 0f) chordIdx++
        }

        // 2. Lush Synth Pads (sustaining 4 beats each)
        beat = 0f
        chordIdx = 0
        while (beat < totalBeats) {
            val triad = chordTriads[chordIdx % chordTriads.size]
            for (m in triad) {
                padNotes.add(SynthNote(noteToFreq(m), beat, 3.8f, 0.5f))
            }
            beat += 4f
            if (beat % 8f == 0f) chordIdx++
        }

        // 3. 80s Neon Lead Melody
        val melodyMotif = listOf(
            D4, F4, A4, D5, C5, A4, G4, F4,
            D4, E4, F4, G4, A4, F4, E4, D4
        )
        for (i in 0 until (totalBeats / 2).toInt()) {
            val noteMidi = melodyMotif[i % melodyMotif.size]
            leadNotes.add(SynthNote(noteToFreq(noteMidi), i * 2f, 1.8f, 0.75f))
        }

        // 4. Punchy 80s Drum Pattern: Kick on 1 & 3, Snare on 2 & 4, Hi-Hats on 8ths
        beat = 0f
        while (beat < totalBeats) {
            val beatInBar = beat % 4f
            if (beatInBar == 0f || beatInBar == 2f) {
                // Synthetic Kick
                drumNotes.add(SynthNote(60f, beat, 0.15f, 1.0f))
            }
            if (beatInBar == 1f || beatInBar == 3f) {
                // Snare noise
                drumNotes.add(SynthNote(180f, beat, 0.2f, 0.85f))
            }
            // Hi-hat on every 0.5 beat
            drumNotes.add(SynthNote(8000f, beat, 0.05f, 0.4f))
            drumNotes.add(SynthNote(8000f, beat + 0.5f, 0.05f, 0.3f))
            beat += 1f
        }

        return listOf(
            SynthTrack("Bassline", WaveformType.SAWTOOTH, volume = 0.75f, attackSec = 0.01f, decaySec = 0.1f, sustainLevel = 0.5f, releaseSec = 0.1f, filterCutoffHz = 2200f, notes = bassNotes),
            SynthTrack("Neon Lead", WaveformType.PULSE_PWM, volume = 0.7f, attackSec = 0.05f, decaySec = 0.2f, sustainLevel = 0.8f, releaseSec = 0.3f, filterCutoffHz = 5500f, notes = leadNotes),
            SynthTrack("Warm Pads", WaveformType.SAWTOOTH, volume = 0.45f, attackSec = 0.3f, decaySec = 0.5f, sustainLevel = 0.9f, releaseSec = 0.6f, filterCutoffHz = 3000f, notes = padNotes),
            SynthTrack("Drums", WaveformType.WHITE_NOISE, volume = 0.65f, attackSec = 0.005f, decaySec = 0.15f, sustainLevel = 0.1f, releaseSec = 0.1f, filterCutoffHz = 6000f, notes = drumNotes)
        )
    }

    private fun composeLofi(totalBeats: Float): List<SynthTrack> {
        val epNotes = mutableListOf<SynthNote>()
        val bassNotes = mutableListOf<SynthNote>()
        val leadNotes = mutableListOf<SynthNote>()
        val beatNotes = mutableListOf<SynthNote>()

        // F Major 7th -> Dm9 -> Gm7 -> C7
        val jazzChords = listOf(
            listOf(F3, A3, C4, E4),       // Fmaj7
            listOf(D3, F3, A3, C4, E4),   // Dm9
            listOf(G3, 34 + 24 /*Bb3*/, D4, F4), // Gm7
            listOf(C3, E3, G3, 34 + 24 /*Bb3*/)  // C7
        )
        val bassRoots = listOf(F2, D2, G2, C2)

        var beat = 0f
        var chordIdx = 0
        while (beat < totalBeats) {
            val chord = jazzChords[chordIdx % jazzChords.size]
            val bass = bassRoots[chordIdx % bassRoots.size]

            // Electric Piano Chords (gentle strumming feel)
            var strumOffset = 0f
            for (pitch in chord) {
                epNotes.add(SynthNote(noteToFreq(pitch), beat + strumOffset, 3.2f, 0.6f))
                strumOffset += 0.04f
            }

            // Warm Bassline
            bassNotes.add(SynthNote(noteToFreq(bass), beat, 1.8f, 0.8f))
            bassNotes.add(SynthNote(noteToFreq(bass + 7), beat + 2f, 1.5f, 0.65f))

            beat += 4f
            chordIdx++
        }

        // Chill Melody
        val melodyNotes = listOf(A4, C5, D5, C5, A4, G4, F4, G4, A4, F4)
        var melBeat = 2f
        var mIdx = 0
        while (melBeat < totalBeats - 2f) {
            val note = melodyNotes[mIdx % melodyNotes.size]
            leadNotes.add(SynthNote(noteToFreq(note), melBeat, 1.2f, 0.55f))
            melBeat += if (mIdx % 2 == 0) 2f else 3f
            mIdx++
        }

        // Laid-back Beat: Kick on 1 & 2.5, Snare on 2 & 4
        beat = 0f
        while (beat < totalBeats) {
            beatNotes.add(SynthNote(55f, beat, 0.18f, 0.8f)) // Kick
            beatNotes.add(SynthNote(55f, beat + 1.66f, 0.12f, 0.6f)) // Swing Kick
            beatNotes.add(SynthNote(220f, beat + 1f, 0.15f, 0.7f)) // Snare
            beatNotes.add(SynthNote(220f, beat + 3f, 0.15f, 0.7f)) // Snare
            // Relaxed hi-hats
            for (h in 0 until 4) {
                beatNotes.add(SynthNote(6000f, beat + h * 0.5f, 0.04f, 0.25f))
            }
            beat += 4f
        }

        return listOf(
            SynthTrack("Electric Piano", WaveformType.SINE, volume = 0.7f, attackSec = 0.02f, decaySec = 0.4f, sustainLevel = 0.6f, releaseSec = 0.5f, filterCutoffHz = 2800f, notes = epNotes),
            SynthTrack("Sub Bass", WaveformType.TRIANGLE, volume = 0.8f, attackSec = 0.05f, decaySec = 0.2f, sustainLevel = 0.8f, releaseSec = 0.3f, filterCutoffHz = 800f, notes = bassNotes),
            SynthTrack("Gentle Lead", WaveformType.FM_SYNTH, volume = 0.5f, attackSec = 0.08f, decaySec = 0.3f, sustainLevel = 0.5f, releaseSec = 0.4f, filterCutoffHz = 3200f, notes = leadNotes),
            SynthTrack("Chill Drums", WaveformType.WHITE_NOISE, volume = 0.6f, attackSec = 0.005f, decaySec = 0.2f, sustainLevel = 0.1f, releaseSec = 0.1f, filterCutoffHz = 4500f, notes = beatNotes)
        )
    }

    private fun composeAmbient(totalBeats: Float): List<SynthTrack> {
        val droneNotes = mutableListOf<SynthNote>()
        val celestialNotes = mutableListOf<SynthNote>()

        // A Minor / E Minor vast expanding chords
        val ambientPitches = listOf(A2, E3, A3, C4, E4, G4, B4, D5)

        var beat = 0f
        while (beat < totalBeats) {
            for (p in ambientPitches) {
                val delay = Random.nextFloat() * 2f
                val dur = 6f + Random.nextFloat() * 4f
                droneNotes.add(SynthNote(noteToFreq(p), beat + delay, dur, 0.35f))
            }

            // High shimmer bells
            val bellPitch = listOf(E5, G5, A5, C5).random()
            celestialNotes.add(SynthNote(noteToFreq(bellPitch), beat + 1f, 3.5f, 0.4f))

            beat += 8f
        }

        return listOf(
            SynthTrack("Deep Space Drone", WaveformType.SINE, volume = 0.65f, attackSec = 1.5f, decaySec = 2.0f, sustainLevel = 0.9f, releaseSec = 2.5f, filterCutoffHz = 1800f, notes = droneNotes),
            SynthTrack("Celestial Shimmer", WaveformType.FM_SYNTH, volume = 0.45f, attackSec = 0.5f, decaySec = 1.0f, sustainLevel = 0.7f, releaseSec = 1.8f, filterCutoffHz = 4000f, notes = celestialNotes)
        )
    }

    private fun composeChiptune(totalBeats: Float): List<SynthTrack> {
        val arpeggioNotes = mutableListOf<SynthNote>()
        val leadNotes = mutableListOf<SynthNote>()
        val bassNotes = mutableListOf<SynthNote>()
        val noiseDrums = mutableListOf<SynthNote>()

        val chords = listOf(
            listOf(C4, E4, G4, C5),
            listOf(A3, C4, E4, A4),
            listOf(F3, A3, C4, F4),
            listOf(G3, B3, D4, G4)
        )

        var beat = 0f
        var chordIdx = 0
        while (beat < totalBeats) {
            val chord = chords[chordIdx % chords.size]

            // Fast 16th-note Arpeggiator (classic 8-bit sound)
            for (step in 0 until 16) {
                val pitch = chord[step % chord.size]
                arpeggioNotes.add(SynthNote(noteToFreq(pitch), beat + step * 0.25f, 0.2f, 0.7f))
            }

            // Square Wave Bass
            val bassPitch = chord[0] - 24
            bassNotes.add(SynthNote(noteToFreq(bassPitch), beat, 1.8f, 0.85f))
            bassNotes.add(SynthNote(noteToFreq(bassPitch), beat + 2f, 1.8f, 0.85f))

            // Noise Drum
            noiseDrums.add(SynthNote(120f, beat, 0.1f, 0.8f))
            noiseDrums.add(SynthNote(400f, beat + 1f, 0.1f, 0.7f))
            noiseDrums.add(SynthNote(120f, beat + 2f, 0.1f, 0.8f))
            noiseDrums.add(SynthNote(400f, beat + 3f, 0.1f, 0.7f))

            beat += 4f
            chordIdx++
        }

        // Heroic 8-Bit Lead Melody
        val melody = listOf(C5, E5, G5, A5, G5, E5, D5, C5)
        for (i in melody.indices) {
            leadNotes.add(SynthNote(noteToFreq(melody[i]), i * 2f, 1.5f, 0.8f))
        }

        return listOf(
            SynthTrack("8-Bit Arp", WaveformType.PULSE_PWM, volume = 0.6f, attackSec = 0.005f, decaySec = 0.05f, sustainLevel = 0.4f, releaseSec = 0.05f, filterCutoffHz = 7000f, notes = arpeggioNotes),
            SynthTrack("Square Lead", WaveformType.SQUARE, volume = 0.7f, attackSec = 0.01f, decaySec = 0.1f, sustainLevel = 0.8f, releaseSec = 0.1f, filterCutoffHz = 8000f, notes = leadNotes),
            SynthTrack("Bass Pulse", WaveformType.TRIANGLE, volume = 0.8f, attackSec = 0.01f, decaySec = 0.1f, sustainLevel = 0.7f, releaseSec = 0.1f, filterCutoffHz = 3000f, notes = bassNotes),
            SynthTrack("Noise Hits", WaveformType.WHITE_NOISE, volume = 0.5f, attackSec = 0.001f, decaySec = 0.08f, sustainLevel = 0.1f, releaseSec = 0.05f, filterCutoffHz = 6000f, notes = noiseDrums)
        )
    }

    private fun composeEdm(totalBeats: Float): List<SynthTrack> {
        val supersawNotes = mutableListOf<SynthNote>()
        val bassNotes = mutableListOf<SynthNote>()
        val drumNotes = mutableListOf<SynthNote>()

        // G Minor -> Eb -> Bb -> F
        val chords = listOf(
            listOf(G3, 34 + 24 /*Bb3*/, D4, G4),
            listOf(34 + 24 - 7 /*Eb3*/, G3, 34 + 24 /*Bb3*/, 34 + 24 + 5 /*Eb4*/),
            listOf(34 + 24 /*Bb3*/, D4, F4, 34 + 36 /*Bb4*/),
            listOf(F3, A3, C4, F4)
        )

        var beat = 0f
        var chordIdx = 0
        while (beat < totalBeats) {
            val chord = chords[chordIdx % chords.size]

            // Energetic sidechain chord stabs (on beats 1.5, 2.5, 3.5)
            for (p in chord) {
                supersawNotes.add(SynthNote(noteToFreq(p), beat, 0.7f, 0.8f))
                supersawNotes.add(SynthNote(noteToFreq(p), beat + 1f, 0.7f, 0.8f))
                supersawNotes.add(SynthNote(noteToFreq(p), beat + 2f, 0.7f, 0.8f))
                supersawNotes.add(SynthNote(noteToFreq(p), beat + 3f, 0.7f, 0.8f))
            }

            // Punchy Bassline
            val root = chord[0] - 12
            bassNotes.add(SynthNote(noteToFreq(root), beat, 0.6f, 0.9f))
            bassNotes.add(SynthNote(noteToFreq(root), beat + 1f, 0.6f, 0.9f))
            bassNotes.add(SynthNote(noteToFreq(root), beat + 2f, 0.6f, 0.9f))
            bassNotes.add(SynthNote(noteToFreq(root), beat + 3f, 0.6f, 0.9f))

            // 4-on-the-floor EDM Kick & Clap
            for (k in 0 until 4) {
                drumNotes.add(SynthNote(65f, beat + k, 0.2f, 1.0f)) // Kick
                if (k == 1 || k == 3) {
                    drumNotes.add(SynthNote(250f, beat + k, 0.2f, 0.9f)) // Snare
                }
                drumNotes.add(SynthNote(9000f, beat + k + 0.5f, 0.05f, 0.6f)) // Offbeat Hat
            }

            beat += 4f
            chordIdx++
        }

        return listOf(
            SynthTrack("Supersaw Chords", WaveformType.SAWTOOTH, volume = 0.75f, attackSec = 0.02f, decaySec = 0.2f, sustainLevel = 0.7f, releaseSec = 0.15f, filterCutoffHz = 6500f, notes = supersawNotes),
            SynthTrack("Drop Bass", WaveformType.SAWTOOTH, volume = 0.85f, attackSec = 0.01f, decaySec = 0.1f, sustainLevel = 0.6f, releaseSec = 0.1f, filterCutoffHz = 2400f, notes = bassNotes),
            SynthTrack("EDM Drums", WaveformType.WHITE_NOISE, volume = 0.75f, attackSec = 0.002f, decaySec = 0.15f, sustainLevel = 0.1f, releaseSec = 0.1f, filterCutoffHz = 7500f, notes = drumNotes)
        )
    }

    private fun composeNeoClassical(totalBeats: Float): List<SynthTrack> {
        val pianoNotes = mutableListOf<SynthNote>()

        // E Minor Classical Arpeggiated Motif (Em -> C -> G -> D)
        val progressions = listOf(
            listOf(E3, G3, B3, E4, G4, B4),
            listOf(C3, E3, G3, C4, E4, G4),
            listOf(G2, B2, D3, G3, B3, D4),
            listOf(D3, F3 + 1 /*F#3*/, A3, D4, F3 + 1 + 12, A4)
        )

        var beat = 0f
        var chordIdx = 0
        while (beat < totalBeats) {
            val motif = progressions[chordIdx % progressions.size]
            for (step in 0 until 8) {
                val pitch = motif[step % motif.size]
                pianoNotes.add(SynthNote(noteToFreq(pitch), beat + step * 0.5f, 1.8f, 0.75f))
            }
            beat += 4f
            chordIdx++
        }

        return listOf(
            SynthTrack("Classical Piano", WaveformType.PLUCK_PHYSICAL, volume = 0.85f, attackSec = 0.01f, decaySec = 0.6f, sustainLevel = 0.4f, releaseSec = 0.8f, filterCutoffHz = 4800f, notes = pianoNotes)
        )
    }

    private fun composeMatrixGlitch(totalBeats: Float): List<SynthTrack> {
        val fmBells = mutableListOf<SynthNote>()
        val glitchBass = mutableListOf<SynthNote>()
        val polyrhythm = mutableListOf<SynthNote>()

        val scales = listOf(D3, E3, F3, G3, A3, B3, C4, D4) // Dorian

        var beat = 0f
        while (beat < totalBeats) {
            // FM Complex Modulation
            for (step in 0 until 8) {
                if (Random.nextFloat() > 0.3f) {
                    val pitch = scales.random()
                    fmBells.add(SynthNote(noteToFreq(pitch), beat + step * 0.5f, 0.4f, 0.7f))
                }
            }

            // Heavy sub modulations
            glitchBass.add(SynthNote(noteToFreq(D2), beat, 1.2f, 0.85f))
            glitchBass.add(SynthNote(noteToFreq(D2 + 7), beat + 2f, 0.8f, 0.8f))

            // White noise micro glitches
            for (g in 0 until 16) {
                if (Random.nextFloat() > 0.6f) {
                    polyrhythm.add(SynthNote(5000f + Random.nextFloat() * 4000f, beat + g * 0.25f, 0.04f, 0.5f))
                }
            }

            beat += 4f
        }

        return listOf(
            SynthTrack("FM Bells", WaveformType.FM_SYNTH, volume = 0.7f, attackSec = 0.01f, decaySec = 0.2f, sustainLevel = 0.5f, releaseSec = 0.3f, filterCutoffHz = 6000f, notes = fmBells),
            SynthTrack("Glitch Bass", WaveformType.SAWTOOTH, volume = 0.8f, attackSec = 0.02f, decaySec = 0.2f, sustainLevel = 0.7f, releaseSec = 0.2f, filterCutoffHz = 2000f, notes = glitchBass),
            SynthTrack("Neural Clicks", WaveformType.WHITE_NOISE, volume = 0.55f, attackSec = 0.001f, decaySec = 0.05f, sustainLevel = 0.1f, releaseSec = 0.05f, filterCutoffHz = 8000f, notes = polyrhythm)
        )
    }
}
