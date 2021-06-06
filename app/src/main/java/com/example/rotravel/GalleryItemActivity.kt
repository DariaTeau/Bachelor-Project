package com.example.rotravel

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class GalleryItemActivity : AppCompatActivity() {
    private lateinit var video : VideoView
    private lateinit var img : ImageView
    lateinit var fireAuth : FirebaseAuth
    private lateinit var fireDB : DatabaseReference
    private lateinit var user : TextView
    private lateinit var descr : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_media)

        fireAuth = Firebase.auth
        fireDB = Firebase.database("https://rotravel-14ed2-default-rtdb.europe-west1.firebasedatabase.app/").reference

        user = findViewById(R.id.tvUser)
        descr = findViewById(R.id.tvDescription)
        video = findViewById(R.id.vvTest)
        img = findViewById(R.id.ivPhoto)

        var itemUrl = ""
        getItemInfo()
        if(intent.getStringExtra("video") == null) {
            itemUrl = intent.getStringExtra("photo")
            Picasso.get().load(itemUrl).into(img)
        } else {
            itemUrl = intent.getStringExtra("video")
            val mediaController = MediaController(this)
            mediaController.setAnchorView(video)
            video.setMediaController(mediaController)

            video.setVideoPath(itemUrl)
            video.requestFocus()
            video.start()

        }
//        val mediaController = MediaController(this)
//        mediaController.setAnchorView(video)
//        video.setMediaController(mediaController)
//
//        //val arr = intent.getStringExtra("video")
//        video.setVideoPath(arr)
//        //Picasso.get().load(Uri.parse(arr!![0])).resize(500, 500).into(video)
//        video.requestFocus()
//        video.start()

    }

    private fun getItemInfo() {
        var uid = intent.getStringExtra("user")
        fireDB.child("Users").child(uid).child("username").get()
            .addOnSuccessListener {
                if(it.exists()) {
                    user.text = it.value.toString()
                    descr.text = intent.getStringExtra("descr")
                }
            }
    }
}