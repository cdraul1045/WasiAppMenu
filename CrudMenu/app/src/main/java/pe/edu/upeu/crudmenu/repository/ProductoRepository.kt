package pe.edu.upeu.crudmenu.repository

import pe.edu.upeu.crudmenu.data.remote.RestProducto
import pe.edu.upeu.crudmenu.modelo.ProductoDto
import pe.edu.upeu.crudmenu.modelo.ProductoResp
import pe.edu.upeu.crudmenu.utils.TokenUtils
import javax.inject.Inject

interface ProductoRepository {
    suspend fun deleteProducto(producto: ProductoDto): Boolean
    suspend fun reportarProductos(): List<ProductoResp> // Cambiado
    suspend fun buscarProductoId(id: Long): ProductoResp // Cambiado
    suspend fun insertarProducto(producto: ProductoDto): Boolean
    suspend fun modificarProducto(producto: ProductoDto): Boolean
}


class ProductoRepositoryImp @Inject constructor(
    private val restProducto: RestProducto,
//private val actividadDao: ActividadDao,
): ProductoRepository{
    override suspend fun deleteProducto(producto: ProductoDto): Boolean {
        val response =
            restProducto.deleteProducto(TokenUtils.TOKEN_CONTENT,
                producto.idProducto)
        return response.isSuccessful && response.body()?.message ==
                "true"
    }
    override suspend fun reportarProductos(): List<ProductoResp> {
        val response =
            restProducto.reportarProducto(TokenUtils.TOKEN_CONTENT)
        return if (response.isSuccessful) response.body() ?: emptyList()
        else emptyList()
    }
    override suspend fun buscarProductoId(id: Long): ProductoResp {
        val response =
            restProducto.getProductoId(TokenUtils.TOKEN_CONTENT, id)
        return response.body() ?: throw Exception("Producto no encontrado")
    }
    override suspend fun insertarProducto(producto: ProductoDto): Boolean
    {
        val response =
            restProducto.insertarProducto(TokenUtils.TOKEN_CONTENT, producto)
        return response.isSuccessful && response.body()?.message ==
                "true"
    }
    override suspend fun modificarProducto(producto: ProductoDto):
            Boolean {
        val response =
            restProducto.actualizarProducto(TokenUtils.TOKEN_CONTENT,
                producto.idProducto, producto)
        return response.isSuccessful && response.body()?.idProducto !=
                null
    }
}