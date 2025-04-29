package tareaEquipo.net.myapplication.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import tareaEquipo.net.myapplication.MY_UUID
import java.io.IOException

class ConnectThread(private val device: BluetoothDevice, private val onSocketReady: (BluetoothSocket) -> Unit) : Thread() {
    private val socket: BluetoothSocket? = device.createRfcommSocketToServiceRecord(MY_UUID)

    override fun run() {
        try {
            socket?.connect()
            socket?.let { onSocketReady(it) }
        } catch (e: IOException) {
            socket?.close()
        }
    }
}