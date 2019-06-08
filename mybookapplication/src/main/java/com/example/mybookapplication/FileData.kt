package com.example.mybookapplication

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(indices = [Index(value= "FilePath", unique = true)])
data class FileData (
    @ColumnInfo(name="FileName") var FileName:String,
    @ColumnInfo(name="FilePath") var FilePath:String,
    @ColumnInfo(name="CurPage") var CurPage:Int?,
    @ColumnInfo(name="Favourite") var Fav:Boolean,
    @ColumnInfo(name="HaveRead") var HaveRead:Boolean,
    @ColumnInfo(name="Wishes") var Wishes:Boolean
    ) {
    @PrimaryKey(autoGenerate = true)
    var id:Long? = null
}