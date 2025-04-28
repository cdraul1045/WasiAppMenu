package pe.edu.upeu.crudmenu.di


import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import pe.edu.upeu.crudmenu.data.local.DbDataSource
import pe.edu.upeu.crudmenu.data.local.dao.MarcaDao
import pe.edu.upeu.crudmenu.data.remote.RestCategoria
import pe.edu.upeu.crudmenu.data.remote.RestMarca
import pe.edu.upeu.crudmenu.data.remote.RestMenu
import pe.edu.upeu.crudmenu.data.remote.RestProducto
import pe.edu.upeu.crudmenu.data.remote.RestUnidadMedida
import pe.edu.upeu.crudmenu.data.remote.RestUsuario
import pe.edu.upeu.crudmenu.utils.TokenUtils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataSourceModule {
    //Api REST
    var retrofit: Retrofit?=null

    @Singleton
    @Provides
    @Named("BaseUrl")
    fun provideBaseUrl()=TokenUtils.API_URL

    @Singleton
    @Provides
    fun provideRetrofit(@Named("BaseUrl") baseUrl:String): Retrofit {
        val okHttpClient= OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
        if (retrofit==null){
            retrofit= Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(baseUrl).build()
        }
        return retrofit!!
    }
    //DB Local
    @Singleton
    @Provides
    fun dbDataSource(@ApplicationContext context:Context):DbDataSource{
        return Room.databaseBuilder(context, DbDataSource::class.java,
            "menu_db").build()
    }
    @Singleton
    @Provides
    fun marcaDao(db:DbDataSource):MarcaDao{
        return db.marcaDao()
    }


    //Api Rest
    @Singleton
    @Provides
    fun restUsuario(retrofit: Retrofit): RestUsuario{
        return retrofit.create(RestUsuario::class.java)
    }

    @Singleton
    @Provides
    fun restProducto(retrofit: Retrofit): RestProducto{
        return retrofit.create(RestProducto::class.java)
    }
    @Singleton
    @Provides
    fun restMarca(retrofit: Retrofit): RestMarca{
        return retrofit.create(RestMarca::class.java)
    }
    @Singleton
    @Provides
    fun restCategoria(retrofit: Retrofit): RestCategoria{
        return retrofit.create(RestCategoria::class.java)
    }
    @Singleton
    @Provides
    fun restUnidadMedida(retrofit: Retrofit): RestUnidadMedida{
        return retrofit.create(RestUnidadMedida::class.java)
    }

    @Singleton
    @Provides
    fun restMenu(retrofit: Retrofit): RestMenu {
        return retrofit.create(RestMenu::class.java)
    }

}