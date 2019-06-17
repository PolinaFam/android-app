package com.example.mybookapplication

import androidx.lifecycle.LiveData
import androidx.annotation.WorkerThread

class FileRepository (private val fileDataDao: FileDataDao) {
    val allFiles: LiveData<List<FileData>> = fileDataDao.getAll()
    val favFiles: LiveData<List<FileData>> = fileDataDao.findFav()
    val wishFiles: LiveData<List<FileData>> = fileDataDao.findWishes()
    val finFiles: LiveData<List<FileData>> = fileDataDao.findHaveRead()

    @WorkerThread
    suspend fun insert(file:FileData) {
        fileDataDao.insertFile(file)
    }
    suspend fun delete(file:FileData) {
        fileDataDao.deleteFile(file)
    }
    suspend fun update(file: FileData) {
        fileDataDao.updateFile(file)
    }
    fun find(id:Long):FileData {
        return fileDataDao.findById(id)
    }
}