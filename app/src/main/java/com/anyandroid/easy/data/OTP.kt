package com.anyandroid.easy.data

data class OTP(val c1:String ,
               val c2:String ,
               val c3:String ,
               val c4:String ,
               val c5:String ,
               val c6:String
) {
    constructor() : this("","","","","","")
}