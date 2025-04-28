package pe.edu.upeu.crudmenu.ui.presentation.screens.producto

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import pe.edu.upeu.crudmenu.modelo.ProductoDto
import pe.edu.upeu.crudmenu.modelo.ProductoResp
import pe.edu.upeu.crudmenu.repository.ProductoRepository
import javax.inject.Inject

@HiltViewModel
class ProductoMainViewModel  @Inject constructor(
    private val prodRepo: ProductoRepository,
): ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _deleteSuccess = MutableStateFlow<Boolean?>(null)
    val deleteSuccess: StateFlow<Boolean?> get() = _deleteSuccess

    private val _prods = MutableStateFlow<List<ProductoResp>>(emptyList())
    val prods: StateFlow<List<ProductoResp>> = _prods

    /*init {
        cargarProductos()
    }*/

    fun cargarProductos() {
        viewModelScope.launch {
            _isLoading.value = true
            _prods.value = prodRepo.reportarProductos()
            _isLoading.value = false
        }
    }

    fun buscarPorId(id: Long): Flow<ProductoResp> = flow {
        emit(prodRepo.buscarProductoId(id))
    }

    fun eliminar(producto: ProductoDto) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val success = prodRepo.deleteProducto(producto)
            if (success) {
                //eliminarProductoDeLista(producto.idProducto)
                cargarProductos()
                _deleteSuccess.value = success
                _isLoading.value = false
            }else{ _deleteSuccess.value = false }
        } catch (e: Exception) {
            Log.e("ProductoVM", "Error al eliminar producto", e)
            _deleteSuccess.value = false
        }
    }

    fun clearDeleteResult() {
        _deleteSuccess.value = null
    }
    /*fun eliminarProductoDeLista(id: Long) {
        _prods.value = _prods.value.filterNot { it.idProducto == id }
    }*/
}