package com.example.rotravel

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import com.squareup.picasso.Picasso

class ChooseMediaActivity : AppCompatActivity() {
    private lateinit var video : VideoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_media)

        video = findViewById(R.id.vvTest)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(video)
        video.setMediaController(mediaController)

        val arr = intent.getStringArrayExtra("videos")
        video.setVideoPath(arr!![1])
        //Picasso.get().load(Uri.parse(arr!![0])).resize(500, 500).into(video)
        video.requestFocus()
        video.start()

    }
}