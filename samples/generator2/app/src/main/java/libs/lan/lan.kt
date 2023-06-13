package libs.lan

import java.net.InetAddress

// ! Добавить в манифест
// <uses-permission android:name= "android.permission.INTERNET" />
// <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
// <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />


//=====================================================
// IP сделать broadcast "192.168.0.100" -> "192.168.0.255"
// region > ipToBroadCast(value : String): String
fun ipToBroadCast(value: String): String {
    val ip = InetAddress.getByName(value)
    val bytes = ip.address
    val ibytes: IntArray = IntArray(4)
    for (i in bytes.indices) {
        if (bytes[i] >= 0) ibytes[i] = bytes[i].toInt()
        else {
            ibytes[i] = 256 + bytes[i].toInt()
        }
    }
    ibytes[3] = 255
    val broadcast = "${ibytes[0]}.${ibytes[1]}.${ibytes[2]}.255"
    println("ipToBroadCast : $broadcast")
    return broadcast
}
//endregion
//=====================================================


fun ping2(ip: String = "192.168.0.200"): Boolean {
    val address = InetAddress.getByName("192.168.0.200")
    return address.isReachable(1000)
}

fun runSystemCommand(command: String?):Boolean {
    try {
        val p = Runtime.getRuntime().exec(command)
        val exitValue: Int = p.waitFor()
        //println("runSystemCommand exitValue ========= $exitValue")
        return (exitValue == 0)
        /*
        val inputStream = BufferedReader( InputStreamReader(p.inputStream)   )
        var s: String? = ""
        while (inputStream.readLine().also { s = it } != null)
        {  println(s)  }
         */

    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        println(e.message)
    }
    return false
}


fun ping3(ip: String = "192.168.0.200"): Boolean {
    return runSystemCommand("/system/bin/ping -c 1 $ip")
}

/*
public boolean isConnectedToThisServer(String host) {
    Runtime runtime = Runtime.getRuntime();
    try {
        Process ipProcess = runtime.exec("/system/bin/ping
                -c 1 8.8.8.8" + host);
        int exitValue = ipProcess.waitFor();
        return (exitValue == 0);
    } catch (IOException e) {
        e.printStackTrace();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    return false;
}
 */