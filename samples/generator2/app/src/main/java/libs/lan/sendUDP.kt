package libs.lan

import android.os.StrictMode
import timber.log.Timber
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

//=====================================================
// Отправить Udp сообщение * Возвращает OK или ошибку
// region // sendUDP(messageStr: String, ip :String, port: Int): String

fun sendUDP(messageStr: String, ip: String, port: Int): String {
    // Hack Prevent crash (sending should be done using an async task)
    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
    StrictMode.setThreadPolicy(policy)
    try {
        val socket = DatagramSocket()
        socket.broadcast = true
        val sendData = messageStr.toByteArray()
        val sendPacket =
            DatagramPacket(sendData, sendData.size, InetAddress.getByName(ip), port)
        socket.send(sendPacket)
        println("sendUDP: $ip:$port")
    } catch (e: IOException) {
        Timber.e("IOException: " + e.message)
        return e.message.toString()
    }
    return "OK"
}
