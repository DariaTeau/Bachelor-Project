package com.example.rotravel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var bottomNav : BottomNavigationView
    private lateinit var fireDB : DatabaseReference
    private lateinit var fireAuth : FirebaseAuth
    private var imgMap : HashMap<String, Array<String>> = HashMap()
    private var videoMap : HashMap<String, Array<String>> = HashMap()
    private var userImgMap : HashMap<String, Array<String>> = HashMap()
    private var userVideoMap : HashMap<String, Array<String>> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //mapFragment.setHasOptionsMenu(true)
        fireAuth = Firebase.auth
        fireDB = Firebase.database("https://rotravel-14ed2-default-rtdb.europe-west1.firebasedatabase.app/").reference
        //def = GlobalScope.async { getPins() }
        getPins()

        bottomNav = findViewById(R.id.navButton)
        //bottomNav.inflateMenu(R.menu.main_map_options)
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.uploadItem -> {
                    val intent = Intent(this, UploadPhotoActivity::class.java).apply {}
                    intent.putExtra("imgMap", imgMap.keys.toTypedArray())
                    intent.putExtra("videoMap", videoMap.keys.toTypedArray())
                    startActivity(intent)
                    true
                }
                R.id.profileItem -> {
                    //Log.i("onOptionsItemSelected", "am selectat profilul")
                    val intent = Intent(this, UserProfileActivity::class.java).apply {}
                    intent.putExtra("imgMap", userImgMap)
                    intent.putExtra("videoMap", userVideoMap)
                    startActivity(intent)
                    true
            }
                else -> true

            }
        }

//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // Get Post object and use the values to update the UI
//                //val post = dataSnapshot.getValue<Post>()
//                // ...
//                for(snap in dataSnapshot.children) {
//                    for(ds in snap.children) {
//                        var lat = ds.child("lat")
//                        var lon = ds.child("lon")
//                        Log.i("getFromDB", lat.value.toString() + " " + lon.value.toString())
//                        var marker = LatLng(lat.value.toString().toDouble(), lon.value.toString().toDouble())
//                        val url = ds.child("url").value.toString()
//                        val key = lat.value.toString() + "&" + lon.value.toString()
//                        if(url.contains("mp4")) {
//                            var vidArr = videoMap[key]
//                            if(vidArr != null) {
//                                vidArr += url
//                            } else {
//                                videoMap[key] = arrayOf(url)
//                            }
//                        } else {
//                            var imgArr = imgMap[key]
//                            if(imgArr != null) {
//                                imgArr += url
//                            } else {
//                                imgMap[key] = arrayOf(url)
//                            }
//                        }
//
//                        mMap.addMarker(MarkerOptions().position(marker).title("marker").title("Photos"))
//
//                        if(snap.key!! == fireAuth.uid) {
//                            if (url.contains("mp4")) {
//                                var arr = userVideoMap.get(key)
//                                if (arr != null) {
//                                    userVideoMap.get(key)?.plus(url)
//                                } else {
//                                    userVideoMap[key] = arrayOf(url)
//                                }
//                            } else {
//                                var arr = userImgMap.get(key)
//                                if (arr != null) {
//                                    userImgMap.get(key)?.plus(url)
//                                } else {
//                                    userImgMap[key] = arrayOf(url)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w("onCancelled", "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//
//        fireDB.child("Photos").addValueEventListener(postListener)

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
        googleMap.setOnInfoWindowClickListener(this)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(romanianBounds, width, height, 0))
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

    override fun onInfoWindowClick(marker: Marker) {
        val popup = PopupMenu(this, this.bottomNav, Gravity.CENTER)
        popup.menuInflater.inflate(R.menu.marker_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.itemPhotos -> {
                    Toast.makeText(this, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
                    launchPhotosDisplay(marker) }
                R.id.itemVideos -> {
                    Toast.makeText(this, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
                    launchVideosDisplay(marker)
                }
                R.id.itemList ->
                    Toast.makeText(this, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
            }
            true
        })
        popup.show()
    }

    private fun launchPhotosDisplay(marker : Marker) {
        val intent = Intent(this, DisplayImgsActivity::class.java).apply {}
        //val intent = Intent(this, ChooseMediaActivity::class.java).apply {}
        var bundle : Bundle = Bundle()
        val key = marker.position.latitude.toString() + "&" + marker.position.longitude.toString()
        //bundle.putStringArray("videos", videoMap[key])
        bundle.putStringArray("photos", imgMap[key])
        intent.putExtras(bundle);
        startActivity(intent)

    }

    private fun launchVideosDisplay(marker : Marker) {
        val intent = Intent(this, DisplayVideosActivity::class.java).apply {}
        //val intent = Intent(this, ChooseMediaActivity::class.java).apply {}
        var bundle : Bundle = Bundle()
        val key = marker.position.latitude.toString() + "&" + marker.position.longitude.toString()
        bundle.putStringArray("videos", videoMap[key])
        //bundle.putStringArray("photos", imgMap[key])
        intent.putExtras(bundle);
        startActivity(intent)

    }

    private fun getPins() {
        fireDB.child("Photos").get().addOnSuccessListener {
            for(snap in it.children) {
                for(ds in snap.children) {
                    var lat = ds.child("lat")
                    var lon = ds.child("lon")
                    Log.i("getFromDB", lat.value.toString() + " " + lon.value.toString())
                    var marker = LatLng(lat.value.toString().toDouble(), lon.value.toString().toDouble())
                    val url = ds.child("url").value.toString()
                    var key = lat.value.toString() + "&" + lon.value.toString()
                    if(url.contains("mp4")) {
                        var vidArr = videoMap[key]
                        if(vidArr != null) {
                            videoMap[key] = vidArr + url
                        } else {
                            videoMap[key] = arrayOf(url)
                        }
                    } else {
                        var imgArr = imgMap[key]
                        if(imgArr != null) {
                            imgMap[key] = imgArr + url
                        } else {
                            imgMap[key] = arrayOf(url)
                        }
                    }

                    mMap.addMarker(MarkerOptions().position(marker).title("marker").title("Photos"))

                    if(snap.key!! == fireAuth.uid) {
                        if (url.contains("mp4")) {
                            var arr = userVideoMap.get(key)
                            if (arr != null) {
                                userVideoMap[key] = arr + url
                            } else {
                                userVideoMap[key] = arrayOf(url)
                            }
                        } else {
                            var arr = userImgMap.get(key)
                            if (arr != null) {
                                userImgMap[key] = arr + url
                            } else {
                                userImgMap[key] = arrayOf(url)
                            }
                        }
                    }
                }
            }

        }
    }

}