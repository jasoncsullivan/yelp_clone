package com.jasoncsullivan.yelpclone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import java.io.IOException

// credit: https://medium.com/@dilipsuthar97/listen-to-internet-connection-using-broadcastreceiver-in-android-kotlin-6b527426a6f2
class ConnectivityReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(isConnectedOrConnecting(context!!))
        }
    }

    private fun isConnectedOrConnecting(context: Context): Boolean {
        return isOnline()
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
    }

}

public fun isOnline() : Boolean {
    val runtime = Runtime.getRuntime()
    try {
        val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
        val exitValue = ipProcess.waitFor()
        return exitValue == 0
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
    return false
}
