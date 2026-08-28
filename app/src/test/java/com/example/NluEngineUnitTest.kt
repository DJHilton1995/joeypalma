package com.example

import com.example.ai.JoeyFallbackEngine
import com.example.ai.JoeyPersona
import com.example.nlu.EntityCategory
import com.example.nlu.NluComplexity
import com.example.nlu.NluEngine
import com.example.nlu.NluIntentId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NluEngineUnitTest {

    @Test
    fun testCodingIntentAndEntityExtraction() {
        val prompt = "How do I refactor this Kotlin Coroutine with Room DB and StateFlow to avoid memory leaks?"
        val result = NluEngine.analyze(prompt)

        assertEquals(NluIntentId.CODING_ARCHITECTURE, result.primaryIntent)
        assertTrue(result.intentConfidence >= 0.70f)

        val entityNames = result.entities.map { it.normalizedValue }
        assertTrue("Entities should contain Kotlin", entityNames.contains("Kotlin"))
        assertTrue("Entities should contain Kotlin Coroutines", entityNames.contains("Kotlin Coroutines"))
        assertTrue("Entities should contain Room Database", entityNames.contains("Room Database"))
        assertTrue("Entities should contain Refactor", entityNames.contains("Refactor"))

        val langEntity = result.entities.find { it.normalizedValue == "Kotlin" }
        assertNotNull(langEntity)
        assertEquals(EntityCategory.PROGRAMMING_LANGUAGE, langEntity?.category)

        assertEquals(JoeyPersona.TECH_ARCHITECT, result.suggestedPersona)
        assertTrue(result.structuredContextDirective.contains("Code & Architecture"))
    }

    @Test
    fun testSecurityCryptographyIntentAndEntities() {
        val prompt = "Verify the RustyMCP X25519 key exchange and ChaCha20-Poly1305 AEAD parameters"
        val result = NluEngine.analyze(prompt)

        assertEquals(NluIntentId.SECURITY_CRYPTOGRAPHY, result.primaryIntent)
        assertTrue(result.intentConfidence >= 0.75f)

        val entityNames = result.entities.map { it.normalizedValue }
        assertTrue(entityNames.any { it.contains("RustyMCP") || it.contains("X25519") || it.contains("ChaCha20") })
    }

    @Test
    fun testTaskProductivityIntent() {
        val prompt = "Create a 5-step sprint plan and checklist roadmap for launching our Android app by next week"
        val result = NluEngine.analyze(prompt)

        assertEquals(NluIntentId.TASK_PRODUCTIVITY, result.primaryIntent)
        assertTrue(result.intentConfidence >= 0.60f)
        assertEquals(JoeyPersona.STUDY_PROD, result.suggestedPersona)
    }

    @Test
    fun testMathLogicIntentAndComplexity() {
        val prompt = "Calculate the derivative of f(x) = 3x^2 + 5x and solve for roots where eigenvalue is zero"
        val result = NluEngine.analyze(prompt)

        assertEquals(NluIntentId.CALCULATION_MATH_LOGIC, result.primaryIntent)
        assertEquals(NluComplexity.ADVANCED_EXPERT, result.complexity)
    }

    @Test
    fun testExplanationFactualIntent() {
        val prompt = "Explain the quantum mechanical mechanism behind photon superposition and wave function collapse"
        val result = NluEngine.analyze(prompt)

        assertEquals(NluIntentId.EXPLANATION_FACTUAL, result.primaryIntent)
        assertEquals(JoeyPersona.DEEP_THINKER, result.suggestedPersona)
    }

    @Test
    fun testCasualGreetingIntent() {
        val prompt = "Hey Joey! How you doin'?"
        val result = NluEngine.analyze(prompt)

        assertEquals(NluIntentId.GREETING_CASUAL, result.primaryIntent)
        assertTrue(result.intentConfidence >= 0.85f)
    }

    @Test
    fun testJoeyFallbackEngineWithNlu() {
        val prompt = "Refactor this Kotlin Coroutine and Jetpack Compose state code"
        val response = JoeyFallbackEngine.generateLocalResponse(prompt, JoeyPersona.TECH_ARCHITECT)

        assertTrue(response.contains("NLU Intent Detected") || response.contains("Kotlin"))
        assertTrue(response.contains("Architecture") || response.contains("Coroutines"))
    }
}
