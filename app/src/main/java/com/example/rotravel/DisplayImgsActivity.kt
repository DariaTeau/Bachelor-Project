package com.example.rotravel

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.VideoView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class DisplayImgsActivity : AppCompatActivity() {
    private lateinit var img : ImageView
    private lateinit var video : VideoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_imgs)
        img = findViewById(R.id.ivFromUser)
        video = findViewById(R.id.vvFromUser)
        val arr = intent.getStringArrayExtra("photos")
        var iter = arr?.iterator()
        var count = 0
        while(iter!!.hasNext()) {
            if(count == arr!!.size - 1) {
                Picasso.get().load(Uri.parse(iter.next())).into(img)
            } else {
                iter.next()
            }
            count++

        }
    }
}