package com.example.mybookapplication

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FileDataDao {
    @Query("SELECT * from FileData")
    fun getAll(): LiveData<List<FileData>>

    @Query("SELECT * from FileData WHERE id LIKE :id")
    fun findById(id: Long): FileData

    @Query("SELECT * from FileData WHERE Fav = 1")
    fun findFav(): LiveData<List<FileData>>

    @Query("SELECT * from FileData WHERE HaveRead = 1")
    fun findHaveRead(): LiveData<List<FileData>>

    @Query("SELECT * from FileData WHERE Wishes = 1")
    fun findWishes(): LiveData<List<FileData>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFile(file: FileData)

    @Delete
    fun deleteFile(file: FileData?)

    @Query("DELETE from FileData")
    fun deleteAll()

    @Update
    fun updateFile(file: FileData)
}