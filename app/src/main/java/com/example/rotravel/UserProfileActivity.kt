package com.example.rotravel

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserProfileActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    lateinit var mGoogleSignInClient : GoogleSignInClient
    lateinit var fireAuth : FirebaseAuth
    private lateinit var fireDB : DatabaseReference
    private lateinit var mMap: GoogleMap
    private var markers : Array<LatLng> = arrayOf<LatLng>()
    private var photosUrls : Array<String> = arrayOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.user_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("318758695349-3ntogdpicl027jop4dqtb05jd2jvgd1v.apps.googleusercontent.com")
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        fireAuth = Firebase.auth
        fireDB = Firebase.database("https://rotravel-14ed2-default-rtdb.europe-west1.firebasedatabase.app/").reference
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        val buc = LatLng(44.439663, 26.096306)
        getUserPins()
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
        var padding : Int = (width * 0.12).toInt();
        mMap.setLatLngBoundsForCameraTarget(cameraBounds)
        //mMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(romanianBounds, width, height, 0))
        googleMap.setOnInfoWindowClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu) : Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.user_profile_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.it_signOut -> {
                if(fireAuth.currentUser != null) {
                    fireAuth.signOut()
                    backToLogin()
                }
                mGoogleSignInClient.signOut().addOnCompleteListener {
                    backToLogin()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backToLogin() {
        val intent = Intent(this, MainActivity::class.java).apply {}
        startActivity(intent)
    }

    private fun getUserPins() {
        fireDB.child("Photos").child(fireAuth.uid!!).get().addOnSuccessListener {
            for(ds in it.children) {
                var lat = ds.child("lat")
                var lon = ds.child("lon")
                Log.i("getFromDB", lat.value.toString() + " " + lon.value.toString())
                var marker = LatLng(lat.value.toString().toDouble(), lon.value.toString().toDouble())
                photosUrls += ds.child("url").value.toString()
                mMap.addMarker(MarkerOptions().position(marker).title("marker").title("Photos"))
            }

        }
    }

    override fun onInfoWindowClick(marker: Marker) {

        val intent = Intent(this, DisplayImgsActivity::class.java).apply {}
        var bundle : Bundle = Bundle()
        //bundle.putParcelableArray()
        bundle.putStringArray("photos", photosUrls)
        intent.putExtras(bundle);
//        intent.putExtra("latitude", photoPos.latitude)
//        intent.putExtra("longitude", photoPos.longitude)
        Log.i("onInfoWindowClick", photosUrls.size.toString())
        startActivity(intent)
    }

}