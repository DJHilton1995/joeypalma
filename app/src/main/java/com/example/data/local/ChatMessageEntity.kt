package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["sessionId", "timestamp"])]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sessionId: Long,
    val sender: String, // "user" or "joey"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = "gemini-3.5-flash",
    val personaId: String = "classic",
    val checksum: String = "",
    val isLiked: Boolean = false,
    val isEncrypted: Boolean = false,
    val nluIntent: String = "",
    val nluEntities: String = "",
    val nluSentiment: String = ""
)
