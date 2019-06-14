package com.example.mybookapplication

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers

class ListViewModel(application: Application) : AndroidViewModel(application){
    private val repository:FileRepository
    val allFiles: LiveData<List<FileData>>
    init {
        val filesDataDao = FileDataBase.getDB(application).fileDataDao()
        repository = FileRepository(filesDataDao)
        allFiles = repository.allFiles
    }
    //fun insert(file:FileData) = viewModelScope.launch(Dispatchers.IO) {
    //    repository.insert(file)
    //}
    //val db = FileDataBase.getDB(application)
    //val allFiles = db?.fileDataDao()?.getAll()  //все книги
    //val favFiles = db?.fileDataDao()?.findFav() //только любимые
    //val wishFiles = db?.fileDataDao()?.findWishes()
    //val finFiles = db?.fileDataDao()?.findHaveRead()
}