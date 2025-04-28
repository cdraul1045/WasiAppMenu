package pe.edu.upeu.crudmenu.ui.presentation.screens.menu

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.github.k0shk0sh.compose.easyforms.BuildEasyForms
import com.github.k0shk0sh.compose.easyforms.EasyFormsResult
import com.google.gson.Gson
import pe.edu.upeu.crudmenu.modelo.MenuDto
import pe.edu.upeu.crudmenu.modelo.toDto
import pe.edu.upeu.crudmenu.ui.navigation.Destinations
import pe.edu.upeu.crudmenu.ui.presentation.components.form.AccionButtonCancel
import pe.edu.upeu.crudmenu.ui.presentation.components.form.AccionButtonSuccess
import pe.edu.upeu.crudmenu.ui.presentation.components.form.MyFormKeys
import pe.edu.upeu.crudmenu.ui.presentation.components.form.NameTextField

@Composable
fun MenuForm(
    text: String,
    navController: NavHostController,
    viewModel: MenuFormViewModel = hiltViewModel(),
    darkMode: MutableState<Boolean>
) {
    val menuState by viewModel.menu.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val addSuccess by viewModel.addSuccess.collectAsState()
    val editSuccess by viewModel.editSuccess.collectAsState()

    // DTO inicial
    val isEdit = text != "0"
    var menuD by remember(text) { mutableStateOf(
        if (isEdit) Gson().fromJson(text, MenuDto::class.java)
        else MenuDto(0, "", "", 0.0)
    ) }

    // Carga en modo edición
    LaunchedEffect(text) {
        if (isEdit) viewModel.loadMenu(menuD.idMenu)
    }
    menuState?.let { if (isEdit) menuD = it.toDto() }

    // Navegar tras crear o editar exitosamente
    LaunchedEffect(addSuccess, editSuccess) {
        if (addSuccess || editSuccess) {
            navController.navigate(Destinations.MenuMainSC.route)
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        MenuFormBody(
            id = menuD.idMenu,
            navController = navController,
            menu = menuD,
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MenuFormBody(
    id: Long,
    navController: NavHostController,
    menu: MenuDto,
    viewModel: MenuFormViewModel
) {
    val temp = remember { MenuDto(0, "", "", 0.0) }

    Scaffold(
        modifier = Modifier
            .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
    ) {
        BuildEasyForms { easyForm ->
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                NameTextField(
                    easyForms = easyForm,
                    text = menu.nombre,
                    label = "Nombre del Menú:",
                    key = MyFormKeys.NAME
                )
                Spacer(modifier = Modifier.height(8.dp))

                NameTextField(
                    easyForms = easyForm,
                    text = menu.descripcion,
                    label = "Descripción:",
                    key = MyFormKeys.DESCRIPTION
                )
                Spacer(modifier = Modifier.height(8.dp))

                NameTextField(
                    easyForms = easyForm,
                    text = menu.precio.toString(),
                    label = "Precio:",
                    key = MyFormKeys.PU
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AccionButtonSuccess(
                        easyForms = easyForm,
                        id = id,
                        label = "Guardar"
                    ) {
                        val data = easyForm.formData()
                        temp.nombre      = (data[0] as EasyFormsResult.StringResult).value
                        temp.descripcion = (data[1] as EasyFormsResult.StringResult).value
                        temp.precio      = (data[2] as EasyFormsResult.StringResult).value.toDouble()

                        if (id == 0L) viewModel.addMenu(temp) else {
                            temp.idMenu = id
                            viewModel.editMenu(temp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    AccionButtonCancel(
                        easyForms = easyForm,
                        label = "Cancelar"
                    ) {
                        navController.navigate(Destinations.MenuMainSC.route)
                    }
                }
            }
        }
    }
}
