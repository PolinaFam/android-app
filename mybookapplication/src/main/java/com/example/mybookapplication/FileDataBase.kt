package com.example.mybookapplication

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = [(FileData::class)], version = 1)
abstract class FileDataBase : RoomDatabase() {

    abstract fun fileDataDao() : FileDataDao

    companion object {
        var INSTANCE:FileDataBase? = null

        fun getDB(context: Context):FileDataBase? {
            if (INSTANCE == null) {
                synchronized(FileDataBase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        FileDataBase::class.java,"file.db")
                        .allowMainThreadQueries()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyDB() {
            INSTANCE = null
        }
    }
}