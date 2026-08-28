package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Joey's Memories - Room Database for persisting chat history, sessions, and NLU metadata.
 */
@Database(
    entities = [ChatSessionEntity::class, ChatMessageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class JoeyDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        const val DATABASE_NAME = "joeys_memories.db"

        @Volatile
        private var INSTANCE: JoeyDatabase? = null

        fun getInstance(context: Context): JoeyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JoeyDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

typealias JoeysMemoriesDatabase = JoeyDatabase
