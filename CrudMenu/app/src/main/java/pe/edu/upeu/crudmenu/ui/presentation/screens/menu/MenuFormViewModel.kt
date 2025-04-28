package pe.edu.upeu.crudmenu.ui.presentation.screens.menu



import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.crudmenu.modelo.MenuDto
import pe.edu.upeu.crudmenu.modelo.MenuResp
import pe.edu.upeu.crudmenu.repository.MenuRepository
import javax.inject.Inject

@HiltViewModel
class MenuFormViewModel @Inject constructor(
    private val menuRepo: MenuRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    // Flujos de éxito para crear y editar
    private val _addSuccess = MutableStateFlow(false)
    val addSuccess: StateFlow<Boolean> get() = _addSuccess

    private val _editSuccess = MutableStateFlow(false)
    val editSuccess: StateFlow<Boolean> get() = _editSuccess

    // Convertimos menúId de String a Long? y solo si >0
    private val menuId: Long? = savedStateHandle
        .get<String>("menuId")
        ?.toLongOrNull()
        ?.takeIf { it > 0L }

    private val _menu = MutableStateFlow<MenuResp?>(null)
    val menu: StateFlow<MenuResp?> get() = _menu

    init {
        menuId?.let { loadMenu(it) }
    }

    fun loadMenu(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        val fetched = runCatching { menuRepo.buscarMenuId(id) }.getOrNull()
        _menu.value = fetched
        _isLoading.value = false
    }

    fun addMenu(dto: MenuDto) = viewModelScope.launch {
        _isLoading.value = true
        menuRepo.insertarMenu(dto)
        _isLoading.value = false
        _addSuccess.value = true
    }

    fun editMenu(dto: MenuDto) = viewModelScope.launch {
        _isLoading.value = true
        menuRepo.modificarMenu(dto)
        _isLoading.value = false
        _editSuccess.value = true
    }
}
