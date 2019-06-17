package com.example.mybookapplication

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@Entity(indices = [Index(value=["FilePath"], unique = true)])
data class FileData (
    var FileName:String = "",
    var FilePath:String = "",
    var CurPage:Int = 0,
    var Pages:Int = 0,
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