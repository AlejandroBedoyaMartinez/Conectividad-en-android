package tareaEquipo.net.myapplication.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import tareaEquipo.net.myapplication.MY_UUID
import java.io.IOException

class AcceptThread(private val adapter: BluetoothAdapter, private val onSocketReady: (BluetoothSocket) -> Unit) : Thread() {
    private val serverSocket: BluetoothServerSocket? =
        adapter.listenUsingRfcommWithServiceRecord("BluetoothApp", MY_UUID)

    override fun run() {
        var socket: BluetoothSocket? = null
        while (true) {
            socket = try {
                serverSocket?.accept()
            } catch (e: IOException) {
                break
            }

            socket?.also {
                onSocketReady(it)
                serverSocket?.close()

            }
        }
    }
}
