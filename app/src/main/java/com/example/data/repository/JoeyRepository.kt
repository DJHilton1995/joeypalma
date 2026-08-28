package com.example.data.repository

import com.example.ai.GeminiChatService
import com.example.ai.JoeyPersona
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.nlu.NluEngine
import com.example.security.RustyMcpEngine
import com.example.security.SecureVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class JoeyRepository(
    private val chatDao: ChatDao,
    private val geminiService: GeminiChatService = GeminiChatService()
) {

    fun getSessions(): Flow<List<ChatSessionEntity>> {
        return chatDao.getAllSessions()
    }

    fun getMessages(sessionId: Long): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId).map { list ->
            list.map { msg ->
                if (msg.isEncrypted) {
                    msg.copy(content = SecureVault.decrypt(msg.content))
                } else {
                    msg
                }
            }
        }
    }

    suspend fun createNewSession(
        title: String = "New Conversation",
        persona: JoeyPersona = JoeyPersona.CLASSIC
    ): Long = withContext(Dispatchers.IO) {
        val session = ChatSessionEntity(
            title = title,
            personaId = persona.id,
            messageCount = 1,
            lastUpdatedAt = System.currentTimeMillis()
        )
        val sessionId = chatDao.insertSession(session)

        // Seed welcome message
        val welcome = persona.welcomeMessage
        val encrypted = SecureVault.encrypt(welcome)
        val checksum = SecureVault.computeChecksum(welcome)

        chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                sender = "joey",
                content = encrypted,
                personaId = persona.id,
                checksum = checksum,
                isEncrypted = true,
                nluIntent = "GREETING_CASUAL",
                nluEntities = "👋 Welcome, 😎 Joey AI",
                nluSentiment = "Enthusiastic"
            )
        )

        sessionId
    }

    suspend fun sendMessage(
        sessionId: Long,
        userPrompt: String,
        persona: JoeyPersona,
        history: List<Pair<String, String>>,
        customApiKey: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val sanitizedPrompt = SecureVault.sanitizePrompt(userPrompt)

        // 1. RustyMCP Hardened Message Sealing & Decryption verification
        val sealedClientMsg = SecureVault.sealMcpMessage(sanitizedPrompt)
        val verifiedClient = SecureVault.processMcpMessage(sealedClientMsg)
        val cleanPrompt = verifiedClient.getOrNull()?.plaintext ?: sanitizedPrompt

        // 2. Natural Language Understanding (NLU) Pipeline Analysis
        val nluAnalysis = NluEngine.analyze(cleanPrompt)
        val entitiesSummary = nluAnalysis.entities.joinToString(", ") { "${it.category.emoji} ${it.normalizedValue}" }

        val userEncrypted = SecureVault.encrypt(cleanPrompt)
        val userChecksum = SecureVault.computeChecksum(cleanPrompt)

        // Save User Message with NLU Metadata
        chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                sender = "user",
                content = userEncrypted,
                personaId = persona.id,
                checksum = userChecksum,
                isEncrypted = true,
                nluIntent = nluAnalysis.primaryIntent.name,
                nluEntities = entitiesSummary,
                nluSentiment = "${nluAnalysis.sentiment.label} ${nluAnalysis.sentiment.emoji}"
            )
        )

        // Auto-update session title if it's default
        val session = chatDao.getSessionById(sessionId)
        if (session != null && (session.title == "New Conversation" || session.title.isBlank())) {
            val shortTitle = if (cleanPrompt.length > 28) {
                cleanPrompt.take(28) + "..."
            } else {
                cleanPrompt
            }
            chatDao.updateSessionTitle(sessionId, shortTitle)
        }

        // 3. Call Gemini Service with NLU Context Augmentation
        val result = geminiService.sendMessage(
            prompt = cleanPrompt,
            history = history,
            persona = persona,
            customApiKey = customApiKey,
            nluAnalysis = nluAnalysis
        )

        val responseText = result.getOrDefault("Joey is ready! Let me know what you'd like to do next.")

        // 4. RustyMCP Server-Side Response Sealing verification
        RustyMcpEngine.sealServerResponse(
            clientEphemeralPubB64 = sealedClientMsg.client_ephemeral_x25519,
            seq = sealedClientMsg.sequence_number,
            responseText = responseText
        )

        // 5. Save Joey's Message with NLU Intelligence Metadata
        val joeyEncrypted = SecureVault.encrypt(responseText)
        val joeyChecksum = SecureVault.computeChecksum(responseText)

        chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                sender = "joey",
                content = joeyEncrypted,
                personaId = persona.id,
                checksum = joeyChecksum,
                isEncrypted = true,
                nluIntent = nluAnalysis.primaryIntent.name,
                nluEntities = entitiesSummary,
                nluSentiment = "${nluAnalysis.complexity.label} (${nluAnalysis.complexity.badge})"
            )
        )

        // Update session meta
        val count = chatDao.getMessageCount(sessionId)
        chatDao.updateSession(
            session?.copy(
                messageCount = count,
                lastUpdatedAt = System.currentTimeMillis()
            ) ?: ChatSessionEntity(
                id = sessionId,
                title = "Chat",
                messageCount = count,
                lastUpdatedAt = System.currentTimeMillis()
            )
        )

        Result.success(responseText)
    }

    suspend fun setPinned(sessionId: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        chatDao.setPinned(sessionId, isPinned)
    }

    suspend fun updateSessionTitle(sessionId: Long, title: String) = withContext(Dispatchers.IO) {
        chatDao.updateSessionTitle(sessionId, title)
    }

    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun setMessageLiked(messageId: Long, isLiked: Boolean) = withContext(Dispatchers.IO) {
        chatDao.setMessageLiked(messageId, isLiked)
    }

    suspend fun deleteMessage(messageId: Long) = withContext(Dispatchers.IO) {
        chatDao.deleteMessage(messageId)
    }

    suspend fun searchMessages(query: String): List<ChatMessageEntity> = withContext(Dispatchers.IO) {
        chatDao.searchMessages(query).map { msg ->
            if (msg.isEncrypted) {
                msg.copy(content = SecureVault.decrypt(msg.content))
            } else {
                msg
            }
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        chatDao.clearAllMessages()
        chatDao.clearAllSessions()
    }
}
