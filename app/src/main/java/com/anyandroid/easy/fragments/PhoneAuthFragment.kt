package com.anyandroid.easy.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anyandroid.easy.data.OTP
import com.anyandroid.easy.databinding.FragmentPhoneAuthBinding
import com.anyandroid.easy.util.Resource
import com.anyandroid.easy.viewmodel.PhoneAuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneAuthFragment : Fragment() {

    private lateinit var binding: FragmentPhoneAuthBinding
    private val viewModelPhoneAuth by viewModels<PhoneAuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPhoneAuthBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editTextInput()
        binding.apply {
            btnVerify.setOnClickListener {
                val c1 = etC1.text.toString().trim()
                val c2 = etC2.text.toString().trim()
                val c3 = etC3.text.toString().trim()
                val c4 = etC4.text.toString().trim()
                val c5 = etC5.text.toString().trim()
                val c6 = etC6.text.toString().trim()
                val otp =OTP(c1,c2,c3,c4,c5,c6)

                val otpCode = "$c1$c2$c3$c4$c5$c6"
                    if (otpCode.isNotEmpty()) {
                        viewModelPhoneAuth.signInWithVerificationCode(viewModelPhoneAuth.verificationId.toString(), otp)
                    }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModelPhoneAuth.verificationInProgress.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading progress (optional)
                        binding.btnVerify.startAnimation()
                    }
                    is Resource.Success -> {
                        // Handle both cases of true and false
                        resource.data?.let { isAuthenticated ->
                            if (isAuthenticated) {
                                // Verification process completed successfully
                                /*Navigation.findNavController(view)
                                    .navigate(R.id.action_otpVerificationFragment_to_homeFragment)*/
                                binding.btnVerify.revertAnimation()
                                Toast.makeText(requireContext(), "you r in mf", Toast.LENGTH_LONG).show()

                            }
                        }
                    }
                    is Resource.Error -> {
                        // Handle the verification error (e.g., display a message)
                        // resource.message contains the error message
                        binding.btnVerify.revertAnimation()
                        Toast.makeText(requireContext(), "you r not in mf ", Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }




        // Observe the isAuthenticated LiveData

    }

    private fun editTextInput() {
        binding.etC1.doOnTextChanged { _, _, _, _ ->
            binding.etC2.requestFocus()
        }
        binding.etC2.doOnTextChanged { _, _, _, _ ->
            binding.etC3.requestFocus()
        }
        binding.etC3.doOnTextChanged { _, _, _, _ ->
            binding.etC4.requestFocus()
        }
        binding.etC4.doOnTextChanged { _, _, _, _ ->
            binding.etC5.requestFocus()
        }
        binding.etC5.doOnTextChanged { _, _, _, _ ->
            binding.etC6.requestFocus()
        }

    }




}
