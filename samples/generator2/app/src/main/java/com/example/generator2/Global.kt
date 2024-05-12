package com.example.generator2

import android.media.AudioFormat
import com.example.generator2.features.audio.AudioOut
import com.example.generator2.backup.MMKv
import com.example.generator2.features.explorer.model.ExploreNodeItem
import com.example.generator2.features.noSQL.NoSQL
import com.example.generator2.model.TreeNode
import javax.inject.Inject
import javax.inject.Singleton


var audioOut: AudioOut = AudioOut(48000, 200, AudioFormat.ENCODING_PCM_FLOAT)


@Singleton
class Global @Inject constructor(
    val appPath: AppPath
)
{
    val mmkv = MMKv(appPath)


    //language | "ru" "en" - выбор языка
    val noSQLConfig2 = NoSQL(path = appPath.config, nameDB = "config2")





}