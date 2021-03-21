package com.example.rotravel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //mapFragment.setHasOptionsMenu(true)

        bottomNav = findViewById(R.id.navButton)
        //bottomNav.inflateMenu(R.menu.main_map_options)
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.uploadItem -> {
                    val intent = Intent(this, UploadPhotoActivity::class.java).apply {}
                    startActivity(intent)
                    true
                }
                R.id.profileItem -> {
                    //Log.i("onOptionsItemSelected", "am selectat profilul")

                    val intent = Intent(this, UserProfileActivity::class.java).apply {}
                    startActivity(intent)
                    true
            }
                else -> true

            }
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        val buc = LatLng(44.439663, 26.096306)
        mMap.addMarker(MarkerOptions().position(buc).title("Marker in Romania"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(buc))
        //mMap.setMinZoomPreference(2.0F)

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
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(romanianBounds.center, 6.2f))
        mMap.setLatLngBoundsForCameraTarget(cameraBounds)
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(romanianBounds, width, height, 0))
//        mMap.setOnMapLoadedCallback {
//            mMap.addMarker(MarkerOptions().position(buc).title("Marker in Romania"))
//            mMap.setLatLngBoundsForCameraTarget(cameraBounds)
//            mMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
//            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(romanianBounds, 10)) }

    }
    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {
        private val contents: View = layoutInflater.inflate(R.layout.custom_info_contents, null)
        override fun getInfoWindow(marker: Marker): View? {
            return null
        }

        override fun getInfoContents(marker: Marker): View? {
            contents.findViewById<ImageView>(R.id.ivInfoWin).setImageResource(R.drawable.bucharest)
            return contents
        }

    }
//    override fun onCreateOptionsMenu(menu: Menu) : Boolean {
//        val inflater: MenuInflater = menuInflater
//        inflater.inflate(R.menu.main_map_options, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle item selection
//        when (item.itemId) {
//            R.id.uploadItem -> {
//                Log.i("onOptionsItemSelected", "am selectat upload")
//
//                val intent = Intent(this, UploadPhotoActivity::class.java).apply {}
//                startActivity(intent)
//                return true
//            }
//            R.id.profileItem -> {
//                Log.i("onOptionsItemSelected", "am selectat profilul")
//
//                val intent2 = Intent(this, UserProfileActivity::class.java).apply {}
//                startActivity(intent2)
//                return true
//            }
//            else -> {
//                Log.i("onOptionsItemSelected", "am selectat map")
//
//                return super.onOptionsItemSelected(item)
//            }
//        }
//        //return true
//    }

}