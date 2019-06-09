package com.example.mybookapplication

import android.arch.persistence.room.*

@Dao
interface FileDataDao {
    @Query("SELECT * from FileData")
    fun getAll(): List<FileData>

    @Query("SELECT * from FileData WHERE id LIKE :id")
    fun findById(id: Long): FileData

    @Query("SELECT * from FileData WHERE Fav = 1")
    fun findFav(): List<FileData>

    @Query("SELECT * from FileData WHERE HaveRead = 1")
    fun findHaveRead(): List<FileData>

    @Query("SELECT * from FileData WHERE Wishes = 1")
    fun findWishes(): List<FileData>

    @Query("SELECT * from FileData ORDER BY FileName ASC")
    fun sortName(): List<FileData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFile(file: FileData)

    @Delete
    fun deleteFile(file: FileData?)

    @Query("DELETE from FileData")
    fun deleteAll()

    @Update
    fun updateFile(file: FileData)
}