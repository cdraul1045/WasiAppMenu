package pe.edu.upeu.crudmenu.ui.presentation.screens.menu

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.gson.Gson
import pe.edu.upeu.crudmenu.modelo.MenuDto
import pe.edu.upeu.crudmenu.modelo.MenuResp
import pe.edu.upeu.crudmenu.modelo.toDto
import pe.edu.upeu.crudmenu.ui.navigation.Destinations
import pe.edu.upeu.crudmenu.ui.presentation.components.*
import pe.edu.upeu.crudmenu.utils.TokenUtils

@Composable
fun MenuMain(
    navegarEditarMenu: (String) -> Unit,
    viewModel: MenuMainViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val menus by viewModel.menus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarMenus()
    }

    // Mostrar Toast tras eliminación
    deleteSuccess?.let { success ->
        val msg = if (success) "Eliminado correctamente" else "Error al eliminar"
        Toast.makeText(LocalContext.current, msg, Toast.LENGTH_SHORT).show()
        viewModel.clearDeleteResult()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        MenuGestion(
            navController = navController,
            onAddClick = { navegarEditarMenu("0") },
            onDeleteClick = { viewModel.eliminar(it.toDto()) },
            menus = menus,
            isLoading = isLoading,
            onEditClick = { navegarEditarMenu(Gson().toJson(it.toDto())) }
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuGestion(
    navController: NavHostController,
    onAddClick: (() -> Unit)? = null,
    onDeleteClick: ((toDelete: MenuResp) -> Unit)? = null,
    menus: List<MenuResp>,
    isLoading: Boolean,
    onEditClick: ((toEdit: MenuResp) -> Unit)? = null,
    viewModel: MenuMainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val searchQuery = remember { mutableStateOf("") }
    var filteredMenus by remember { mutableStateOf(menus) }

    // filtrar cuando cambia searchQuery o menus
    LaunchedEffect(searchQuery.value, menus) {
        filteredMenus = menus.filter {
            it.nombre.contains(searchQuery.value, ignoreCase = true) ||
                    it.descripcion.contains(searchQuery.value, ignoreCase = true)
        }
    }

    val fabItems = listOf(
        FabItem(Icons.Filled.Add, "Añadir Menú") { onAddClick?.invoke() }
    )

    Scaffold(
        bottomBar = { BottomAppBar { BottomNavigationBar(emptyList(), navController) } },
        floatingActionButton = {
            MultiFloatingActionButton(
                navController = navController,
                fabIcon = Icons.Filled.Add,
                items = fabItems,
                showLabels = true
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                label = { Text("Buscar menú") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
            if (filteredMenus.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron menús", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
                        .fillMaxSize()
                ) {
                    items(filteredMenus.size) { index ->
                        val menu = filteredMenus[index]
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = menu.nombre,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = menu.descripcion,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Precio: S/ ${menu.precio}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { onDeleteClick?.invoke(menu) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                }
                                IconButton(onClick = { onEditClick?.invoke(menu) }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                }
                            }
                        }
                    }
                    if (isLoading) {
                        item { LoadingCard() }
                    }
                }
            }
        }
    }
}
