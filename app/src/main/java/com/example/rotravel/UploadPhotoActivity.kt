package com.example.rotravel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class UploadPhotoActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private var photoPos : LatLng = LatLng(0.0, 0.0)
    //private lateinit var galleryFragment : GalleryFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_photo)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        val buc = LatLng(44.439663, 26.096306)

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
        val centerMarker = MarkerOptions().position(romanianBounds.center).title("Choose your picture").draggable(true)
        mMap.addMarker(centerMarker)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(romanianBounds, width, height, 0))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(romanianBounds.center, 6.2f))
        mMap.setLatLngBoundsForCameraTarget(cameraBounds)

        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker?) {

            }

            override fun onMarkerDragStart(p0: Marker?) {

            }
            override fun onMarkerDragEnd(p0: Marker?) {
                if (p0 != null) {
                    this@UploadPhotoActivity.photoPos = p0.position
                    Log.i("onMarkerDragEnd", "position is " + photoPos.latitude + " " + photoPos.longitude)
                }
            }
        })
        googleMap.setOnInfoWindowClickListener(this)

    }

    override fun onInfoWindowClick(marker: Marker) {
        Log.i("onInfoWindowClick", "am dat click")

        val intent = Intent(this, GalleryActivity::class.java).apply {}
        intent.putExtra("latitude", photoPos.latitude)
        intent.putExtra("longitude", photoPos.longitude)
        startActivity(intent)
    }


}