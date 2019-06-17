package com.example.mybookapplication

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                    .addCallback(FileDataBaseCallback(scope))
                    .build()
                INSTANCE = instance
                return instance
            }
        }
        private class FileDataBaseCallback(private val scope:CoroutineScope):RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.fileDataDao())
                    }
                }
            }
        }
        suspend fun populateDatabase(fileDataDao: FileDataDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            //fileDataDao.deleteAll()
        }
    }
}
