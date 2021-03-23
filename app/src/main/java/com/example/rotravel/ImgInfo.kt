package com.example.rotravel

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ImgInfo(val lat: String? = null, val lon: String? = null, val url: String? = null, val season: String? = null) {

}
