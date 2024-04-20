package com.example.generator2.room

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey


@Entity
data class EntityPlaylist(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val i: Int = 1,         //Добавили в 2 версии

)


@Dao
interface EntityMainDao {
    @Insert
    fun insert(mainTable: EntityPlaylist)

}