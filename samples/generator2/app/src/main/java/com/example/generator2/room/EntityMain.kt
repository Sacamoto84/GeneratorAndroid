package com.example.generator2.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey


@Entity
data class EntityPlaylist(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistName: String,     //Имя плейлиста

    val path: String = "", //путь до файла

    val name: String = "?",// Отображаемое имя файла

    val balance: Int = 0,     //Баланс    -100..100
    val volume: Float = 1.0f, //Громкость    0..1

    val isExist: Boolean = false, //Признак того что файл существует

    val isExist1: Boolean = false,
    val isExist2: Boolean = false,
    val isExist3: Boolean = false,

    @ColumnInfo(defaultValue = "false")
    val isExist4: Boolean = false,

    /* Мета данные */
    val lengthInSeconds: String = "",
    val sampleRate: String = "",
    val bitRate: String = "",
    val channelMode: String = "",

    val additionalData: String = "" //JSON для map

    //val icon: Bitmap? = null,

)


@Dao
interface EntityMainDao {
    @Insert
    fun insert(mainTable: EntityPlaylist)

}