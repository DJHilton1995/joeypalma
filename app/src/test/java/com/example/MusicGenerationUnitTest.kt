package com.example

import com.example.music.AudioSynthEngine
import com.example.music.ModelTrustTier
import com.example.music.MusicGenre
import com.example.music.MusicIntelligenceEngine
import com.example.music.MusicModelRegistry
import com.example.music.SynthNote
import com.example.music.SynthTrack
import com.example.music.WaveformType
import com.example.nlu.NluEngine
import com.example.nlu.NluIntentId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File

class MusicGenerationUnitTest {

    @Test
    fun testModelRegistryIntegrityAndTrustScores() {
        val models = MusicModelRegistry.VERIFIED_MODELS
        assertTrue("Model registry should contain multiple verified models", models.size >= 8)

        for (model in models) {
            assertTrue("Model name should not be blank", model.name.isNotBlank())
            assertTrue("Organization should not be blank", model.organization.isNotBlank())
            assertTrue("Security score must be >= 90", model.securityScore >= 90)
            assertTrue("Academic citation must be present", model.academicPaper.isNotBlank())
            assertTrue("Sample rate should be valid", model.audioSampleRate.isNotBlank())
            assertTrue("License must be open source", model.license.isNotBlank())
            assertNotNull(model.trustTier)
        }

        // Test genre lookup
        for (genre in MusicGenre.values()) {
            val matchedModels = MusicModelRegistry.findBestModelsForGenre(genre)
            assertTrue("Should find models for genre ${genre.name}", matchedModels.isNotEmpty())
        }
    }

    @Test
    fun testDspSynthEngineAudioGeneration() {
        val notes = listOf(
            SynthNote(pitchHz = 440f, startBeat = 0f, durationBeats = 1f, velocity = 0.8f),
            SynthNote(pitchHz = 880f, startBeat = 1f, durationBeats = 1f, velocity = 0.8f)
        )
        val track = SynthTrack(
            name = "Test Lead",
            waveform = WaveformType.SAWTOOTH,
            volume = 0.8f,
            notes = notes
        )

        val pcm = AudioSynthEngine.synthesizeTracks(
            tracks = listOf(track),
            totalDurationSeconds = 2.0f,
            bpm = 120,
            sampleRate = 44100
        )

        assertNotNull(pcm)
        assertTrue("PCM data should contain samples", pcm.isNotEmpty())
        assertEquals("Sample count should match 2 seconds at 44.1kHz", 44100 * 2, pcm.size)

        // Verify non-silent samples exist
        val hasNonZeroSamples = pcm.any { it != 0.toShort() }
        assertTrue("PCM stream should contain active audible audio", hasNonZeroSamples)
    }

    @Test
    fun testWavEncodingHeaderIntegrity() {
        val testPcm = ShortArray(44100) { (kotlin.math.sin(it.toDouble() * 0.1) * 16000).toInt().toShort() }
        val wavBytes = AudioSynthEngine.createWavByteArray(testPcm, 44100)

        assertTrue("WAV bytes should be larger than 44 bytes header", wavBytes.size > 44)

        // Check RIFF header magic bytes
        val riff = String(wavBytes.copyOfRange(0, 4))
        assertEquals("RIFF", riff)

        val wave = String(wavBytes.copyOfRange(8, 12))
        assertEquals("WAVE", wave)

        val fmt = String(wavBytes.copyOfRange(12, 16))
        assertEquals("fmt ", fmt)

        val data = String(wavBytes.copyOfRange(36, 40))
        assertEquals("data", data)
    }

    @Test
    fun testMusicIntelligenceEngineCompositionFromPrompts() {
        val genresToTest = listOf(
            "Cyberpunk synthwave retro 80s" to MusicGenre.SYNTHWAVE,
            "Lofi study beats chill coffee" to MusicGenre.LO_FI,
            "Deep space ambient meditation" to MusicGenre.AMBIENT,
            "8-bit arcade chiptune video game" to MusicGenre.CHIPTUNE,
            "Future bass edm club drop" to MusicGenre.EDM_FUTURE_BASS,
            "Classical piano nocturne" to MusicGenre.NEO_CLASSICAL
        )

        for ((prompt, expectedGenre) in genresToTest) {
            val track = MusicIntelligenceEngine.composeFromPrompt(prompt)
            assertNotNull(track)
            assertEquals("Genre mapping failed for prompt: $prompt", expectedGenre, track.genre)
            assertTrue("Track title should not be blank", track.title.isNotBlank())
            assertTrue("Track duration should be > 0", track.durationSeconds > 0f)
            assertTrue("Audio PCM data should be generated", track.audioDataPcm16.isNotEmpty())
            assertTrue("Model provenance must be present", track.aiModelProvenance.isNotBlank())
        }
    }

    @Test
    fun testNluMusicIntentClassification() {
        val musicPrompts = listOf(
            "Compose a Cyberpunk Synthwave track with punchy bass",
            "Generate a chill lofi study beat in F major",
            "Play me some 8-bit chiptune arcade music",
            "Create an ambient drone soundscape for deep focus"
        )

        for (prompt in musicPrompts) {
            val result = NluEngine.analyze(prompt)
            assertEquals(
                "Prompt should be classified as MUSIC_AUDIO_SYNTH: '$prompt'",
                NluIntentId.MUSIC_AUDIO_SYNTH,
                result.primaryIntent
            )
            assertTrue("Intent confidence should be >= 0.5", result.intentConfidence >= 0.5f)
        }
    }
}
