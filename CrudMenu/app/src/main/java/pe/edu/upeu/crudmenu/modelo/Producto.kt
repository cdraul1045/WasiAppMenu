package pe.edu.upeu.crudmenu.modelo

data class ProductoDto (
    var idProducto: Long,
    var nombre: String,
    var pu: Double,
    var puOld: Double,
    var utilidad: Double,
    var stock: Double,
    var stockOld: Double,
    var categoria: Long,
    var marca: Long,
    var unidadMedida: Long
)
data class ProductoResp(
    val idProducto: Long,
    val nombre: String,
    val pu: Double,
    val puOld: Double,
    val utilidad: Double,
    val stock: Double,
    val stockOld: Double,
    val categoria: Categoria,
    val marca: Marca,
    val unidadMedida: UnidadMedida
)
fun ProductoResp.toDto(): ProductoDto {
    return ProductoDto(
        idProducto = this.idProducto,
        nombre = this.nombre,
        pu = this.pu,
        puOld = this.puOld,
        utilidad = this.utilidad,
        stock = this.stock,
        stockOld = this.stockOld,
        categoria = this.categoria.idCategoria,
        marca = this.marca.idMarca,
        unidadMedida = this.unidadMedida.idUnidad
    )
}