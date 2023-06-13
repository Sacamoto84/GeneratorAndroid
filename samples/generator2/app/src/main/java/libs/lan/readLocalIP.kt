package libs.lan

import android.content.Context
import android.net.wifi.WifiManager
import android.os.StrictMode
import android.text.format.Formatter
import timber.log.Timber
import java.io.IOException

/**
 * Получить IP адрес Wifi подключения
 *
 * @param context
 *
 * @return Строка "192.168.0.100" иначе "127.0.0.1" если не получилось получить адрес
 */
fun readLocalIP(context: Context): String {
    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
    StrictMode.setThreadPolicy(policy)
    try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress: String = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        Timber.i("IP: $ipAddress")
        return ipAddress
    } catch (e: IOException) {
        Timber.e("IOException: " + e.message)
    }
    return "127.0.0.1"
}