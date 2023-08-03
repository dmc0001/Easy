package com.anyandroid.easy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anyandroid.easy.data.User
import com.anyandroid.easy.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val firebaseAuth: FirebaseAuth) :
    ViewModel() {
    private val _register = MutableStateFlow<Resource<FirebaseUser>>(Resource.Unspecified())
    val register: Flow<Resource<FirebaseUser>> = _register
    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()


    fun createAccountWithEmailAndPassword(
        user: User,
        password: String
    ) {
        if (checkValidation(user, password)) {
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
                validFirstName(user.firstName),
                validLastName(user.lastName),
                validPhoneNumber(user.phoneNumber)
            )
            viewModelScope.launch {
                _validation.send(registerFieldState)
            }
        }
    }
    private fun checkValidation(
        user: User,
        password: String,
    ): Boolean {
        val emailValidation = validEmail(user.email)
        val passwordValidation = validPassword(password)
        val firstNameValidation = validFirstName(user.firstName)
        val lastnameValidation = validLastName(user.lastName)
        val phoneNumberValidation = validPhoneNumber(user.phoneNumber)
        return (emailValidation is RegisterValidation.Success
                && passwordValidation is RegisterValidation.Success
                && lastnameValidation is RegisterValidation.Success
                && firstNameValidation is RegisterValidation.Success
                && phoneNumberValidation is RegisterValidation.Success)
    }

}


