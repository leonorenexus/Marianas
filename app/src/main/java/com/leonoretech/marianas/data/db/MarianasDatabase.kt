package com.leonoretech.marianas.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SessionEntity::class, MessageEntity::class, ConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MarianasDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
    abstract fun configDao(): ConfigDao

    companion object {
        @Volatile
        private var INSTANCE: MarianasDatabase? = null

        fun getInstance(context: Context): MarianasDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MarianasDatabase::class.java,
                    "marianas_ai.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
