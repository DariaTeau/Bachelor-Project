package com.example.rotravel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DestinationListActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fireDB : DatabaseReference
    private lateinit var fireAuth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination_list)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fireAuth = Firebase.auth
        fireDB = Firebase.database("https://rotravel-14ed2-default-rtdb.europe-west1.firebasedatabase.app/").reference
        getDestinations()
    }
    
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val romanianBounds = LatLngBounds(
            LatLng((43.688), 20.22),  // SW bounds
            LatLng((48.22), 29.626) // NE bounds
        )
        val cameraBounds = LatLngBounds(
            LatLng((45.0), 22.0),  // SW bounds
            LatLng((50.0), 31.0) // NE bounds
        )
        var width : Int = resources.displayMetrics.widthPixels;
        var height : Int = resources.displayMetrics.heightPixels;
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(romanianBounds, width, height, 0))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(romanianBounds.center, 6.2f))
        mMap.setLatLngBoundsForCameraTarget(cameraBounds)
    }

    private fun getDestinations() {
        fireDB.child("Users").child(fireAuth.uid.toString()).child("FutureDest").get().addOnSuccessListener {
            for(item in it.children) {
                Log.i("getDestinations", item.key.toString() + " " + item.value.toString())
                val pos = item.value.toString().split(";").toTypedArray()
                mMap.addMarker(MarkerOptions().position(LatLng(pos[0].toDouble(), pos[1].toDouble())).title(item.key.toString()))
            }
        }
    }
}