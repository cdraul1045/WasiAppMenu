package pe.edu.upeu.crudmenu.data.remote

import pe.edu.upeu.crudmenu.data.remote.RestProducto.Companion.BASE_PROD
import pe.edu.upeu.crudmenu.modelo.MenuDto
import pe.edu.upeu.crudmenu.modelo.MenuResp
import pe.edu.upeu.crudmenu.modelo.Message
import retrofit2.Response
import retrofit2.http.*

interface RestMenu {

    @GET("${BASE_MENU}")
    suspend fun reportarMenu(
        @Header("Authorization") token: String
    ): Response<List<MenuResp>>

    @GET("${BASE_MENU}/{id}")
    suspend fun getMenuById(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<MenuResp>

    @DELETE("${BASE_MENU}/{id}")
    suspend fun deleteMenu(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Message>

    @PUT("${BASE_MENU}/{id}")
    suspend fun actualizarMenu(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body menu: MenuDto
    ): Response<MenuResp>

    @POST("${BASE_MENU}")
    suspend fun insertarMenu(
        @Header("Authorization") token: String,
        @Body menu: MenuDto
    ): Response<Message>

    companion object {
        const val BASE_MENU = "/menus"
    }
}
