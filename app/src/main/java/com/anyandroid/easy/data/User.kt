package com.anyandroid.easy.data

data class User(
    val firstName :String,
    val lastName :String,
    val email:String,
    val phoneNumber :String,
    val role :String = "",
    val imagePath :String =""
)
{
    constructor() : this("","","","","","")
}