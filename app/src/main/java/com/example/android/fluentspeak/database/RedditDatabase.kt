package com.example.android.fluentspeak.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Conversation::class, Utterance::class], version = 1, exportSchema = false)
abstract class RedditDatabase : RoomDatabase() {

    abstract val redditDatabaseDao: RedditDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: RedditDatabase? = null

        fun getInstance(context: Context): RedditDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        RedditDatabase::class.java,
                        "reddit_database"
                    )
                        .fallbackToDestructiveMigration()
                        .createFromAsset("database/reddit.db")
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}