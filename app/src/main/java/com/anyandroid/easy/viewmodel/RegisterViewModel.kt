package com.anyandroid.easy.viewmodel

import androidx.lifecycle.ViewModel
import com.anyandroid.easy.data.User
import com.anyandroid.easy.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val firebaseAuth: FirebaseAuth) :
    ViewModel() {
    private val _register = MutableStateFlow<Resource<FirebaseUser>>(Resource.Unspecified())
    val register: Flow<Resource<FirebaseUser>> = _register


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

    fun createAccountWithEmailAndPassword(user: User, password: String) {
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
    }

}


