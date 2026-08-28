package com.example.nlu

import com.example.ai.JoeyPersona
import com.squareup.moshi.JsonClass

/**
 * Natural Language Understanding (NLU) Core Models for Joey AI.
 * Represents parsed semantic structures, identified intents, and extracted entities.
 */

@JsonClass(generateAdapter = false)
enum class NluIntentId(
    val displayName: String,
    val emoji: String,
    val description: String,
    val defaultPersona: JoeyPersona
) {
    CODING_ARCHITECTURE(
        displayName = "Code & Architecture",
        emoji = "⚙️",
        description = "Implementation, debugging, refactoring, algorithms, systems engineering",
        defaultPersona = JoeyPersona.TECH_ARCHITECT
    ),
    EXPLANATION_FACTUAL(
        displayName = "Explanation & Science",
        emoji = "🔬",
        description = "Deep conceptual inquiries, science, physics, mechanics, definitions",
        defaultPersona = JoeyPersona.DEEP_THINKER
    ),
    TASK_PRODUCTIVITY(
        displayName = "Planning & Productivity",
        emoji = "📋",
        description = "Task lists, step-by-step guides, project roadmaps, scheduling, summaries",
        defaultPersona = JoeyPersona.STUDY_PROD
    ),
    CREATIVE_GENERATION(
        displayName = "Creative & Brainstorming",
        emoji = "✨",
        description = "Idea generation, creative writing, storytelling, metaphors, humor",
        defaultPersona = JoeyPersona.CREATIVE_SPARK
    ),
    CALCULATION_MATH_LOGIC(
        displayName = "Math & Logic Deduction",
        emoji = "🧮",
        description = "Formulas, logic puzzles, calculations, proofs, mathematical deductions",
        defaultPersona = JoeyPersona.DEEP_THINKER
    ),
    SECURITY_CRYPTOGRAPHY(
        displayName = "Security & Cryptography",
        emoji = "🛡️",
        description = "Vault integrity, RustyMCP verification, zero-trust protocols, encryption",
        defaultPersona = JoeyPersona.TECH_ARCHITECT
    ),
    GREETING_CASUAL(
        displayName = "Casual & Greeting",
        emoji = "👋",
        description = "Greetings, pleasantries, banter, casual conversational check-in",
        defaultPersona = JoeyPersona.CLASSIC
    ),
    SYSTEM_COMMAND(
        displayName = "System Command",
        emoji = "⚡",
        description = "Session clearing, audio speech control, export transcript, settings",
        defaultPersona = JoeyPersona.CLASSIC
    ),
    MUSIC_AUDIO_SYNTH(
        displayName = "Music & Neural Audio Synth",
        emoji = "🎵",
        description = "Composition, multi-track synthesis, beats, chord progressions, open-source audio models",
        defaultPersona = JoeyPersona.CREATIVE_SPARK
    ),
    UNCERTAIN_GENERAL(
        displayName = "General Inquiry",
        emoji = "💬",
        description = "Open-ended inquiry and multi-domain conversation",
        defaultPersona = JoeyPersona.CLASSIC
    );

    companion object {
        fun fromName(name: String): NluIntentId {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: UNCERTAIN_GENERAL
        }
    }
}

@JsonClass(generateAdapter = false)
enum class EntityCategory(
    val label: String,
    val emoji: String,
    val colorHex: Long
) {
    PROGRAMMING_LANGUAGE("Language", "💻", 0xFF60A5FA),
    FRAMEWORK_LIBRARY("Framework", "📦", 0xFF34D399),
    ACTION_VERB("Action", "🎯", 0xFFF59E0B),
    TECHNICAL_CONCEPT("Concept", "🧩", 0xFFA78BFA),
    TEMPORAL_TIME("Temporal", "⏳", 0xFFF472B6),
    NUMERICAL_METRIC("Metric", "🔢", 0xFF38BDF8),
    DOMAIN_TOPIC("Domain", "🌐", 0xFF818CF8),
    MUSIC_AUDIO("Music & Synth", "🎹", 0xFF06B6D4),
    SYSTEM_TARGET("Target", "⚙️", 0xFF94A3B8)
}

@JsonClass(generateAdapter = false)
data class ExtractedEntity(
    val text: String,
    val category: EntityCategory,
    val normalizedValue: String,
    val confidence: Float = 0.95f,
    val startIndex: Int = -1,
    val endIndex: Int = -1
)

@JsonClass(generateAdapter = false)
data class NluSentiment(
    val label: String,       // e.g. "Analytical", "Urgent", "Curious", "Frustrated", "Enthusiastic", "Neutral", "Playful"
    val score: Float,        // -1.0 (very negative) to +1.0 (very positive)
    val emoji: String
)

@JsonClass(generateAdapter = false)
enum class NluComplexity(
    val label: String,
    val badge: String,
    val emoji: String
) {
    BEGINNER("Foundational", "Level 1", "🟢"),
    INTERMEDIATE("Intermediate", "Level 2", "🟡"),
    ADVANCED_EXPERT("Advanced / Deep Tech", "Level 3", "🟣")
}

@JsonClass(generateAdapter = false)
data class NluAnalysisResult(
    val rawText: String,
    val primaryIntent: NluIntentId,
    val intentConfidence: Float,
    val secondaryIntents: List<Pair<NluIntentId, Float>> = emptyList(),
    val entities: List<ExtractedEntity> = emptyList(),
    val sentiment: NluSentiment = NluSentiment("Neutral", 0.0f, "😐"),
    val complexity: NluComplexity = NluComplexity.INTERMEDIATE,
    val keywords: List<String> = emptyList(),
    val suggestedPersona: JoeyPersona = primaryIntent.defaultPersona,
    val structuredContextDirective: String = "",
    val processingTimeMs: Long = 0L
) {
    /**
     * Compact summary for UI tags & badges
     */
    val shortSummary: String
        get() = "${primaryIntent.emoji} ${primaryIntent.displayName} (${(intentConfidence * 100).toInt()}%)"

    val entitiesSummary: String
        get() = entities.take(4).joinToString(", ") { "${it.category.emoji} ${it.normalizedValue}" }
}
