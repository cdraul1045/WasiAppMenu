package pe.edu.upeu.crudmenu.data.remote
import pe.edu.upeu.crudmenu.modelo.Categoria
import pe.edu.upeu.crudmenu.modelo.Marca
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
interface RestMarca{
    companion object {
        const val BASE_RUTA = "/marcas"
    }
    @GET("${BASE_RUTA}")
    suspend fun reportarMarcas(@Header("Authorization")
                               token:String): Response<List<Marca>>
}