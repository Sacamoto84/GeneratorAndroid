package com.example.generator2.explorer

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.generator2.AppPath
import com.example.generator2.di.Hub
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ScreenExplorerViewModel @Inject constructor(
     @ApplicationContext val context: Context,
    val hub: Hub
) : ViewModel() {

    val explorerCurrentDir = MutableStateFlow(AppPath().music) //Текущая рабочая папка

    var update by mutableIntStateOf(0)

    val listItems = mutableListOf <ExplorerItem>()

    init {
        //observe()
        //scan()
    }


    fun observe()
    {

        viewModelScope.launch(Dispatchers.IO) {
            explorerCurrentDir.collect {




            }
        }

    }


    fun scan()
    {

        listItems.clear()

        try {
            val directory = File(explorerCurrentDir.value)
            if (directory == null) {
                Timber.e("Ошибка в ${explorerCurrentDir.value}")
                return
            }

            if (directory.exists() && directory.isDirectory) {

                val files = directory.listFiles()

                if (files != null) {

                    for (file in files) {

                        if (file.isDirectory) {
                            listItems.add(ExplorerItem(isDirectory = true, name = file.name, fullPatch = file.path))
                            println("${file.name} - это папка")
                        } else {
                            listItems.add(ExplorerItem(isDirectory = false, name = file.name, fullPatch = file.path))
                            println("${file.name} - это файл")
                        }

                    }
                } else {
                    println("Ошибка при получении списка файлов")
                }
            } else {
                println("Папка не существует или это не папка")
            }








        }
        catch (e : Exception)
        {
            Timber.e(e.localizedMessage)
        }

        listItems
        update++


    }









//    /**
//     * Получить список файлов по пути
//     */
//    fun filesInDirToList(
//
//    ): List<String> { ///storage/emulated/0/Android/data/com.example.generator2/files
//
//
//        val r: MutableList<String> = mutableListOf()
//        if (pathDocuments != null) {
//            pathDocuments.list()?.let { r.addAll(it) }
//        }
//        return r
//    }

}