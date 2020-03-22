package com.example.mybookapplication

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListViewModel(application: Application) : AndroidViewModel(application){
    private val repository:FileRepository
    var allFilesDb: LiveData<List<FileData>>
    var favFilesDb:LiveData<List<FileData>>
    var wishFilesDb:LiveData<List<FileData>>
    var finFilesDb:LiveData<List<FileData>>
    var Files = MediatorLiveData<List<FileData>>()
    var currentCategory = "All"
    var currentOrder = "None"

    init {
        val filesDataDao = FileDataBase.getDB(application,viewModelScope).fileDataDao()
        repository = FileRepository(filesDataDao)
        allFilesDb = repository.allFiles
        favFilesDb = repository.favFiles
        wishFilesDb = repository.wishFiles
        finFilesDb = repository.finFiles

        Files.addSource(allFilesDb) { result:List<FileData>? ->
            if (currentCategory == "All") { result?.let {Files.value = it} }
        }
        Files.addSource(favFilesDb) {result:List<FileData>? ->
            if (currentCategory == "Fav") { result?.let {Files.value = it} }
        }
        Files.addSource(wishFilesDb) {result:List<FileData>? ->
            if (currentCategory == "Wish") { result?.let {Files.value = it} }
        }
        Files.addSource(finFilesDb) {result:List<FileData>? ->
            if (currentCategory == "Fin") { result?.let {Files.value = it} }
        }
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
    fun changeList(cat:String) = when (cat) {
        "All" -> allFilesDb.value?.let {Files.value = it}
        "Fav" -> favFilesDb.value?.let {Files.value = it}
        "Wish" -> wishFilesDb.value?.let {Files.value = it}
        "Fin" -> finFilesDb.value?.let {Files.value = it}
        else -> println("Nothing")
    }.also { currentCategory = cat }

    fun sortList(order:String) = when (order) {
        "Name" -> Files.value?.let {Files.value = it.sortedBy { it.FileName }}
        "Date" -> Files.value?.let {Files.value = it.sortedBy { it.DateOfAdding }}
        "Size" -> Files.value?.let {Files.value = it.sortedBy { it.Size }}
        else -> println("Nothing")
    }.also { currentOrder = order }

}