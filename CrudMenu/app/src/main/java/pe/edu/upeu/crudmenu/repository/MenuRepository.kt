package pe.edu.upeu.crudmenu.repository


import pe.edu.upeu.crudmenu.data.remote.RestMenu
import pe.edu.upeu.crudmenu.modelo.MenuDto
import pe.edu.upeu.crudmenu.modelo.MenuResp
import pe.edu.upeu.crudmenu.utils.TokenUtils
import javax.inject.Inject

interface MenuRepository {
    suspend fun deleteMenu(menu: MenuDto): Boolean
    suspend fun reportarMenus(): List<MenuResp> // Cambiado
    suspend fun buscarMenuId(id: Long): MenuResp // Cambiado
    suspend fun insertarMenu(menu: MenuDto): Boolean
    suspend fun modificarMenu(menu: MenuDto): Boolean
}

class MenuRepositoryImp @Inject constructor(
    private val restMenu: RestMenu
): MenuRepository {

    override suspend fun deleteMenu(menu: MenuDto): Boolean {
        val response =
            restMenu.deleteMenu(TokenUtils.TOKEN_CONTENT, menu.idMenu) // Aquí asumo que idProducto es el ID en tu modelo MenuDto
        return response.isSuccessful && response.body()?.message == "true"
    }

    override suspend fun reportarMenus(): List<MenuResp> {
        val response =
            restMenu.reportarMenu(TokenUtils.TOKEN_CONTENT)
        return if (response.isSuccessful) response.body() ?: emptyList()
        else emptyList()
    }

    override suspend fun buscarMenuId(id: Long): MenuResp {
        val response =
            restMenu.getMenuById(TokenUtils.TOKEN_CONTENT, id)
        return response.body() ?: throw Exception("Menu no encontrado")
    }

    override suspend fun insertarMenu(menu: MenuDto): Boolean {
        val response =
            restMenu.insertarMenu(TokenUtils.TOKEN_CONTENT, menu)
        return response.isSuccessful && response.body()?.message == "true"
    }

    override suspend fun modificarMenu(menu: MenuDto): Boolean {
        val response =
            restMenu.actualizarMenu(TokenUtils.TOKEN_CONTENT, menu.idMenu, menu) // Asegúrate de que el idProducto está correcto
        return response.isSuccessful && response.body()?.idMenu != null
    }
}
