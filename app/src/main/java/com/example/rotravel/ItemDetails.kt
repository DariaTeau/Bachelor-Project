package com.example.rotravel

import android.os.Parcelable
import androidx.versionedparcelable.VersionedParcelize
import java.io.Serializable

data class ItemDetails(val uid: String? = null, val description: String? = null) : Serializable
