package pe.edu.upeu.crudmenu.modelo

data class Message(
    val statusCode: Long,
    val datetime: String,
    val message: String,
    val details: String
)
