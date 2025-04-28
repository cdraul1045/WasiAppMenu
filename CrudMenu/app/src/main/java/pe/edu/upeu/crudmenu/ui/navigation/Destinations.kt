package pe.edu.upeu.crudmenu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destinations(
    val route: String,
    val title: String,
    val icon: ImageVector) {
    object Login:Destinations("login", "Login", Icons.Filled.Settings)
    object Pantalla1 : Destinations( "pantalla1", "Pantalla 1", Icons.Filled.Home )
    object Pantalla2 : Destinations("pantalla2/?newText={newText}","Pantalla 2", Icons.Filled.Settings) {
            fun createRoute(newText: String) ="pantalla2/?newText=$newText"
    }
    object Pantalla3 : Destinations("pantalla3", "Pantalla 3", Icons.Filled.Favorite)
    object Pantalla4 : Destinations("pantalla4", "Pantalla 4x", Icons.Filled.Face )
    object Pantalla5 : Destinations("pantalla5", "Pantalla 5x", Icons.Filled.AccountCircle )

    object ProductoMainSC: Destinations("productomain","Adm. Productos", Icons.Filled.DateRange)
        object ProductoFormSC:
            Destinations("productoForm?prodId={prodId}", "Form Producto",
                Icons.Filled.Add){
            fun passId(prodId:String?):String{
                return "productoForm?prodId=$prodId"
            }
        }

    object MenuMainSC : Destinations("menuMain", "Adm. Menú", Icons.Filled.DateRange)

        object MenuFormSC :
            Destinations("menuForm?menuId={menuId}", "Form Menú",
                Icons.Filled.Add) {
        fun passId(menuId: String?): String {
            return "menuForm?menuId=$menuId"
        }
    }object MenuFormNewSC : Destinations(
        route = "menuForm",
        title = "Crear Menú",
        icon  = Icons.Filled.Add
    )



}