package com.example.rotravel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var bottomNav : BottomNavigationView
    private lateinit var searchBt : FloatingActionButton
    private lateinit var fireDB : DatabaseReference
    private lateinit var fireAuth : FirebaseAuth
    private lateinit var placesClient : PlacesClient
    private var imgMap : HashMap<String, Array<String>> = HashMap()
    private var videoMap : HashMap<String, Array<String>> = HashMap()
    private var userImgMap : HashMap<String, Array<String>> = HashMap()
    private var userVideoMap : HashMap<String, Array<String>> = HashMap()
    private var friendRequests : HashMap<String, String> = HashMap()
    private var videoDetails : HashMap<String, HashMap<String, ItemDetails>> = HashMap()
    private var photoDetails : HashMap<String, HashMap<String, ItemDetails>> = HashMap()
    private var userVideoDetails : HashMap<String, HashMap<String, ItemDetails>> = HashMap()
    private var userPhotoDetails : HashMap<String, HashMap<String, ItemDetails>> = HashMap()


    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private var destDescr : String = "default text"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.uploadMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //mapFragment.setHasOptionsMenu(true)

        //NearbyCommunication.doInit(this)

        Places.initialize(this, "AIzaSyBKM7rITzpe9u2-kEu5lt_ePs4zpg4UChg")
        placesClient = Places.createClient(this)
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
                    intent.putExtra("userImgMap", userImgMap)
                    intent.putExtra("userVideoMap", userVideoMap)
                    intent.putExtra("userPhotoDetails", userPhotoDetails)
                    intent.putExtra("userVideoDetails", userVideoDetails)
                    startActivity(intent)
                    true
                }
                R.id.profileItem -> {
                    //Log.i("onOptionsItemSelected", "am selectat profilul")
                    val intent = Intent(this, UserProfileActivity::class.java).apply {}
                    intent.putExtra("imgMap", userImgMap)
                    intent.putExtra("videoMap", userVideoMap)
                    intent.putExtra("photoDetails", userPhotoDetails)
                    intent.putExtra("videoDetails", userVideoDetails)
                    intent.putExtra("imgAll", imgMap.keys.toTypedArray())
                    intent.putExtra("videoAll", videoMap.keys.toTypedArray())
                    startActivity(intent)
                    true
            }
                else -> true

            }
        }

        searchBt = findViewById(R.id.btSearch)
        searchBt.setOnClickListener { autoCompleteIntent() }

//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // Get Post object and use the values to update the UI
//                //val post = dataSnapshot.getValue<Post>()
//                // ...
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w("onCancelled", "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//
//        fireDB.child("Photos").addValueEventListener(postListener)
        checkRequests()
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
        //mMap.setLatLngBoundsForCameraTarget(cameraBounds)
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
                    //Toast.makeText(this, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
                    launchPhotosDisplay(marker) }
                R.id.itemVideos -> {
                    //Toast.makeText(this, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
                    launchVideosDisplay(marker)
                }
                R.id.itemList -> {
                    //popup.dismiss()
                    createPopup(marker)
                    //Toast.makeText(this, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
                }
            }
            true
        })
        popup.show()
    }

    private fun createPopup(marker: Marker) {
        var popupWindow = PopupWindow(this)
        val view = layoutInflater.inflate(R.layout.descr_popup_window, null)
        popupWindow.contentView = view
        //TransitionManager.beginDelayedTransition(findViewById(R.layout.activity_map))
        var descr : EditText = view.findViewById(R.id.etDestName)
        var done : Button = view.findViewById(R.id.btDone)
        done.setOnClickListener {
            destDescr = descr.text.toString()
            addToDestList(marker)
            popupWindow.dismiss()}
        popupWindow.showAtLocation(this.bottomNav, Gravity.CENTER, 0, 20)
        popupWindow.setFocusable(true)
        popupWindow.update()

    }

    private fun launchPhotosDisplay(marker : Marker) {
        val intent = Intent(this, DisplayImgsActivity::class.java).apply {}
        //val intent = Intent(this, GalleryItemActivity::class.java).apply {}
        var bundle : Bundle = Bundle()
        val key = marker.position.latitude.toString() + "&" + marker.position.longitude.toString()
        //bundle.putStringArray("videos", videoMap[key])
        bundle.putStringArray("photos", imgMap[key])
        intent.putExtra("details", photoDetails[key])
        intent.putExtras(bundle);
        startActivity(intent)

    }

    private fun launchVideosDisplay(marker : Marker) {
        val intent = Intent(this, DisplayVideosActivity::class.java).apply {}
        //val intent = Intent(this, GalleryItemActivity::class.java).apply {}
        var bundle : Bundle = Bundle()
        val key = marker.position.latitude.toString() + "&" + marker.position.longitude.toString()
        bundle.putStringArray("videos", videoMap[key])
        //bundle.putStringArray("photos", imgMap[key])
        intent.putExtra("details", videoDetails[key])
        intent.putExtras(bundle);
        startActivity(intent)

    }

    private fun getPins() {
        fireDB.child("Photos").get().addOnSuccessListener {
            for(snap in it.children) {
                for(ds in snap.children) {
                    var private = ds.child("private").value.toString()
                    if(private == "false" || snap.key.toString() == fireAuth.currentUser.uid) {
                        computePhotos(ds, snap)
                    } else {
                        fireDB.child("Users").child(fireAuth.currentUser.uid).child("Friends").child(snap.key.toString()).get()
                            .addOnSuccessListener {
                                if(it.exists()) {
                                    computePhotos(ds, snap)
                                }
                            }
                    }

                }
            }

        }
    }

    private fun computePhotos(ds : DataSnapshot, snap: DataSnapshot) {
        var lat = ds.child("lat")
        var lon = ds.child("lon")
        Log.i("getFromDB", lat.value.toString() + " " + lon.value.toString())
        var marker = LatLng(lat.value.toString().toDouble(), lon.value.toString().toDouble())
        val url = ds.child("url").value.toString()
        var key = lat.value.toString() + "&" + lon.value.toString()
        var descr = ""
        if(ds.child("description").exists()) {
            descr = ds.child("description").value.toString()
        }
        if(url.contains("mp4")) {
            var vidArr = videoMap[key]
            if(vidArr != null) {
                videoMap[key] = vidArr + url
                videoDetails[key]?.set(url,
                    ItemDetails(snap.key.toString(), descr)
                )
            } else {
                videoMap[key] = arrayOf(url)
                videoDetails[key] = HashMap()
                videoDetails[key]?.set(url,
                    ItemDetails(snap.key.toString(), descr)
                )
            }
        } else {
            var imgArr = imgMap[key]
            if(imgArr != null) {
                imgMap[key] = imgArr + url
                photoDetails[key]?.set(url,
                    ItemDetails(snap.key.toString(), descr)
                )
            } else {
                imgMap[key] = arrayOf(url)
                photoDetails[key] = HashMap()
                photoDetails[key]?.set(url,
                    ItemDetails(snap.key.toString(), descr)
                )
            }
        }

        mMap.addMarker(MarkerOptions().position(marker).title("marker").title("Photos"))

        if(snap.key!! == fireAuth.uid) {
            if (url.contains("mp4")) {
                var arr = userVideoMap.get(key)
                if (arr != null) {
                    userVideoMap[key] = arr + url
                    userVideoDetails[key]?.set(url,
                        ItemDetails(snap.key.toString(), descr)
                    )
                } else {
                    userVideoMap[key] = arrayOf(url)
                    userVideoDetails[key] = HashMap()
                    userVideoDetails[key]?.set(url,
                        ItemDetails(snap.key.toString(), descr)
                    )
                }
            } else {
                var arr = userImgMap.get(key)
                if (arr != null) {
                    userImgMap[key] = arr + url
                    userPhotoDetails[key]?.set(url,
                        ItemDetails(snap.key.toString(), descr)
                    )
                } else {
                    userImgMap[key] = arrayOf(url)
                    userPhotoDetails[key] = HashMap()
                    userPhotoDetails[key]?.set(url,
                        ItemDetails(snap.key.toString(), descr)
                    )
                }
            }
        }
    }
    private fun autoCompleteIntent() {

        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        Log.i("onActivityResult", "Place: ${place.name}, ${place.id}")
                        //mMap.addMarker(MarkerOptions().position(place.latLng!!).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                        //centerMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        val key = place.latLng?.latitude.toString() + "&" + place.latLng?.longitude.toString()
                        if(videoMap[key] != null || imgMap[key] != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 10.0f));
                        } else {
                            mMap.addMarker(MarkerOptions().position(place.latLng!!).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 10.0f));
                        }
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i("onActivityResult", status.statusMessage!!)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

     private fun addToDestList(marker: Marker) {
        fireDB.child("Users").child(fireAuth.currentUser.uid).child("FutureDest")
            .child(destDescr).setValue(marker.position.latitude.toString() + ";" + marker.position.longitude.toString())

        Toast.makeText(this, "Destination added to your travel list", Toast.LENGTH_LONG).show()
    }

    private fun checkRequests() {
        fireDB.child("Users").child(fireAuth.currentUser.uid).child("Pending").get().addOnSuccessListener {
            if(it.exists()) {
                for(item in it.children) {
                    friendRequests[item.key.toString()] = item.value.toString()
                }
                popForFriendReq()
            }
        }
    }

    private fun popForFriendReq() {
        var popupWindow = PopupWindow(this)
        val view = layoutInflater.inflate(R.layout.friend_request_popup, null)
        popupWindow.contentView = view
        //TransitionManager.beginDelayedTransition(findViewById(R.layout.activity_map))
        var check : ImageButton = view.findViewById(R.id.ibCheck)
        var cancel : ImageButton = view.findViewById(R.id.ibCancel)
        cancel.setOnClickListener {
            popupWindow.dismiss()}
        check.setOnClickListener {
            val intent = Intent(this, DisplayFriendRequestsActivity::class.java).apply {}
            intent.putExtra("requests", friendRequests)
            intent.putExtra("friends", false)
            popupWindow.dismiss()
            startActivity(intent)}
        popupWindow.showAtLocation(this.bottomNav, Gravity.CENTER, 0, 20)
        popupWindow.setFocusable(true)
        popupWindow.update()

    }

}