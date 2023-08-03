package com.anyandroid.easy.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anyandroid.easy.data.OTP
import com.anyandroid.easy.util.OTPFieldsState
import com.anyandroid.easy.util.Resource
import com.anyandroid.easy.util.VerificationOTPValidation
import com.anyandroid.easy.util.validOTP
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(private val firebaseAuth: FirebaseAuth) : ViewModel() {
    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId


    private val _verificationInProgress =
        MutableStateFlow<Resource<Boolean>>(Resource.Unspecified())
    val verificationInProgress: Flow<Resource<Boolean>> = _verificationInProgress

    private val _validation = Channel<OTPFieldsState>()
    val validation = _validation.receiveAsFlow()

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d("onVerificationCompleted", "onVerificationCompleted:$credential")
                //signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("onVerificationFailed", "onVerificationFailed", e)



                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                _verificationId.value = verificationId
                _verificationInProgress.value = Resource.Success(true)
                Log.d("onCodeSent", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                // storedVerificationId = verificationId
                // resendToken = token
            }

        }
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    fun signInWithVerificationCode(verificationId: String, otp: OTP) {
        val credential = PhoneAuthProvider.getCredential(verificationId, "${otp.c1}${otp.c2}${otp.c3}${otp.c4}${otp.c5}${otp.c6}")
        signInWithPhoneAuthCredential(credential,otp)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, otp: OTP) {
        if (checkValidation(otp)) {
            runBlocking {
                _verificationInProgress.emit(Resource.Loading())
            }
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("addOnCompleteListener", "signInWithCredential:success")
                        _verificationInProgress.value = Resource.Success(false)
                        //val user = task.result?.user
                    } else  {
                        // Sign in failed, display a message and update the UI
                        Log.w(
                            "addOnCompleteListener",
                            "signInWithCredential:failure",
                            task.exception
                        )
                        _verificationInProgress.value = Resource.Error("Authentication failed")
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            _verificationInProgress.value = Resource.Error(task.exception.toString())
                        }
                        // Update UI
                    }
                }
        } else {
            val otpFieldsState =
                OTPFieldsState(validOTP(otp.c1, otp.c2, otp.c3, otp.c4, otp.c5, otp.c6))
            viewModelScope.launch {
                _validation.send(otpFieldsState)
            }
        }
    }


    private fun checkValidation(
        otp: OTP
    ): Boolean {
        val otpValidation = validOTP(otp.c1, otp.c2, otp.c3, otp.c4, otp.c5, otp.c6)
        return (otpValidation is VerificationOTPValidation.Success)
    }
}

