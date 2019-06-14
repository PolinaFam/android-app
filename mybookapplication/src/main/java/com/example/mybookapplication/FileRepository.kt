package com.example.mybookapplication

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread

class FileRepository (private val fileDataDao: FileDataDao) {
    val allFiles: LiveData<List<FileData>> = fileDataDao.getAll()

    @WorkerThread
    fun insert(file:FileData) {
        fileDataDao.insertFile(file)
    }
}