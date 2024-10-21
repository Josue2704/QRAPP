package josue.hernandez.qraplication.vista

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanOptions
import josue.hernandez.qraplication.sampledata.Asistente
import josue.hernandez.qraplication.sampledata.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Función para validar la asistencia con manejo de errores
suspend fun validarAsistencia(id: Int, context: Context) {
    try {
        val response = withContext(Dispatchers.IO) {
            RetrofitClient.api.validarAsistencia(id)
        }
        Log.d("QR Scan", "Respuesta API: ${response.detail}")
        Toast.makeText(context, "Asistencia validada: ${response.detail}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Log.e("QR Scan", "Error: ${e.message}")
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}


// Composable principal para escanear el QR
@Composable
fun EscanearQrScreen() {
    val context = LocalContext.current
    val mensaje = remember { mutableStateOf("Escanea un QR") }
    val scope = rememberCoroutineScope()

    Log.d("QR Scan", "Mensaje recibido: ${mensaje.value}")

    // Configurar el launcher para iniciar la actividad de escaneo
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contents = result.data?.getStringExtra("SCAN_RESULT")
            contents?.let {
                mensaje.value = "Código escaneado: $it"
                if (it.isNullOrEmpty()) {
                    Log.e("QR Scan", "El código escaneado es nulo o vacío")
                    Toast.makeText(context, "El código QR es inválido", Toast.LENGTH_LONG).show()
                } else {
                    Log.i("QR Scan", "Código escaneado: $it")
                    try {
                        // Extraer el ID de la URL usando split
                        val urlParts = it.split("/")
                        val id = urlParts.last().toInt() // El último segmento es el ID

                        scope.launch {
                            validarAsistencia(id, context) // Llamada con el ID extraído
                        }
                    } catch (e: NumberFormatException) {
                        Log.e("QR Scan", "El QR no contiene un ID válido: $it", e)
                        Toast.makeText(context, "QR no contiene un ID válido", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            mensaje.value = "Escaneo cancelado"
        }
    }


    // Verifica y solicita permisos si es necesario
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            iniciarEscaneo(context, scope, mensaje, launcher)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                iniciarEscaneo(context, scope, mensaje, launcher)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }) {
            Text("Escanear QR")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = mensaje.value)
    }
}

@Composable
fun AsistentesScreen() {
    val scope = rememberCoroutineScope()
    var asistentes by remember { mutableStateOf<List<Asistente>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    // Cargar asistentes al inicio
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                asistentes = RetrofitClient.api.obtenerAsistentes()
            } catch (e: Exception) {
                error = "Error al obtener asistentes"
            }
        }
    }

    if (error != null) {
        Text(text = error ?: "Error desconocido", modifier = Modifier.padding(16.dp))
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(asistentes) { asistente ->
                AsistenteItem(asistente)
            }
        }
    }
}

@Composable
fun AsistenteItem(asistente: Asistente) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: ${asistente.nombre}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Email: ${asistente.email}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (asistente.presente) "Presente" else "Ausente",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun iniciarEscaneo(
    context: Context,
    scope: CoroutineScope,
    mensaje: MutableState<String>,
    launcher: ActivityResultLauncher<Intent>
) {
    val options = ScanOptions().apply {
        setPrompt("Escanea el código QR")
        setBeepEnabled(true)
        setOrientationLocked(true)
        captureActivity = com.journeyapps.barcodescanner.CaptureActivity::class.java
    }

    val intent = options.createScanIntent(context)
    launcher.launch(intent)
}


