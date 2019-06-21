package com.example.mybookapplication

import androidx.annotation.WorkerThread

class FileRepository (private val fileDataDao: FileDataDao) {
    var allFiles = fileDataDao.getAll()
    var favFiles = fileDataDao.findFav()
    var wishFiles = fileDataDao.findWishes()
    var finFiles = fileDataDao.findHaveRead()

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