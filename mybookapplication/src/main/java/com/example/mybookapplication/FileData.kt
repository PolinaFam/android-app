package com.example.mybookapplication

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(indices = [Index(value="FilePath", unique = true)])
data class FileData (
    var FileName:String = "",
    var FilePath:String = "",
    var CurPage:Int = 0,
    var Fav:Boolean = false,
    var HaveRead:Boolean = false,
    var Wishes:Boolean = false
    ) {
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0
}