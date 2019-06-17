package com.example.mybookapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ListViewModel(application: Application) : AndroidViewModel(application){
    private val repository:FileRepository
    val allFiles: LiveData<List<FileData>>
    val favFiles: LiveData<List<FileData>>
    val wishFiles: LiveData<List<FileData>>
    val finFiles: LiveData<List<FileData>>
    init {
        val filesDataDao = FileDataBase.getDB(application,viewModelScope).fileDataDao()
        repository = FileRepository(filesDataDao)
        allFiles = repository.allFiles
        favFiles = repository.favFiles
        wishFiles = repository.wishFiles
        finFiles = repository.finFiles
    }
    fun insert(file:FileData) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(file)
    }
    fun delete(file:FileData) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(file)
    }
    fun update(file:FileData) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(file)
    }
    fun findForUpdate(id:Long, p:Int)= viewModelScope.launch(Dispatchers.IO) {
        val file = repository.find(id)
        file.CurPage = p
        update(file)
    }
}