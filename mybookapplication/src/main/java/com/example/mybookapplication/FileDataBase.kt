package com.example.mybookapplication

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import kotlinx.coroutines.CoroutineScope

@Database(entities = [(FileData::class)], version = 1)
@TypeConverters(DateConverter::class)
public abstract class FileDataBase : RoomDatabase() {
    abstract fun fileDataDao() : FileDataDao

    companion object {
        @Volatile
        private var INSTANCE:FileDataBase? = null

        fun getDB(context: Context, scope:CoroutineScope):FileDataBase {
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
        fun deleteDB(context: Context){
            context.deleteDatabase("file.db")
        }
    }
}
