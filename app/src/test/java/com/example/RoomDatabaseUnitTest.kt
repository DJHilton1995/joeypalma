package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ai.JoeyPersona
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.JoeyDatabase
import com.example.data.repository.JoeyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseUnitTest {

    private lateinit var database: JoeyDatabase
    private lateinit var chatDao: ChatDao
    private lateinit var repository: JoeyRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, JoeyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        chatDao = database.chatDao()
        repository = JoeyRepository(chatDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveSessionWithMessages() = runBlocking {
        val sessionId = repository.createNewSession("Quantum Computing Discussion", JoeyPersona.TECH_ARCHITECT)
        assertTrue(sessionId > 0L)

        val sessions = repository.getSessions().first()
        assertEquals(1, sessions.size)
        assertEquals("Quantum Computing Discussion", sessions[0].title)
        assertEquals(JoeyPersona.TECH_ARCHITECT.id, sessions[0].personaId)

        val messages = repository.getMessages(sessionId).first()
        assertEquals(1, messages.size)
        assertEquals("joey", messages[0].sender)
        assertTrue(messages[0].content.contains("architect") || messages[0].content.contains("developer"))
    }

    @Test
    fun testSendMessagePersistenceAndContextHistory() = runBlocking {
        val sessionId = repository.createNewSession("Coding Session", JoeyPersona.TECH_ARCHITECT)

        // Simulate sending a user message
        val prompt = "How do I optimize Room SQLite queries in Kotlin Compose?"
        val initialHistory = repository.getMessages(sessionId).first().map { it.sender to it.content }

        val result = repository.sendMessage(
            sessionId = sessionId,
            userPrompt = prompt,
            persona = JoeyPersona.TECH_ARCHITECT,
            history = initialHistory
        )

        assertTrue(result.isSuccess)

        // Verify that Room persisted both the user message and Joey's response
        val updatedMessages = repository.getMessages(sessionId).first()
        assertEquals(3, updatedMessages.size) // Welcome + User + Joey Response

        val userMsg = updatedMessages.find { it.sender == "user" }
        assertNotNull(userMsg)
        assertEquals(prompt, userMsg?.content)
        assertEquals("CODING_ARCHITECTURE", userMsg?.nluIntent)

        val botMsg = updatedMessages.find { it.sender == "joey" && it.id != updatedMessages[0].id }
        assertNotNull(botMsg)
        assertTrue(botMsg?.content?.isNotBlank() == true)

        // Verify updated message count on session
        val session = chatDao.getSessionById(sessionId)
        assertEquals(3, session?.messageCount)
    }

    @Test
    fun testSessionPinningAndRenaming() = runBlocking {
        val sessionId = repository.createNewSession("Project Alpha", JoeyPersona.CLASSIC)
        repository.setPinned(sessionId, true)

        var session = chatDao.getSessionById(sessionId)
        assertEquals(true, session?.isPinned)

        repository.updateSessionTitle(sessionId, "Project Alpha (Updated)")
        session = chatDao.getSessionById(sessionId)
        assertEquals("Project Alpha (Updated)", session?.title)
    }

    @Test
    fun testMessageLikesAndDeletion() = runBlocking {
        val sessionId = repository.createNewSession("Test Chat", JoeyPersona.CLASSIC)
        val initialMessages = repository.getMessages(sessionId).first()
        val firstMsgId = initialMessages[0].id

        repository.setMessageLiked(firstMsgId, true)
        var messages = repository.getMessages(sessionId).first()
        assertEquals(true, messages[0].isLiked)

        repository.deleteMessage(firstMsgId)
        messages = repository.getMessages(sessionId).first()
        assertEquals(0, messages.size)
    }

    @Test
    fun testSearchAndClearAllData() = runBlocking {
        val session1 = repository.createNewSession("Kotlin Coroutines Tips", JoeyPersona.TECH_ARCHITECT)
        val session2 = repository.createNewSession("Pizza Recipe Ideas", JoeyPersona.CLASSIC)

        chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = session1,
                sender = "user",
                content = "Room database indexing improves speed",
                isEncrypted = false
            )
        )

        val searchResults = repository.searchMessages("indexing")
        assertEquals(1, searchResults.size)
        assertEquals("Room database indexing improves speed", searchResults[0].content)

        repository.clearAllData()
        val sessionsAfterClear = repository.getSessions().first()
        assertEquals(0, sessionsAfterClear.size)
    }
}
