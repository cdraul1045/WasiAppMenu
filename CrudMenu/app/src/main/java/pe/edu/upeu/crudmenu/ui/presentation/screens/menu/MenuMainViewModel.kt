package pe.edu.upeu.crudmenu.ui.presentation.screens.menu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import pe.edu.upeu.crudmenu.modelo.MenuDto
import pe.edu.upeu.crudmenu.modelo.MenuResp
import pe.edu.upeu.crudmenu.repository.MenuRepository
import javax.inject.Inject

@HiltViewModel
class MenuMainViewModel @Inject constructor(
    private val menuRepo: MenuRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _deleteSuccess = MutableStateFlow<Boolean?>(null)
    val deleteSuccess: StateFlow<Boolean?> get() = _deleteSuccess

    private val _menus = MutableStateFlow<List<MenuResp>>(emptyList())
    val menus: StateFlow<List<MenuResp>> get() = _menus

    /**
     * Carga la lista completa de menús desde el repositorio
     */
    fun cargarMenus() {
        viewModelScope.launch {
            _isLoading.value = true
            _menus.value = menuRepo.reportarMenus()
            _isLoading.value = false
        }
    }

    /**
     * Busca un menú por su ID
     */
    fun buscarPorId(id: Long): Flow<MenuResp> = flow {
        emit(menuRepo.buscarMenuId(id))
    }

    /**
     * Elimina un menú y recarga la lista
     */
    fun eliminar(menu: MenuDto) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val success = menuRepo.deleteMenu(menu)
            if (success) {
                cargarMenus()
                _deleteSuccess.value = true
            } else {
                _deleteSuccess.value = false
            }
        } catch (e: Exception) {
            Log.e("MenuMainVM", "Error al eliminar menú", e)
            _deleteSuccess.value = false
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Limpia el estado de la operación de borrado para poder volver a usarlo
     */
    fun clearDeleteResult() {
        _deleteSuccess.value = null
    }
}