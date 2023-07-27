package com.anyandroid.easy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anyandroid.easy.data.User
import com.anyandroid.easy.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val firebaseAuth: FirebaseAuth) :
    ViewModel() {
    private val _register = MutableStateFlow<Resource<FirebaseUser>>(Resource.Unspecified())
    val register: Flow<Resource<FirebaseUser>> = _register
    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()


    fun createAccountWithNumberPhone2(user: User, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(
                    user.email,
                    password
                ).await()
                withContext(Dispatchers.Main) {
                    authResult.user?.let { _register.value = Resource.Success(it) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _register.value = Resource.Error(e.message.toString())
                }
            }
        }
    }

    fun createAccountWithEmailAndPassword(user: User, password: String,firstname:String,lastname:String) {
        if (checkValidation(user, password,firstname,lastname)) {
            runBlocking {
                _register.emit(Resource.Loading())
            }
            firebaseAuth.createUserWithEmailAndPassword(user.email, password)
                .addOnSuccessListener { it ->
                    it.user?.let {
                        _register.value = Resource.Success(it)
                    }
                }.addOnFailureListener {
                    _register.value = Resource.Error(it.message.toString())
                }
        } else {
            val registerFieldState = RegisterFieldsState(
                validEmail(user.email),
                validPassword(password),
                validFirstName(firstname),
                validLastName(lastname)
            )
            viewModelScope.launch {
                _validation.send(registerFieldState)
            }
        }
    }

    private fun checkValidation(user: User, password: String,firstname: String,lastname: String): Boolean {
        val emailValidation = validEmail(user.email)
        val passwordValidation = validPassword(password)
        val firstNameValidation = validFirstName(firstname)
        val lastnameValidation = validLastName(lastname)
        val shouldRegister =
            emailValidation is RegisterValidation.Success && passwordValidation is RegisterValidation.Success
        return shouldRegister
    }

}


