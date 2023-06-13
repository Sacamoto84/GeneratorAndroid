package com.example.generator2.screens.config.vm

////Section saveBackupToFirebase
////Сохранить бекап по пути /user/UID/backup.zip
//
//fun saveBackupToFirebase(uid : String, backup: Backup) {
//    progressMetadata.value = true
//    strMetadataError.value = ""
//    strMetadata.value = ""
//    val storage : FirebaseStorage = FirebaseStorage.getInstance()
//    val storageRef = storage.reference //Корневая папка gs://test-e538d.appspot.com/
//    //val uid = global.firebase.auth.uid
//    if ((uid == "") || (uid == null)) return
//    val fil = File(backup.getPathToBackup())
//    if (!fil.exists()) {
//        progressMetadata.value = false
//        strMetadataError.value = "local backup.zip not found"
//        return
//    }
//    val lastModified = fil.lastModified()  //Время последнего изменения
//    val s = storageRef.child("/user/${uid}/backup.zip")
//        .putFile(backup.getURIBackup(), storageMetadata {
//            contentType = "application/zip"
//            setCustomMetadata("time_create", lastModified.toString())
//        }).addOnCompleteListener {
//            if (it.isSuccessful) {
//                println("backup addOnCompleteListener..ok")
//                readMetaBackupFromFirebase(uid)
//            } else {
//                Log.e("saveBackupToFirebase", "${it.exception?.message}")
//                strMetadataError.value =
//                    it.exception?.message.toString() //Вывод сообщения об ошибке
//            }
//            progressMetadata.value = false
//        }.addOnFailureListener {
//            println("backup addOnFailureListener:$it")
//            strMetadataError.value = it.toString() //Вывод сообщения об ошибке
//            progressMetadata.value = false
//        }
//}
//
////Section readMetaBackupFromFirebase
////Сохранить бекап по пути /user/UID/backup.zip
//fun readMetaBackupFromFirebase(uid : String?) {
//
//    strMetadata.value = ""
//    strMetadataError.value = ""
//    progressMetadata.value = true
//
//    println("metadata")
//
//    val storage : FirebaseStorage = FirebaseStorage.getInstance()
//    val storageRef = storage.reference //Корневая папка
//
//    if ((uid != "") && (uid != null)) {
//        println("metadata 3 uid:${uid}")
//        val s = storageRef.child("/user/${uid}/backup.zip").metadata.addOnCompleteListener {
//            if (it.isSuccessful) {
//                println("metadata addOnCompleteListener..isSuccessful")
//                println("metadata sizeBytes " + it.result.sizeBytes) //Размер файла
//                println("metadata creationTimeMillis " + it.result.creationTimeMillis) //Время создания файла в помойке
//
//                //Метаданные времени cоздания файла на андроид
//                val time_create = it.result.getCustomMetadata("time_create")?.toLong()
//                println("metadata time_create $time_create")
//
//                var str =
//                    "Time file creation : " + time_create?.let { it1 -> Date(it1) }?.let { it2 ->
//                            DateToString(it2)
//                        } + "\n"
//                str += "Time saved in cloud: " + DateToString(Date(it.result.creationTimeMillis)) + "\n"
//                str += "size: ${it.result.sizeBytes} byte"
//
//                strMetadata.value = str
//                Log.i("Firebase", "metadata..ok")
//
//            } else {
//                Log.e("Firebase", "metadata error> ${it.exception?.message}")
//                strMetadataError.value = it.exception?.message.toString()
//            }
//
//            progressMetadata.value = false
//
//        }.addOnFailureListener {
//            println("metadata addOnFailureListener:$it")
//            strMetadataError.value = it.toString()
//            progressMetadata.value = false
//        }
//    }
//
//}
//
//private fun DateToString(r: Date): String {
//    return (1900 + r.year - 2000).toString() + "/" + r.month.toString() + "/" + r.date.toString() + " " + r.hours.toString() + ":" + r.minutes.toString() + ":" + r.seconds.toString()
//}