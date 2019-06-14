package com.example.mybookapplication

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import java.util.*

@Entity(indices = [Index(value="FilePath", unique = true)])
data class FileData (
    var FileName:String = "",
    var FilePath:String = "",
    var CurPage:Int = 0,
    var Size:String = "",
    @TypeConverters(DateConverter::class)
    var DateOfAdding: Date?,
    var Fav:Boolean = false,
    var HaveRead:Boolean = false,
    var Wishes:Boolean = false
    ) {
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0
}