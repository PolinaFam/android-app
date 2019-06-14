package com.example.mybookapplication

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

@Database(entities = [(FileData::class)], version = 1)
@TypeConverters(DateConverter::class)
public abstract class FileDataBase : RoomDatabase() {
    abstract fun fileDataDao() : FileDataDao

    companion object {
        @Volatile
        private var INSTANCE:FileDataBase? = null

        fun getDB(context: Context):FileDataBase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FileDataBase::class.java,
                    "file.db")
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}