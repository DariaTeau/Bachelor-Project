package com.example.rotravel

import android.net.Uri
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ImgInfo(val lat: String? = null, val lon: String? = null, val url: String? = null,
                   val season: String? = null, val private : String, val description : String? = null) {

}
