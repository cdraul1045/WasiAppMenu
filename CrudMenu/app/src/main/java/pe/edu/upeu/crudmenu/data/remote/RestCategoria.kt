package pe.edu.upeu.crudmenu.data.remote

import pe.edu.upeu.crudmenu.modelo.Categoria
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface RestCategoria {
    companion object {
        const val BASE_RUTA = "/categorias"
    }
    @GET("${BASE_RUTA}")
    suspend fun reportarCategorias(@Header("Authorization")
                                   token:String): Response<List<Categoria>>
}