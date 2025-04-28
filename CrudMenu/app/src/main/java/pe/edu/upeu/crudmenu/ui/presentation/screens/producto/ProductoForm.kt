package pe.edu.upeu.crudmenu.ui.presentation.screens.producto

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.github.k0shk0sh.compose.easyforms.BuildEasyForms
import com.github.k0shk0sh.compose.easyforms.EasyFormsResult
import com.google.gson.Gson
import pe.edu.upeu.crudmenu.modelo.Categoria
import pe.edu.upeu.crudmenu.modelo.ComboModel
import pe.edu.upeu.crudmenu.modelo.Marca
import pe.edu.upeu.crudmenu.modelo.ProductoDto
import pe.edu.upeu.crudmenu.modelo.UnidadMedida
import pe.edu.upeu.crudmenu.modelo.toDto
import pe.edu.upeu.crudmenu.ui.navigation.Destinations
import pe.edu.upeu.crudmenu.ui.presentation.components.Spacer
import pe.edu.upeu.crudmenu.ui.presentation.components.form.AccionButtonCancel
import pe.edu.upeu.crudmenu.ui.presentation.components.form.AccionButtonSuccess
import pe.edu.upeu.crudmenu.ui.presentation.components.form.ComboBox
import pe.edu.upeu.crudmenu.ui.presentation.components.form.ComboBoxThre
import pe.edu.upeu.crudmenu.ui.presentation.components.form.ComboBoxTwo
import pe.edu.upeu.crudmenu.ui.presentation.components.form.MyFormKeys
import pe.edu.upeu.crudmenu.ui.presentation.components.form.NameTextField


@Composable
fun ProductoForm(
    text: String,
    darkMode: MutableState<Boolean>,
    navController: NavHostController,
    viewModel: ProductoFormViewModel= hiltViewModel()
) {
    val producto by viewModel.producto.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val marcas by viewModel.marcs.collectAsState()
    val categorias by viewModel.categors.collectAsState()
    val unidadesmedidas by viewModel.unidMeds.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getDatosPrevios()
    }

    var productoD: ProductoDto
    if (text!="0"){
        productoD = Gson().fromJson(text, ProductoDto::class.java)
        LaunchedEffect(Unit) {
            viewModel.getProducto(productoD.idProducto)
        }
        producto?.let {
            productoD=it.toDto()
            Log.i("DMPX","Producto: ${productoD.toString()}")
        }
    }else{
        productoD= ProductoDto(0,"",0.0,0.0,0.0,0.0,0.0,0,0,0)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    formulario(
        productoD.idProducto!!,
        darkMode,
        navController,
        productoD,
        viewModel,
        marcas,
        categorias,
        unidadesmedidas
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MissingPermission",
    "CoroutineCreationDuringComposition"
)
@Composable
fun formulario(id:Long,
               darkMode: MutableState<Boolean>,
               navController: NavHostController,
               producto: ProductoDto,
               viewModel: ProductoFormViewModel,
               listMarca: List<Marca>,
               listCategoria: List<Categoria>,
               listUnidMed: List<UnidadMedida>,
               ){
    val product= ProductoDto(0,"",0.0,0.0,0.0,0.0,0.0,0,0,0)
    Scaffold(modifier = Modifier.padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom =
        32.dp)){
        BuildEasyForms { easyForm ->
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                NameTextField(easyForms = easyForm, text=producto?.nombre!!,"Nomb. Producto:", MyFormKeys.NAME )
                val listM: List<ComboModel> = listMarca.map { marca ->
                    ComboModel(code = marca.idMarca.toString(), name = marca.nombre)
                }
                ComboBox(easyForm = easyForm, "Marca:", producto?.marca?.let { it.toString() } ?: "", listM)
                val listC: List<ComboModel> = listCategoria.map { categor ->
                    ComboModel(code = categor.idCategoria.toString(), name = categor.nombre)
                }
                ComboBoxTwo(easyForm = easyForm, "Categoría:", producto?.categoria?.let { it.toString() } ?: "", listC)
                val listUM: List<ComboModel> = listUnidMed.map { unitMed ->
                    ComboModel(code = unitMed.idUnidad.toString(), name = unitMed.nombreMedida)
                }

                ComboBoxThre(easyForm = easyForm, label = "Unidad Medida:", producto?.unidadMedida?.let { it.toString() } ?: "", list =listUM)

                NameTextField(easyForms = easyForm, text=producto?.pu.toString()!!,"P. Unitario:", MyFormKeys.PU )
                NameTextField(easyForms = easyForm, text=producto?.puOld.toString()!!,"P. Unit Antiguo:", MyFormKeys.PU_OLD )
                NameTextField(easyForms = easyForm, text=producto?.utilidad.toString()!!,"Utilidad:", MyFormKeys.UTILIDAD )
                NameTextField(easyForms = easyForm, text=producto?.stock.toString()!!,"Stock:", MyFormKeys.STOCK )
                NameTextField(easyForms = easyForm, text=producto?.stockOld.toString()!!,"Stock Antiguo:", MyFormKeys.STOCK_OLD )

                Row(Modifier.align(Alignment.CenterHorizontally)){
                    AccionButtonSuccess(easyForms = easyForm, "Guardar", id){
                        val lista=easyForm.formData()
                        product.nombre=(lista.get(0) as EasyFormsResult.StringResult).value
                        product.marca= (splitCadena((lista.get(1) as EasyFormsResult.GenericStateResult<String>).value)).toLong()
                        product.categoria= (splitCadena((lista.get(2) as EasyFormsResult.GenericStateResult<String>).value)).toLong()
                        product.unidadMedida= (splitCadena((lista.get(3) as EasyFormsResult.GenericStateResult<String>).value)).toLong()

                        product.pu=((lista.get(4) as EasyFormsResult.StringResult).value).toDouble()
                        product.puOld=((lista.get(5) as EasyFormsResult.StringResult).value).toDouble()
                        product.utilidad=((lista.get(6) as EasyFormsResult.StringResult).value).toDouble()
                        product.stock=((lista.get(7) as EasyFormsResult.StringResult).value).toDouble()
                        product.stockOld=((lista.get(8) as EasyFormsResult.StringResult).value).toDouble()

                        if (id==0.toLong()){
                            Log.i("AGREGAR", "M:"+ product.marca)
                            Log.i("AGREGAR", "VI:"+ product.stock)
                            viewModel.addProducto(product)
                        }else{
                            product.idProducto=id
                            Log.i("MODIFICAR", "M:"+product)
                            viewModel.editProducto(product)
                        }
                        navController.navigate(Destinations.ProductoMainSC.route)
                    }
                    Spacer()
                    AccionButtonCancel(easyForms = easyForm, "Cancelar"){
                        navController.navigate(Destinations.ProductoMainSC.route)
                    }
                }
            }
        }
    }

}

fun splitCadena(data:String):String{
    return if(data!="") data.split("-")[0] else ""
}