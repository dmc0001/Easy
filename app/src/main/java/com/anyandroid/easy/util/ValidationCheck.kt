package com.anyandroid.easy.util

import android.util.Patterns


fun validEmail(email: String): RegisterValidation {
    if (email.isEmpty())
        return RegisterValidation.Failed("Email cannot be an empty")

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return RegisterValidation.Failed("Wrong email format")
    return RegisterValidation.Success
}

fun validFirstName(firstName: String): RegisterValidation {
    if (firstName.isEmpty())
        return RegisterValidation.Failed("First name cannot be an empty")
    return RegisterValidation.Success
}

fun validLastName(lastName: String): RegisterValidation {
    if (lastName.isEmpty())
        return RegisterValidation.Failed("Last name cannot be an empty")
    return RegisterValidation.Success
}

fun validPassword(password: String): RegisterValidation {
    if (password.isEmpty())
        return RegisterValidation.Failed("Password cannot be an empty")
    if (password.length < 6)
        return RegisterValidation.Failed("Password should contains 6 char at least")
    return RegisterValidation.Success
}

fun validPhoneNumber(phone: String): RegisterValidation {
    if (phone.isEmpty())
        return RegisterValidation.Failed("Phone number is required!")
    else if (!Patterns.PHONE.matcher(phone).matches() || phone.length < 11)

        return RegisterValidation.Failed("Need valid phone number!")

    return RegisterValidation.Success
}

fun validOTP(
    c1: String,
    c2: String,
    c3: String,
    c4: String,
    c5: String,
    c6: String
): VerificationOTPValidation {

    if (c1.isEmpty() || c2.isEmpty() || c3.isEmpty() || c4.isEmpty() || c5.isEmpty() || c6.isEmpty()) {
        return VerificationOTPValidation.Failed("OTP is not valid!")
    }
    return VerificationOTPValidation.Success
}