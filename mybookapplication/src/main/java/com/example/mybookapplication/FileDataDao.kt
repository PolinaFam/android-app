package com.example.mybookapplication

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FileDataDao {
    @Query("SELECT * from FileData")
    //fun getAll(): List<FileData>
    fun getAll(): LiveData<List<FileData>>

    @Query("SELECT * from FileData WHERE id LIKE :id")
    fun findById(id: Long): FileData

    @Query("SELECT * from FileData WHERE Fav = 1")
    //fun findFav(): List<FileData>
    fun findFav(): LiveData<List<FileData>>

    @Query("SELECT * from FileData WHERE HaveRead = 1")
    //fun findHaveRead(): List<FileData>
    fun findHaveRead(): LiveData<List<FileData>>

    @Query("SELECT * from FileData WHERE Wishes = 1")
    //fun findWishes(): List<FileData>
    fun findWishes(): LiveData<List<FileData>>

    @Query("SELECT * from FileData ORDER BY FileName ASC")
    fun sortName(): List<FileData>

    @Query("SELECT * from FileData ORDER BY DateOfAdding DESC")
    fun sortDate(): List<FileData>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFile(file: FileData)

    @Delete
    fun deleteFile(file: FileData?)

    @Query("DELETE from FileData")
    fun deleteAll()

    @Update
    fun updateFile(file: FileData)
}