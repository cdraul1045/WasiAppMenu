package pe.edu.upeu.crudmenu.data.remote

import pe.edu.upeu.crudmenu.modelo.Message
import pe.edu.upeu.crudmenu.modelo.ProductoDto
import pe.edu.upeu.crudmenu.modelo.ProductoResp
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RestProducto {
    @GET("${BASE_PROD}")
    suspend fun reportarProducto(@Header("Authorization")
                                 token:String): Response<List<ProductoResp>>
    @GET("${BASE_PROD}/{id}")
    suspend fun getProductoId(@Header("Authorization")
                              token:String, @Path("id") id:Long): Response<ProductoResp>
    @DELETE("${BASE_PROD}/{id}")
    suspend fun deleteProducto(@Header("Authorization")
                               token:String, @Path("id") id:Long): Response<Message>
    @PUT("${BASE_PROD}/{id}")
    suspend fun actualizarProducto(@Header("Authorization")
                                   token:String, @Path("id") id:Long, @Body producto:
                                   ProductoDto): Response<ProductoResp>
    @POST("${BASE_PROD}")
    suspend fun insertarProducto(@Header("Authorization")
                                 token:String, @Body producto:ProductoDto): Response<Message>
    companion object {
        const val BASE_PROD = "/productos"
    }
}
