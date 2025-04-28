package pe.edu.upeu.crudmenu.ui.presentation.screens.producto

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.google.gson.Gson
import pe.edu.upeu.crudmenu.modelo.ProductoResp
import pe.edu.upeu.crudmenu.modelo.toDto
import pe.edu.upeu.crudmenu.ui.presentation.components.ConfirmDialog
import pe.edu.upeu.crudmenu.ui.presentation.components.LoadingCard
import pe.edu.upeu.crudmenu.ui.presentation.components.Spacer
import pe.edu.upeu.crudmenu.utils.TokenUtils
import pe.edu.upeu.crudmenu.R
import pe.edu.upeu.crudmenu.ui.navigation.Destinations
import pe.edu.upeu.crudmenu.ui.presentation.components.BottomNavigationBar
import pe.edu.upeu.crudmenu.ui.presentation.components.FabItem
import pe.edu.upeu.crudmenu.ui.presentation.components.MultiFloatingActionButton

@Composable
fun ProductoMain(navegarEditarAct: (String) -> Unit,
                 viewModel: ProductoMainViewModel = hiltViewModel(),
                 navController: NavHostController) {
    val productos by viewModel.prods.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
    PoroductoGestion(navController, onAddClick = {
        //viewModel.addUser()
        navegarEditarAct((0).toString())
    }, onDeleteClick = {
        viewModel.eliminar(it.toDto())
    }, productos, isLoading,
    onEditClick = {
            val jsonString = Gson().toJson(it.toDto())
            navegarEditarAct(jsonString)
    }
    )


}


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PoroductoGestion( navController: NavHostController,
           onAddClick: (() -> Unit)? = null,
           onDeleteClick: ((toDelete: ProductoResp) -> Unit)? = null,
           productos: List<ProductoResp>,
           isLoading: Boolean,
           onEditClick: ((toPersona: ProductoResp) -> Unit)? = null,
           viewModel: ProductoMainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    val navigationItems2 = listOf(
        Destinations.ProductoMainSC,
        Destinations.Pantalla1,
        Destinations.Pantalla2,
        Destinations.Pantalla3
    )
    val searchQuery = remember { mutableStateOf("") }
    var productosFiltrados = remember { mutableStateOf<List<ProductoResp>>(emptyList()) }
    val fabItems = listOf(
        FabItem(
            Icons.Filled.ShoppingCart,
            "Shopping Cart"
        ) {
            val toast = Toast.makeText(context, "Hola Mundo", Toast.LENGTH_LONG)
            toast.view!!.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN)
            toast.show()
        },
        FabItem(
            Icons.Filled.Favorite,
            "Add Producto"
        ) { onAddClick?.invoke() })

    Scaffold(
        bottomBar = {
            BottomAppBar {
                BottomNavigationBar(navigationItems2, navController = navController)
            }
        },
        modifier = Modifier,
        floatingActionButton = {
            MultiFloatingActionButton(
                navController=navController,
                fabIcon = Icons.Filled.Add,
                items = fabItems,
                showLabels = true
            )
        },
        floatingActionButtonPosition = FabPosition.End,
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(top = 60.dp)){
               OutlinedTextField(
                   value = searchQuery.value,
                   onValueChange = { searchQuery.value = it },
                   label = { Text("Buscar producto") },
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(top = 16.dp, end = 22.dp, start = 22.dp),
                   singleLine = true
               )
            productosFiltrados.value = productos.filter {
                        it.nombre.contains(searchQuery.value, ignoreCase = true) ||
                        it.categoria.nombre.contains(searchQuery.value, ignoreCase = true) ||
                        it.marca.nombre.contains(searchQuery.value, ignoreCase = true)
            }
            LazyColumn(modifier = Modifier
                .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
                .align(alignment = Alignment.TopCenter),
                //.offset(x = (16).dp, y = (-32).dp),
                userScrollEnabled= true,
            ){
                var itemCount = productosFiltrados.value.size
                items(count = itemCount) { index ->
                    var auxIndex = index;
                    if (isLoading) {
                        if (auxIndex == 0)
                            return@items LoadingCard()
                        auxIndex--
                    }
                    val productox = productosFiltrados.value.get(index)
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        ),
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .fillMaxWidth(),
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Image(modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .clip(RoundedCornerShape(8.dp)),
                                painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(context)
                                        .data(productox.marca.nombre)
                                        .placeholder(R.drawable.bg)
                                        .error(R.drawable.bg)
                                        .build()
                                ),
                                contentDescription = null,
                                contentScale = ContentScale.FillHeight
                            )
                            Spacer()
                            Column(
                                Modifier.weight(1f),
                            ) {
                                Text("${productox.nombre} - ${productox.pu}",
                                    fontWeight = FontWeight.Bold)
                                Text("${productox.categoria.nombre} - ${productox.marca.nombre}", color =MaterialTheme.colorScheme.primary)
                            }
                            Spacer()
                            val showDialog = remember { mutableStateOf(false) }
                            IconButton(onClick = {
                                showDialog.value = true
                            }) {
                                Icon(Icons.Filled.Delete, "Remove", tint =
                                    MaterialTheme.colorScheme.primary)
                            }
                            if (showDialog.value){
                                ConfirmDialog(
                                    message = "Esta seguro de eliminar?",
                                    onConfirm = {
                                        onDeleteClick?.invoke(productox)
                                        showDialog.value=false

                                    },
                                    onDimins = {
                                        showDialog.value=false
                                    }
                                )
                            }
                            IconButton(onClick = {
                                Log.i("VERTOKEN", TokenUtils.TOKEN_CONTENT)
                                onEditClick?.invoke(productox)
                            }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Editar",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

            }
        }
    }
    deleteSuccess?.let { success ->
        if (success) {
            Toast.makeText(LocalContext.current, "Eliminado correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(LocalContext.current, "Error al eliminar", Toast.LENGTH_SHORT).show()
        }
        viewModel.clearDeleteResult()
    }
}