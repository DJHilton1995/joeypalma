package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.nlu.NluAnalysisResult
import com.example.nlu.NluEngine
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): GeminiGenerateResponse
}

class GeminiChatService {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private val apiService: GeminiApiService = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GeminiApiService::class.java)

    /**
     * Generates a response from Joey AI given the conversation history, user prompt, persona,
     * and NLU analysis context.
     */
    suspend fun sendMessage(
        prompt: String,
        history: List<Pair<String, String>>, // sender ("user" or "joey") to content
        persona: JoeyPersona,
        customApiKey: String? = null,
        nluAnalysis: NluAnalysisResult? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customApiKey)
        val nlu = nluAnalysis ?: NluEngine.analyze(prompt)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiChatService", "No valid API key present, engaging JoeyFallbackEngine with NLU")
            val fallback = JoeyFallbackEngine.generateLocalResponse(prompt, persona)
            return@withContext Result.success(fallback)
        }

        try {
            // Build conversation turns
            val contents = mutableListOf<GeminiContent>()

            // Include past few turns for context (limit to last 10 messages for speed)
            val recentHistory = history.takeLast(10)
            for ((sender, text) in recentHistory) {
                val role = if (sender.equals("user", ignoreCase = true)) "user" else "model"
                contents.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = text))
                    )
                )
            }

            // Append current prompt
            contents.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            )

            // Synthesize NLU Context Directive into the System Prompt
            val enrichedSystemPrompt = buildString {
                append(persona.systemPrompt)
                append("\n\n")
                append("### NATURAL LANGUAGE UNDERSTANDING (NLU) CONTEXT:\n")
                append(nlu.structuredContextDirective)
                append("\n")
                append("Enforce maximum logical precision, address all identified entities (${nlu.entities.joinToString { it.normalizedValue }}), and adhere strictly to the Primary Intent (${nlu.primaryIntent.displayName}).")
            }

            val request = GeminiGenerateRequest(
                contents = contents,
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = enrichedSystemPrompt))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = when (persona) {
                        JoeyPersona.CREATIVE_SPARK -> 0.85f
                        JoeyPersona.TECH_ARCHITECT -> 0.35f
                        JoeyPersona.DEEP_THINKER -> 0.40f
                        JoeyPersona.STUDY_PROD -> 0.50f
                        else -> 0.70f
                    },
                    maxOutputTokens = 2048
                )
            )

            val response = apiService.generateContent(apiKey = apiKey, request = request)

            if (response.error != null) {
                val errMessage = response.error.message ?: "Unknown API error"
                Log.w("GeminiChatService", "API returned error: $errMessage, using fallback")
                val fallback = JoeyFallbackEngine.generateLocalResponse(prompt, persona)
                return@withContext Result.success(fallback)
            }

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                val fallback = JoeyFallbackEngine.generateLocalResponse(prompt, persona)
                Result.success(fallback)
            }
        } catch (e: Exception) {
            Log.e("GeminiChatService", "Failed to call Gemini API: ${e.message}", e)
            val fallback = JoeyFallbackEngine.generateLocalResponse(prompt, persona)
            Result.success(fallback)
        }
    }

    private fun resolveApiKey(customKey: String?): String {
        if (!customKey.isNullOrBlank()) return customKey.trim()
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }
}
