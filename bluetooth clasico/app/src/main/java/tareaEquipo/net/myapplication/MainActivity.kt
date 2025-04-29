package tareaEquipo.net.myapplication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import tareaEquipo.net.myapplication.bluetooth.AcceptThread
import tareaEquipo.net.myapplication.bluetooth.ConnectThread
import tareaEquipo.net.myapplication.bluetooth.ConnectedThread
import tareaEquipo.net.myapplication.ui.theme.BluetoothClasicoTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permisos = arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        ActivityCompat.requestPermissions(this, permisos, 1)

        enableEdgeToEdge()
        setContent {
            BluetoothClasicoTheme {
                bluetooth()
            }
        }
    }
}

// UUID debe ser el mismo en ambos dispositivos
val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

@Composable
fun bluetooth() {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val context = LocalContext.current

    var isConnected by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    val dispositivos = remember { mutableStateListOf<BluetoothDevice>() }
    var showDevicesDialog by remember { mutableStateOf(false) }
    var mensajesRecibidos by remember { mutableStateOf("") }
    var connectedThread by remember { mutableStateOf<ConnectedThread?>(null) }

    LaunchedEffect(Unit) {
        if (tienePermiso(context, Manifest.permission.BLUETOOTH_CONNECT)) {
            AcceptThread(bluetoothAdapter!!) { socket ->
                connectedThread = ConnectedThread(socket).also {
                    it.startReading { msg -> mensajesRecibidos += "\n${msg}" }
                }
                isConnected = true
            }.start()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Conectar", Modifier.padding(top = 30.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            if (tienePermiso(context, Manifest.permission.BLUETOOTH_CONNECT)) {
                val emparejados = obtenerDispositivosEmparejados(context)
                dispositivos.clear()
                dispositivos.addAll(emparejados)
                showDevicesDialog = true
            } else {
                Toast.makeText(context, "Permisos no otorgados", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Buscar Dispositivo")
        }

        if (showDevicesDialog) {
            AlertDialog(
                onDismissRequest = { showDevicesDialog = false },
                title = { Text("Seleccionar Dispositivo") },
                text = {
                    LazyColumn {
                        items(dispositivos) { device ->
                            Button(
                                onClick = {
                                    ConnectThread(device) { socket ->
                                        connectedThread = ConnectedThread(socket).also {
                                            it.startReading { msg -> mensajesRecibidos += "\n${msg}" }
                                        }
                                        isConnected = true
                                    }.start()
                                    Toast.makeText(context, "Conectando a ${device.name}", Toast.LENGTH_SHORT).show()
                                    showDevicesDialog = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RectangleShape
                            ) {
                                Text("Conectar a ${device.name ?: "Sin nombre"}")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDevicesDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }

        if (isConnected) {
            Spacer(modifier = Modifier.height(24.dp))
            formulario(
                mensaje,
                onMensajeChange = { mensaje = it },
                onEnviarClick = {
                    connectedThread?.write(mensaje)
                    mensaje = ""
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recibido:")
            Text(text = mensajesRecibidos)
        }
    }
}

@Composable
fun formulario(
    mensaje: String,
    onMensajeChange: (String) -> Unit,
    onEnviarClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Mensaje")
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = mensaje,
            onValueChange = onMensajeChange,
            label = { Text("Escribe tu mensaje") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onEnviarClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar mensaje")
        }
    }
}


fun obtenerDispositivosEmparejados(context: Context): Set<BluetoothDevice> {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    return if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
        == PackageManager.PERMISSION_GRANTED
    ) {
        bluetoothAdapter?.bondedDevices ?: emptySet()
    } else {
        emptySet()
    }
}

fun tienePermiso(context: Context, permiso: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED
}