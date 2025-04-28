package pe.edu.upeu.crudmenu.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.crudmenu.data.local.dao.MarcaDao
import pe.edu.upeu.crudmenu.data.remote.RestMarca
import pe.edu.upeu.crudmenu.modelo.Marca
import pe.edu.upeu.crudmenu.utils.TokenUtils
import pe.edu.upeu.crudmenu.utils.isNetworkAvailable
import javax.inject.Inject

interface MarcaRepository {
    suspend fun findAll(): List<Marca>
    suspend fun findAllR(): Flow<List<Marca>>
}
class MarcaRepositoryImp @Inject constructor(
    private val rest: RestMarca,
    private val dao: MarcaDao,
): MarcaRepository{
    override suspend fun findAll(): List<Marca> {
        val response =rest.reportarMarcas(TokenUtils.TOKEN_CONTENT)
        return if (response.isSuccessful) response.body() ?:emptyList() else emptyList()
    }

    override suspend fun findAllR(): Flow<List<Marca>> {
        try {
            CoroutineScope(Dispatchers.IO).launch{
             if(isNetworkAvailable(TokenUtils.CONTEXTO_APPX)){
                 val data=rest.reportarMarcas(TokenUtils.TOKEN_CONTENT).body()!!
                 dao.insertAll(data)
                }
            }
        }catch (e:Exception){
            Log.e("ERROR", "Error: ${e.message}")
        }
        return dao.getAll()
    }

}