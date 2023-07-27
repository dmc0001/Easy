package com.anyandroid.easy.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anyandroid.easy.data.User
import com.anyandroid.easy.databinding.FragmentRegisterBinding
import com.anyandroid.easy.util.RegisterValidation
import com.anyandroid.easy.util.Resource
import com.anyandroid.easy.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private const val TAG = "RegisterFragment"

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModelRegister by viewModels<RegisterViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnRegister.setOnClickListener {
                val firstName = etFirstName.text.toString().trim()
                val lastName = etLastName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val phoneNumber = etPhoneNumberRegister.text.toString().trim()
                val password = etPasswordRegister.text.toString()
                val user = User(firstName, lastName, email, phoneNumber)
                viewModelRegister.createAccountWithEmailAndPassword(
                    user,
                    password,
                    firstName,
                    lastName
                )

            }
        }
        lifecycleScope.launchWhenStarted {
            viewModelRegister.register.collect {
                when (it) {
                    is Resource.Loading -> {
                        binding.btnRegister.startAnimation()
                    }
                    is Resource.Success -> {
                        Log.d("test", it.data.toString())
                        binding.btnRegister.revertAnimation()
                    }
                    is Resource.Error -> {
                        Log.d(TAG, it.message.toString())
                        binding.btnRegister.revertAnimation()
                    }
                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModelRegister.validation.collect { validation ->
                if (validation.email is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.etEmail.apply {
                            requestFocus()
                            error = validation.email.message

                        }
                    }
                }
                if (validation.password is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.etPasswordRegister.apply {
                            requestFocus()
                            error = validation.password.message
                        }
                    }
                }
                if (validation.firstname is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.etFirstName.apply {
                            requestFocus()
                            error = validation.firstname.message
                        }
                    }
                }
                if (validation.lastname is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.etLastName.apply {
                            requestFocus()
                            error = validation.lastname.message
                        }
                    }
                }
            }
        }
    }
}

