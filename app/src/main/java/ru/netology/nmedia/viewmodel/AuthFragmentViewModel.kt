package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.AuthApi
import ru.netology.nmedia.auth.AppAuth

class AuthFragmentViewModel : ViewModel() {

    // Приватный MutableLiveData для изменения состояния внутри ViewModel
    private val _loginState = MutableLiveData<LoginState>()

    // Публичный LiveData для наблюдения из Fragment
    val loginState: LiveData<LoginState> = _loginState

    /**
     * Метод авторизации
     * Вызывается из Fragment при нажатии на кнопку "Войти"
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                val response = AuthApi.service.authenticate(username, password)

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!

                    // Сохраняем токен и ID в хранилище
                    AppAuth.getInstance().setAuth(authData.id, authData.token)

                    _loginState.value = LoginState.Success
                } else {
                    // Обработка ошибок от сервера
                    _loginState.value = LoginState.Error(
                        response.message() ?: "Ошибка авторизации"
                    )
                }
            } catch (e: Exception) {
                //  Обработка ошибки
                _loginState.value = LoginState.Error(e.message ?: "Неизвестная ошибка")
            }
        }

        fun resetState() {
            _loginState.value = LoginState.Idle
        }
    }

}

sealed class LoginState {
    object Idle : LoginState()              // Начальное состояние
    object Loading : LoginState()           // Идёт запрос
    object Success : LoginState()           // Успех
    data class Error(val message: String) : LoginState()  // Ошибка с сообщением
}