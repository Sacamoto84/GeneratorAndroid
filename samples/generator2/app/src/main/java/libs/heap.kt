package libs

//            Button(
//                onClick = {
//                    val timings = System.currentTimeMillis()
//                         val thread = Thread(){
//                             coroutineScope1.launch (){
//                                 println("Тест скорости-----------------------------")
//                                 val time = measureTimeMillis()
//                                 {
//                                     concurrentSum()
//                                 }
//                                 println("Готово за $time ms count = $count ")
//                                 println("----------------------------- Корутина все -----------------------------")
//                             }
//                         }
//                         thread.start()
//runBlocking<Unit> {
//                }
//
//            ) {
//                Text(text = "Заеба")
//            }


// }




//val max = 4000
//var count = 0
//
//
//
//
//val potok = 16
//
//suspend fun one(): Int {
//    println("Запуск One")
//    for (p3 in 2 .. max/potok step 2) {
//        for (p2 in 2 until max step 2) {
//            for (p1 in 2 until max step 2) {
//                if (((p3 > p2) && (p2 > p1) && ((p1 + p2 + p3) == max))) {
//
//                    count++;
//                }
//            }
//        }
//    }
//    println("Конец One")
//    return 1
//}
//
//
//suspend fun concurrentSum(): Int = coroutineScope {
//
//    val one1 = async (Dispatchers.IO){ one() }
//    val two1 = async (Dispatchers.IO){ one() }
//    one1.start()
//    two1.start()
//    launch {
//        println("Ждем One + Two")
//        one1.await() + two1.await()
//    }
//     1
//}

//.pointerInput(Unit) {
//    detectTapGestures(
//        onPress = { /* Called when the gesture starts */ },
//        onDoubleTap = { /* Called on Double Tap */ },
//        onLongPress = { /* Called on Long Press */ },
//        onTap = {
//            navController.popBackStack()
//        }
//    )
//
//}