package pe.edu.upeu.crudmenu.ui.presentation.screens.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.IOException
import pe.edu.upeu.crudmenu.modelo.UsuarioDto
import pe.edu.upeu.crudmenu.modelo.UsuarioResp
import pe.edu.upeu.crudmenu.repository.UsuarioRepository
import pe.edu.upeu.crudmenu.utils.TokenUtils
import java.net.SocketTimeoutException

import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepo: UsuarioRepository
) : ViewModel(){
    private val _isLoading: MutableLiveData<Boolean> by lazy {MutableLiveData<Boolean>(false)}
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _islogin: MutableLiveData<Boolean> by lazy {MutableLiveData<Boolean>(false)}
    val islogin: LiveData<Boolean> get() = _islogin

    val isError=MutableLiveData<Boolean>(false)
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    val listUser = MutableLiveData<UsuarioResp>()

    fun loginSys(toData: UsuarioDto) {
        Log.i("LOGIN", toData.user)
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
        try {
            _islogin.postValue(false)
            val totek=userRepo.loginUsuario(toData).body()
            delay(1500L)
            TokenUtils.TOKEN_CONTENT="Bearer "+totek?.token
            Log.i("DATAXDMP", "Holas")
            listUser.postValue(totek!!)
            Log.i("DATAXDMP", TokenUtils.TOKEN_CONTENT)
            if(TokenUtils.TOKEN_CONTENT!="Bearer null"){
                TokenUtils.USER_LOGIN=toData.user
                _islogin.postValue(true)
            }else{
                isError.postValue(true)
                _errorMessage.postValue("Error de login: verifique sus credenciales")
            }
            _isLoading.postValue(false)
        }   catch (e: SocketTimeoutException){
            isError.postValue(true)
            _errorMessage.postValue("No se pudo conectar al servidor. Verifica tu red.")
        }catch (e: IOException) {
            isError.postValue(true)
            _errorMessage.postValue("Error de red: ${e.localizedMessage}")
        } catch (e: Exception) {
            isError.postValue(true)
            _errorMessage.postValue("Ocurrió un error inesperado.")
        }

        }
    }

    fun clearErrorMessage() {
        _errorMessage.postValue(null)
        isError.postValue(false)
        _isLoading.postValue(false)
    }
}