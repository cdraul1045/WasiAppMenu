package pe.edu.upeu.crudmenu.modelo

data class MenuDto(
    var idMenu: Long,
    var nombre: String,
    var descripcion: String,
    var precio: Double
)

data class MenuResp(
    val idMenu: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Double
)

fun MenuResp.toDto(): MenuDto {
    return MenuDto(
        idMenu    = this.idMenu,
        nombre    = this.nombre,
        descripcion = this.descripcion,
        precio    = this.precio
    )
}