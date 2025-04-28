package pe.edu.upeu.crudmenu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upeu.crudmenu.repository.CategoriaRepository
import pe.edu.upeu.crudmenu.repository.CategoriaRepositoryImp
import pe.edu.upeu.crudmenu.repository.MarcaRepository
import pe.edu.upeu.crudmenu.repository.MarcaRepositoryImp
import pe.edu.upeu.crudmenu.repository.MenuRepository
import pe.edu.upeu.crudmenu.repository.MenuRepositoryImp
import pe.edu.upeu.crudmenu.repository.ProductoRepository
import pe.edu.upeu.crudmenu.repository.ProductoRepositoryImp
import pe.edu.upeu.crudmenu.repository.UnidadMedidaRepository
import pe.edu.upeu.crudmenu.repository.UnidadMedidaRepositoryImp
import pe.edu.upeu.crudmenu.repository.UsuarioRepository
import pe.edu.upeu.crudmenu.repository.UsuarioRepositoryImp
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun userRepository(userRepos:UsuarioRepositoryImp):UsuarioRepository

    @Binds
    @Singleton
    abstract fun productoRepository(prodRepos:ProductoRepositoryImp): ProductoRepository
    @Binds
    @Singleton
    abstract fun marcaRepository(marcaRepos: MarcaRepositoryImp ): MarcaRepository
    @Binds
    @Singleton
    abstract fun categoriaRepository(categoriaRepos: CategoriaRepositoryImp): CategoriaRepository
    @Binds
    @Singleton
    abstract fun unidadMedRepository(unidmedRepos: UnidadMedidaRepositoryImp): UnidadMedidaRepository


    @Binds
    @Singleton
    abstract fun menuRepository(menuRepos: MenuRepositoryImp): MenuRepository
}