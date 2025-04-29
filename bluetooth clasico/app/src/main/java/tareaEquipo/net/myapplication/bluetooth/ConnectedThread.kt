package tareaEquipo.net.myapplication.bluetooth

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.thread

class ConnectedThread(private val socket: BluetoothSocket) {
    private val input: InputStream = socket.inputStream
    private val output: OutputStream = socket.outputStream
    private val buffer = ByteArray(1024)
    private val handler = Handler(Looper.getMainLooper())

    fun startReading(onMessageReceived: (String) -> Unit) {
        thread {
            while (true) {
                try {
                    val bytes = input.read(buffer)
                    val message = String(buffer, 0, bytes)
                    handler.post { onMessageReceived(message) }
                } catch (e: IOException) {
                    break
                }
            }
        }
    }

    fun write(message: String) {
        try {
            output.write(message.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun cancel() {
        socket.close()
    }
}