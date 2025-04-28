package pe.edu.upeu.crudmenu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import pe.edu.upeu.crudmenu.data.local.dao.MarcaDao
import pe.edu.upeu.crudmenu.modelo.Marca

@Database(entities = [Marca::class, ], version = 1)
abstract class DbDataSource: RoomDatabase() {
    abstract fun marcaDao():MarcaDao
}