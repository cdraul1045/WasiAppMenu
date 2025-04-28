package pe.edu.upeu.crudmenu.data.remote


import pe.edu.upeu.crudmenu.modelo.UsuarioDto
import pe.edu.upeu.crudmenu.modelo.UsuarioResp
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RestUsuario {

    @POST("/users/login")
    suspend fun login(@Body user:UsuarioDto):Response<UsuarioResp>
}