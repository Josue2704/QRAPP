package josue.hernandez.qraplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import josue.hernandez.qraplication.vista.EscanearQrScreen
import josue.hernandez.qraplication.vista.AsistentesScreen
import josue.hernandez.qraplication.vista.ui.theme.QRAplicationTheme
import josue.hernandez.qraplication.vista.validarAsistencia

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRAplicationTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    EscanearQrScreen()  // Cargar la pantalla del registro

                    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
                        Log.e("QR Scan", "Excepción no controlada", throwable)
                    }
                }
            }
        }
    }
}
