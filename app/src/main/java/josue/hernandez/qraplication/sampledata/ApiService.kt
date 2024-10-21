package josue.hernandez.qraplication.sampledata

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Modelo de datos para los asistentes
data class Asistente(
    val id: Int,
    val nombre: String,
    val email: String,
    val presente: Boolean
)

data class ValidacionAsistenciaResponse(
    val detail: String
)



// Interface para definir los endpoints de la API
interface ApiService {
    // Validar asistencia del asistente con el ID escaneado desde el QR
    @GET("asistentes/validar/{id}")
    suspend fun validarAsistencia(@Path("id") id: Int): ValidacionAsistenciaResponse


    // Obtener la lista completa de asistentes
    @GET("api/asistentes/")
    suspend fun obtenerAsistentes(): List<Asistente>
}

// Modelo para la respuesta de validación
data class ValidacionResponse(
    val detail: String // Recibimos el mensaje de detalle de la validación
)

// Cliente Retrofit para la conexión con la API
object RetrofitClient {
    // Asegúrate de usar http://10.0.2.2:8003/ para comunicarte con localhost desde un emulador http://192.168.0.7:8003
    private const val BASE_URL = "http://192.168.0.7:8003"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}


