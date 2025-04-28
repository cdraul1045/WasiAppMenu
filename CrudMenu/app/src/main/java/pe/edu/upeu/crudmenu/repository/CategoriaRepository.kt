package pe.edu.upeu.crudmenu.repository

import pe.edu.upeu.crudmenu.data.remote.RestCategoria
import pe.edu.upeu.crudmenu.modelo.Categoria
import pe.edu.upeu.crudmenu.utils.TokenUtils
import javax.inject.Inject

interface CategoriaRepository {
    suspend fun findAll(): List<Categoria>
}

class CategoriaRepositoryImp @Inject constructor(
    private val rest: RestCategoria,
): CategoriaRepository{
    override suspend fun findAll(): List<Categoria> {
        val response =rest.reportarCategorias(TokenUtils.TOKEN_CONTENT)
        return if (response.isSuccessful) response.body() ?:
        emptyList()
        else emptyList()
    }
}