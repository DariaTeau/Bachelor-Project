package com.example.rotravel

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserInfo(val username: String? = null, val email: String? = null) {}

