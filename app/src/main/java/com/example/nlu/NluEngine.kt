package com.example.nlu

import com.example.ai.JoeyPersona
import java.util.Locale
import java.util.regex.Pattern

/**
 * Natural Language Understanding (NLU) Engine for Joey AI.
 * 
 * Provides:
 * 1. Lexical and Semantic Tokenization
 * 2. Intent Identification & Multi-class Confidence Scoring
 * 3. Named Entity Recognition (NER) across Tech, Frameworks, Actions, Metrics, Concepts
 * 4. Sentiment & Complexity Analysis
 * 5. Structured Context Augmentation for Joey's Reasoning Chain
 */
object NluEngine {

    // --- Entity Dictionaries ---

    private val PROGRAMMING_LANGUAGES = mapOf(
        "kotlin" to "Kotlin",
        "rust" to "Rust",
        "python" to "Python",
        "typescript" to "TypeScript",
        "javascript" to "JavaScript",
        "java" to "Java",
        "c++" to "C++",
        "cpp" to "C++",
        "golang" to "Go",
        "go" to "Go",
        "swift" to "Swift",
        "c#" to "C#",
        "csharp" to "C#",
        "sql" to "SQL",
        "bash" to "Bash",
        "shell" to "Shell",
        "ruby" to "Ruby",
        "php" to "PHP",
        "dart" to "Dart",
        "html" to "HTML",
        "css" to "CSS",
        "assembly" to "Assembly",
        "scala" to "Scala",
        "elixir" to "Elixir",
        "zig" to "Zig",
        "lua" to "Lua"
    )

    private val FRAMEWORKS_AND_LIBRARIES = mapOf(
        "jetpack compose" to "Jetpack Compose",
        "compose" to "Jetpack Compose",
        "room" to "Room Database",
        "room db" to "Room Database",
        "sqlite" to "SQLite",
        "retrofit" to "Retrofit",
        "coroutines" to "Kotlin Coroutines",
        "coroutine" to "Kotlin Coroutines",
        "flow" to "Kotlin Flow",
        "tokio" to "Tokio Async (Rust)",
        "actix" to "Actix Web",
        "diesel" to "Diesel ORM",
        "serde" to "Serde",
        "moshi" to "Moshi",
        "ksp" to "KSP",
        "gradle" to "Gradle",
        "okhttp" to "OkHttp",
        "react" to "React",
        "react native" to "React Native",
        "flutter" to "Flutter",
        "vue" to "Vue.js",
        "angular" to "Angular",
        "nextjs" to "Next.js",
        "next.js" to "Next.js",
        "tailwind" to "Tailwind CSS",
        "spring" to "Spring Boot",
        "django" to "Django",
        "fastapi" to "FastAPI",
        "pytorch" to "PyTorch",
        "tensorflow" to "TensorFlow",
        "docker" to "Docker",
        "kubernetes" to "Kubernetes",
        "k8s" to "Kubernetes"
    )

    private val ACTION_VERBS = mapOf(
        "debug" to "Debug / Fix",
        "fix" to "Debug / Fix",
        "refactor" to "Refactor",
        "optimize" to "Optimize",
        "implement" to "Implement",
        "write" to "Write / Generate",
        "create" to "Create",
        "build" to "Build / Architect",
        "explain" to "Explain",
        "analyze" to "Analyze",
        "summarize" to "Summarize",
        "calculate" to "Calculate",
        "solve" to "Solve",
        "translate" to "Translate",
        "design" to "Design",
        "test" to "Test / Verify",
        "verify" to "Verify",
        "audit" to "Security Audit",
        "encrypt" to "Encrypt",
        "decrypt" to "Decrypt",
        "seal" to "Seal Protocol",
        "clear" to "Clear / Reset",
        "export" to "Export",
        "speak" to "Speech Synthesis",
        "brainstorm" to "Brainstorm",
        "pitch" to "Pitch / Market"
    )

    private val TECHNICAL_CONCEPTS = mapOf(
        "memory safety" to "Memory Safety",
        "borrow checker" to "Borrow Checker",
        "lifetimes" to "Lifetimes",
        "zero-cost abstractions" to "Zero-Cost Abstractions",
        "concurrency" to "Concurrency",
        "multithreading" to "Multithreading",
        "deadlock" to "Deadlock Prevention",
        "race condition" to "Race Condition",
        "async/await" to "Async/Await",
        "garbage collection" to "Garbage Collection",
        "monad" to "Monad Pattern",
        "dependency injection" to "Dependency Injection",
        "clean architecture" to "Clean Architecture",
        "mvvm" to "MVVM Architecture",
        "anti-replay" to "Anti-Replay Monotonic Sequence",
        "diffie-hellman" to "Diffie-Hellman Key Exchange",
        "x25519" to "X25519 Key Agreement",
        "ed25519" to "Ed25519 Digital Signatures",
        "chacha20" to "ChaCha20-Poly1305 AEAD",
        "aes-gcm" to "AES-GCM Encryption",
        "aead" to "Authenticated Encryption (AEAD)",
        "hkdf" to "HKDF-SHA256 Key Derivation",
        "rustymcp" to "RustyMCP Protocol",
        "zero trust" to "Zero-Trust Architecture",
        "quantum computing" to "Quantum Computing",
        "superposition" to "Superposition",
        "neural network" to "Neural Network",
        "transformer" to "Transformer Architecture",
        "attention mechanism" to "Attention Mechanism",
        "embeddings" to "Vector Embeddings",
        "first principles" to "First-Principles Reasoning"
    )

    private val DOMAIN_TOPICS = mapOf(
        "cryptography" to "Cryptography & Security",
        "security" to "Cybersecurity",
        "artificial intelligence" to "Artificial Intelligence",
        "ai" to "Artificial Intelligence",
        "machine learning" to "Machine Learning",
        "mobile development" to "Android & Mobile",
        "android" to "Android Systems",
        "web development" to "Web Engineering",
        "backend" to "Backend Infrastructure",
        "frontend" to "Frontend UI/UX",
        "cloud" to "Cloud Infrastructure",
        "physics" to "Physics & Mechanics",
        "mathematics" to "Mathematics",
        "math" to "Mathematics",
        "neuroscience" to "Neuroscience",
        "biology" to "Biology",
        "astronomy" to "Space & Astronomy",
        "space" to "Space & Astronomy",
        "finance" to "Finance & Economics",
        "productivity" to "Productivity & Time Management"
    )

    // Regex patterns for temporal, numerical metrics, math equations
    private val TEMPORAL_PATTERN = Pattern.compile(
        "\\b(today|tomorrow|yesterday|tonight|next week|this weekend|deadline|schedule|milestone|202[0-9]|daily|hourly|monthly|sprint)\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val NUMERIC_METRIC_PATTERN = Pattern.compile(
        "\\b(\\d+(?:\\.\\d+)?\\s*(?:ms|seconds|sec|minutes|min|hours|kb|mb|gb|tb|bits|bytes|%|percent|fps|hz|usd|dollars|users|ops/sec))\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val MATH_EXPR_PATTERN = Pattern.compile(
        "(\\b\\d+\\s*[+\\-*/^%=]\\s*\\d+|\\bsin|cos|tan|log|sqrt|derivative|integral|matrix|eigenvalue|fibonacci|factorial\\b)",
        Pattern.CASE_INSENSITIVE
    )

    // Intent Keyword Weights
    private val CODING_KEYWORDS = setOf(
        "code", "function", "class", "algorithm", "compiler", "syntax", "compile",
        "kotlin", "rust", "python", "java", "typescript", "javascript", "c++", "golang",
        "bug", "error", "stacktrace", "nullpointer", "exception", "refactor", "optimize",
        "coroutine", "thread", "async", "struct", "enum", "trait", "interface", "api",
        "rest", "endpoint", "database", "query", "sql", "git", "gradle", "package", "sdk"
    )

    private val EXPLANATION_KEYWORDS = setOf(
        "explain", "why", "how does", "what is", "define", "concept", "theory", "mechanism",
        "science", "physics", "universe", "quantum", "biological", "evolution", "history",
        "origin", "mechanism", "principle", "understand", "difference between", "compare"
    )

    private val TASK_PRODUCTIVITY_KEYWORDS = setOf(
        "plan", "todo", "checklist", "steps", "roadmap", "schedule", "summarize", "summary",
        "action items", "organize", "bullet points", "goals", "deadline", "productivity",
        "breakdown", "strategy", "sprint", "meeting notes", "key takeaways"
    )

    private val CREATIVE_KEYWORDS = setOf(
        "write a story", "poem", "creative", "brainstorm", "metaphor", "joke", "funny",
        "imagine", "character", "dialogue", "plot", "pitch", "slogan", "tagline", "fiction",
        "vibe", "rhyme", "song", "lyrics", "humor"
    )

    private val MATH_LOGIC_KEYWORDS = setOf(
        "calculate", "solve", "math", "formula", "equation", "proof", "deduce", "probability",
        "statistics", "integral", "derivative", "matrix", "algebra", "geometry", "puzzle",
        "riddle", "logic", "boolean", "theorem"
    )

    private val SECURITY_KEYWORDS = setOf(
        "security", "encrypt", "decrypt", "cipher", "vault", "rustymcp", "replay attack",
        "signature", "ed25519", "x25519", "diffie-hellman", "chacha20", "aes", "hash",
        "sha256", "hkdf", "key exchange", "aead", "zero trust", "audit", "vulnerability"
    )

    private val GREETING_KEYWORDS = setOf(
        "hello", "hi", "hey", "howdy", "sup", "how are you", "how you doin", "good morning",
        "good afternoon", "good evening", "what's up", "yo"
    )

    private val SYSTEM_COMMAND_KEYWORDS = setOf(
        "clear history", "clear chat", "delete messages", "export", "share transcript",
        "stop speaking", "mute audio", "read aloud", "speak this", "change persona",
        "security settings", "api key"
    )

    private val MUSIC_AUDIO_KEYWORDS = setOf(
        "music", "compose", "composition", "beat", "synth", "synthwave", "lofi", "lo-fi",
        "ambient", "chiptune", "8-bit", "chord", "melody", "edm", "audio", "track", "dsp",
        "wav", "instrument", "bpm", "tempo", "soundscape", "piano", "classical", "glitch",
        "musicgen", "song", "tune", "synthesizer", "arpeggio", "bassline"
    )

    /**
     * Main NLU Processing Method.
     * Parses raw user text into structured intents, extracted entities, sentiment, and context directives.
     */
    fun analyze(text: String): NluAnalysisResult {
        val startTime = System.currentTimeMillis()
        val normalizedText = text.trim()
        val lower = normalizedText.lowercase(Locale.ROOT)

        // 1. Extract Entities
        val extractedEntities = extractAllEntities(normalizedText, lower)

        // 2. Score Intents
        val intentScores = computeIntentScores(lower, extractedEntities)
        val sortedIntents = intentScores.toList().sortedByDescending { it.second }
        val primaryIntentEntry = sortedIntents.firstOrNull() ?: (NluIntentId.UNCERTAIN_GENERAL to 0.5f)
        val primaryIntent = primaryIntentEntry.first
        val primaryConfidence = primaryIntentEntry.second.coerceIn(0.40f, 0.99f)

        val secondaryIntents = sortedIntents.drop(1).take(2).map { it.first to it.second.coerceIn(0.1f, 0.9f) }

        // 3. Extract Keywords
        val keywords = extractSignificantKeywords(lower)

        // 4. Analyze Sentiment & Tone
        val sentiment = computeSentiment(lower)

        // 5. Detect Complexity Level
        val complexity = computeComplexity(lower, extractedEntities)

        // 6. Select Persona Recommendation
        val suggestedPersona = when (primaryIntent) {
            NluIntentId.CODING_ARCHITECTURE -> JoeyPersona.TECH_ARCHITECT
            NluIntentId.EXPLANATION_FACTUAL -> JoeyPersona.DEEP_THINKER
            NluIntentId.TASK_PRODUCTIVITY -> JoeyPersona.STUDY_PROD
            NluIntentId.CREATIVE_GENERATION -> JoeyPersona.CREATIVE_SPARK
            NluIntentId.CALCULATION_MATH_LOGIC -> JoeyPersona.DEEP_THINKER
            NluIntentId.SECURITY_CRYPTOGRAPHY -> JoeyPersona.TECH_ARCHITECT
            NluIntentId.GREETING_CASUAL -> JoeyPersona.CLASSIC
            NluIntentId.SYSTEM_COMMAND -> JoeyPersona.CLASSIC
            NluIntentId.MUSIC_AUDIO_SYNTH -> JoeyPersona.CREATIVE_SPARK
            NluIntentId.UNCERTAIN_GENERAL -> JoeyPersona.CLASSIC
        }

        // 7. Synthesize Structured Context Directive
        val directive = buildContextDirective(
            primaryIntent = primaryIntent,
            confidence = primaryConfidence,
            entities = extractedEntities,
            sentiment = sentiment,
            complexity = complexity
        )

        val duration = System.currentTimeMillis() - startTime

        return NluAnalysisResult(
            rawText = normalizedText,
            primaryIntent = primaryIntent,
            intentConfidence = primaryConfidence,
            secondaryIntents = secondaryIntents,
            entities = extractedEntities,
            sentiment = sentiment,
            complexity = complexity,
            keywords = keywords,
            suggestedPersona = suggestedPersona,
            structuredContextDirective = directive,
            processingTimeMs = duration
        )
    }

    /**
     * Entity Extraction Pipeline
     */
    private fun extractAllEntities(originalText: String, lowerText: String): List<ExtractedEntity> {
        val result = mutableListOf<ExtractedEntity>()
        val seenRanges = mutableListOf<IntRange>()

        fun tryAddEntity(
            text: String,
            category: EntityCategory,
            normalized: String,
            conf: Float,
            start: Int,
            end: Int
        ) {
            val range = start until end
            if (seenRanges.none { it.first <= range.first && it.last >= range.last }) {
                seenRanges.add(range)
                result.add(
                    ExtractedEntity(
                        text = text,
                        category = category,
                        normalizedValue = normalized,
                        confidence = conf,
                        startIndex = start,
                        endIndex = end
                    )
                )
            }
        }

        // 1. Languages
        for ((key, normalized) in PROGRAMMING_LANGUAGES) {
            val idx = findWordBoundaryIndex(lowerText, key)
            if (idx >= 0) {
                val matched = originalText.substring(idx, idx + key.length)
                tryAddEntity(matched, EntityCategory.PROGRAMMING_LANGUAGE, normalized, 0.98f, idx, idx + key.length)
            }
        }

        // 2. Frameworks & Libraries
        for ((key, normalized) in FRAMEWORKS_AND_LIBRARIES) {
            val idx = findWordBoundaryIndex(lowerText, key)
            if (idx >= 0) {
                val matched = originalText.substring(idx, idx + key.length)
                tryAddEntity(matched, EntityCategory.FRAMEWORK_LIBRARY, normalized, 0.95f, idx, idx + key.length)
            }
        }

        // 3. Technical Concepts & Cryptography
        for ((key, normalized) in TECHNICAL_CONCEPTS) {
            val idx = findWordBoundaryIndex(lowerText, key)
            if (idx >= 0) {
                val matched = originalText.substring(idx, idx + key.length)
                tryAddEntity(matched, EntityCategory.TECHNICAL_CONCEPT, normalized, 0.92f, idx, idx + key.length)
            }
        }

        // 4. Domains & Topics
        for ((key, normalized) in DOMAIN_TOPICS) {
            val idx = findWordBoundaryIndex(lowerText, key)
            if (idx >= 0) {
                val matched = originalText.substring(idx, idx + key.length)
                tryAddEntity(matched, EntityCategory.DOMAIN_TOPIC, normalized, 0.90f, idx, idx + key.length)
            }
        }

        // 5. Action Verbs
        for ((key, normalized) in ACTION_VERBS) {
            val idx = findWordBoundaryIndex(lowerText, key)
            if (idx >= 0) {
                val matched = originalText.substring(idx, idx + key.length)
                tryAddEntity(matched, EntityCategory.ACTION_VERB, normalized, 0.88f, idx, idx + key.length)
            }
        }

        // 6. Temporal Time Expressions
        val tempMatcher = TEMPORAL_PATTERN.matcher(originalText)
        while (tempMatcher.find()) {
            val match = tempMatcher.group()
            val capMatch = match.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            tryAddEntity(match, EntityCategory.TEMPORAL_TIME, capMatch, 0.85f, tempMatcher.start(), tempMatcher.end())
        }

        // 7. Numerical Metrics
        val metricMatcher = NUMERIC_METRIC_PATTERN.matcher(originalText)
        while (metricMatcher.find()) {
            val match = metricMatcher.group()
            tryAddEntity(match, EntityCategory.NUMERICAL_METRIC, match, 0.90f, metricMatcher.start(), metricMatcher.end())
        }

        return result
    }

    private fun findWordBoundaryIndex(text: String, word: String): Int {
        val pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.start() else -1
    }

    /**
     * Intent Classification with Weighted Feature Vectors
     */
    private fun computeIntentScores(
        lowerText: String,
        entities: List<ExtractedEntity>
    ): Map<NluIntentId, Float> {
        val scores = mutableMapOf<NluIntentId, Float>()
        for (intent in NluIntentId.entries) {
            scores[intent] = 0.05f
        }

        val words = lowerText.split(Regex("[^a-zA-Z0-9_+#.-]+")).filter { it.isNotBlank() }

        // Keyword Density Matching
        var codingMatches = 0
        var explanationMatches = 0
        var taskMatches = 0
        var creativeMatches = 0
        var mathMatches = 0
        var securityMatches = 0
        var greetingMatches = 0
        var sysMatches = 0
        var musicMatches = 0

        for (w in words) {
            if (CODING_KEYWORDS.contains(w)) codingMatches++
            if (EXPLANATION_KEYWORDS.contains(w)) explanationMatches++
            if (TASK_PRODUCTIVITY_KEYWORDS.contains(w)) taskMatches++
            if (CREATIVE_KEYWORDS.contains(w)) creativeMatches++
            if (MATH_LOGIC_KEYWORDS.contains(w)) mathMatches++
            if (SECURITY_KEYWORDS.contains(w)) securityMatches++
            if (GREETING_KEYWORDS.contains(w)) greetingMatches++
            if (SYSTEM_COMMAND_KEYWORDS.contains(w)) sysMatches++
            if (MUSIC_AUDIO_KEYWORDS.contains(w)) musicMatches++
        }

        scores[NluIntentId.CODING_ARCHITECTURE] = (scores[NluIntentId.CODING_ARCHITECTURE] ?: 0f) + codingMatches * 0.20f
        scores[NluIntentId.EXPLANATION_FACTUAL] = (scores[NluIntentId.EXPLANATION_FACTUAL] ?: 0f) + explanationMatches * 0.22f
        scores[NluIntentId.TASK_PRODUCTIVITY] = (scores[NluIntentId.TASK_PRODUCTIVITY] ?: 0f) + taskMatches * 0.22f
        scores[NluIntentId.CREATIVE_GENERATION] = (scores[NluIntentId.CREATIVE_GENERATION] ?: 0f) + creativeMatches * 0.25f
        scores[NluIntentId.CALCULATION_MATH_LOGIC] = (scores[NluIntentId.CALCULATION_MATH_LOGIC] ?: 0f) + mathMatches * 0.25f
        scores[NluIntentId.SECURITY_CRYPTOGRAPHY] = (scores[NluIntentId.SECURITY_CRYPTOGRAPHY] ?: 0f) + securityMatches * 0.30f
        scores[NluIntentId.GREETING_CASUAL] = (scores[NluIntentId.GREETING_CASUAL] ?: 0f) + greetingMatches * 0.40f
        scores[NluIntentId.SYSTEM_COMMAND] = (scores[NluIntentId.SYSTEM_COMMAND] ?: 0f) + sysMatches * 0.40f
        scores[NluIntentId.MUSIC_AUDIO_SYNTH] = (scores[NluIntentId.MUSIC_AUDIO_SYNTH] ?: 0f) + musicMatches * 0.35f

        // Entity-driven Boosting
        for (e in entities) {
            when (e.category) {
                EntityCategory.PROGRAMMING_LANGUAGE, EntityCategory.FRAMEWORK_LIBRARY -> {
                    scores[NluIntentId.CODING_ARCHITECTURE] = (scores[NluIntentId.CODING_ARCHITECTURE] ?: 0f) + 0.35f
                }
                EntityCategory.TECHNICAL_CONCEPT -> {
                    if (e.normalizedValue.contains("Encryption") || e.normalizedValue.contains("Diffie") || e.normalizedValue.contains("MCP") || e.normalizedValue.contains("Security")) {
                        scores[NluIntentId.SECURITY_CRYPTOGRAPHY] = (scores[NluIntentId.SECURITY_CRYPTOGRAPHY] ?: 0f) + 0.40f
                    } else {
                        scores[NluIntentId.EXPLANATION_FACTUAL] = (scores[NluIntentId.EXPLANATION_FACTUAL] ?: 0f) + 0.25f
                        scores[NluIntentId.CODING_ARCHITECTURE] = (scores[NluIntentId.CODING_ARCHITECTURE] ?: 0f) + 0.20f
                    }
                }
                EntityCategory.ACTION_VERB -> {
                    when (e.normalizedValue) {
                        "Debug / Fix", "Refactor", "Optimize", "Implement" -> {
                            scores[NluIntentId.CODING_ARCHITECTURE] = (scores[NluIntentId.CODING_ARCHITECTURE] ?: 0f) + 0.30f
                        }
                        "Explain", "Analyze" -> {
                            scores[NluIntentId.EXPLANATION_FACTUAL] = (scores[NluIntentId.EXPLANATION_FACTUAL] ?: 0f) + 0.25f
                        }
                        "Summarize", "Plan" -> {
                            scores[NluIntentId.TASK_PRODUCTIVITY] = (scores[NluIntentId.TASK_PRODUCTIVITY] ?: 0f) + 0.30f
                        }
                        "Calculate", "Solve" -> {
                            scores[NluIntentId.CALCULATION_MATH_LOGIC] = (scores[NluIntentId.CALCULATION_MATH_LOGIC] ?: 0f) + 0.35f
                        }
                        "Brainstorm", "Pitch" -> {
                            scores[NluIntentId.CREATIVE_GENERATION] = (scores[NluIntentId.CREATIVE_GENERATION] ?: 0f) + 0.35f
                        }
                        "Security Audit", "Encrypt", "Decrypt", "Seal Protocol" -> {
                            scores[NluIntentId.SECURITY_CRYPTOGRAPHY] = (scores[NluIntentId.SECURITY_CRYPTOGRAPHY] ?: 0f) + 0.40f
                        }
                        "Clear / Reset", "Export", "Speech Synthesis" -> {
                            scores[NluIntentId.SYSTEM_COMMAND] = (scores[NluIntentId.SYSTEM_COMMAND] ?: 0f) + 0.45f
                        }
                    }
                }
                EntityCategory.TEMPORAL_TIME -> {
                    scores[NluIntentId.TASK_PRODUCTIVITY] = (scores[NluIntentId.TASK_PRODUCTIVITY] ?: 0f) + 0.20f
                }
                EntityCategory.NUMERICAL_METRIC -> {
                    scores[NluIntentId.CALCULATION_MATH_LOGIC] = (scores[NluIntentId.CALCULATION_MATH_LOGIC] ?: 0f) + 0.15f
                }
                EntityCategory.DOMAIN_TOPIC -> {
                    if (e.normalizedValue.contains("Cryptography") || e.normalizedValue.contains("Cybersecurity")) {
                        scores[NluIntentId.SECURITY_CRYPTOGRAPHY] = (scores[NluIntentId.SECURITY_CRYPTOGRAPHY] ?: 0f) + 0.35f
                    } else if (e.normalizedValue.contains("Mathematics")) {
                        scores[NluIntentId.CALCULATION_MATH_LOGIC] = (scores[NluIntentId.CALCULATION_MATH_LOGIC] ?: 0f) + 0.30f
                    } else {
                        scores[NluIntentId.EXPLANATION_FACTUAL] = (scores[NluIntentId.EXPLANATION_FACTUAL] ?: 0f) + 0.20f
                    }
                }
                else -> Unit
            }
        }

        // Structural Pattern Boosts
        if (MATH_EXPR_PATTERN.matcher(lowerText).find()) {
            scores[NluIntentId.CALCULATION_MATH_LOGIC] = (scores[NluIntentId.CALCULATION_MATH_LOGIC] ?: 0f) + 0.40f
        }

        if (lowerText.startsWith("how do i") || lowerText.startsWith("how to") || lowerText.startsWith("write a")) {
            if (codingMatches > 0) {
                scores[NluIntentId.CODING_ARCHITECTURE] = (scores[NluIntentId.CODING_ARCHITECTURE] ?: 0f) + 0.25f
            } else {
                scores[NluIntentId.EXPLANATION_FACTUAL] = (scores[NluIntentId.EXPLANATION_FACTUAL] ?: 0f) + 0.20f
            }
        }

        if (lowerText.contains("```") || lowerText.contains("fun ") || lowerText.contains("fn ") || lowerText.contains("def ") || lowerText.contains("import ")) {
            scores[NluIntentId.CODING_ARCHITECTURE] = (scores[NluIntentId.CODING_ARCHITECTURE] ?: 0f) + 0.50f
        }

        // Greeting phrase boost
        if (lowerText.contains("how you doin") || lowerText.contains("how are you") || lowerText.startsWith("hello") || lowerText.startsWith("hey") || lowerText.startsWith("hi ")) {
            scores[NluIntentId.GREETING_CASUAL] = (scores[NluIntentId.GREETING_CASUAL] ?: 0f) + 0.80f
        }

        // Normalize scores to probabilities between 0.0 and 1.0
        val maxScore = scores.values.maxOrNull() ?: 1.0f
        return scores.mapValues { (_, v) -> if (maxScore > 0f) (v / maxScore).coerceIn(0.10f, 0.98f) else 0.5f }
    }

    /**
     * Sentiment & Tone Extraction
     */
    private fun computeSentiment(lower: String): NluSentiment {
        var score = 0.0f
        var label = "Analytical"
        var emoji = "🧐"

        val urgentTokens = setOf("urgent", "asap", "immediately", "broken", "critical", "emergency", "crash", "stuck")
        val frustratedTokens = setOf("fail", "failing", "error", "hate", "terrible", "worst", "broken", "why does it not work")
        val enthusiasticTokens = setOf("awesome", "love", "great", "amazing", "cool", "super", "fantastic", "exciting")
        val curiousTokens = setOf("wonder", "curious", "why", "how come", "interesting", "explore", "learn")
        val playfulTokens = setOf("haha", "lol", "funny", "joke", "roast", "silly", "play")

        if (urgentTokens.any { lower.contains(it) }) {
            label = "Urgent / High Priority"
            emoji = "🚨"
            score = -0.4f
        } else if (frustratedTokens.any { lower.contains(it) }) {
            label = "Troubleshooting / Frustrated"
            emoji = "⚠️"
            score = -0.6f
        } else if (enthusiasticTokens.any { lower.contains(it) }) {
            label = "Enthusiastic"
            emoji = "🎉"
            score = 0.8f
        } else if (playfulTokens.any { lower.contains(it) }) {
            label = "Playful / Humorous"
            emoji = "😄"
            score = 0.6f
        } else if (curiousTokens.any { lower.contains(it) }) {
            label = "Curious / Inquisitive"
            emoji = "💡"
            score = 0.4f
        } else {
            label = "Analytical / Objective"
            emoji = "🧐"
            score = 0.1f
        }

        return NluSentiment(label = label, score = score, emoji = emoji)
    }

    /**
     * Complexity Level Detection
     */
    private fun computeComplexity(lower: String, entities: List<ExtractedEntity>): NluComplexity {
        val advancedKeywords = setOf(
            "monad", "ast", "compiler", "concurrency", "deadlock", "memory safety",
            "x25519", "ed25519", "chacha20", "quantum", "superposition", "eigenvalue",
            "differential", "asymptotic", "zero-cost", "borrow checker", "reentrancy",
            "cryptographic", "derivation", "aead", "proof"
        )
        val beginnerKeywords = setOf("beginner", "simple", "basics", "intro", "easy", "for dummies", "what is", "hello")

        val hasAdvanced = advancedKeywords.any { lower.contains(it) } || entities.any { it.category == EntityCategory.TECHNICAL_CONCEPT }
        val hasBeginner = beginnerKeywords.any { lower.contains(it) }

        return when {
            hasAdvanced -> NluComplexity.ADVANCED_EXPERT
            hasBeginner -> NluComplexity.BEGINNER
            entities.size >= 3 -> NluComplexity.ADVANCED_EXPERT
            entities.size >= 1 -> NluComplexity.INTERMEDIATE
            else -> NluComplexity.INTERMEDIATE
        }
    }

    private fun extractSignificantKeywords(lower: String): List<String> {
        val stopWords = setOf(
            "the", "is", "at", "which", "on", "a", "an", "and", "or", "in", "with",
            "to", "for", "of", "it", "my", "me", "you", "i", "can", "please", "help"
        )
        return lower.split(Regex("[^a-zA-Z0-9_+#.-]+"))
            .filter { it.length > 2 && !stopWords.contains(it) }
            .distinct()
            .take(6)
    }

    /**
     * Constructs a Structured Context Augmentation Directive
     * This directive is prepended or injected into the LLM Reasoning Pipeline so Joey responds with precision.
     */
    private fun buildContextDirective(
        primaryIntent: NluIntentId,
        confidence: Float,
        entities: List<ExtractedEntity>,
        sentiment: NluSentiment,
        complexity: NluComplexity
    ): String {
        val entitiesStr = if (entities.isEmpty()) "None explicitly detected" else entities.joinToString(", ") { "${it.category.label}: ${it.normalizedValue}" }

        val tacticalGuidance = when (primaryIntent) {
            NluIntentId.CODING_ARCHITECTURE -> "Deliver clean, memory-safe, idiomatic code examples with complete syntax highlighting, explaining time/space complexity and error handling."
            NluIntentId.EXPLANATION_FACTUAL -> "Provide deep, first-principles explanations, using data-backed facts and rigorous step-by-step logic."
            NluIntentId.TASK_PRODUCTIVITY -> "Structure response with clean actionable checklists, bullet points, milestones, and high-impact summaries."
            NluIntentId.CREATIVE_GENERATION -> "Produce original, engaging, vibrant concepts while maintaining internal narrative consistency."
            NluIntentId.CALCULATION_MATH_LOGIC -> "Provide step-by-step mathematical derivation, showing exact formulas, steps, and logical proof."
            NluIntentId.SECURITY_CRYPTOGRAPHY -> "Ensure zero-trust security standards, explaining key exchanges, cipher parameters, and integrity checks."
            NluIntentId.GREETING_CASUAL -> "Respond with warm, charismatic Joey energy and upbeat wit."
            NluIntentId.SYSTEM_COMMAND -> "Execute and confirm the system command with clarity."
            NluIntentId.MUSIC_AUDIO_SYNTH -> "Provide musical composition analysis, chord progressions, tempo, instrumentation structure, and highlight the open-source DSP & AI models (MusicGen, Stable Audio Open, Magenta)."
            NluIntentId.UNCERTAIN_GENERAL -> "Provide a comprehensive, logically sound, and articulate response."
        }

        return """
            [NLU Semantic Context Engine]
            • Primary Intent: ${primaryIntent.displayName} (${(confidence * 100).toInt()}% confidence)
            • Extracted Entities: $entitiesStr
            • User Tone / Sentiment: ${sentiment.label} (${sentiment.emoji})
            • Detected Complexity: ${complexity.label} (${complexity.badge})
            • Reasoning Directive: $tacticalGuidance
        """.trimIndent()
    }
}
