package com.example.rotravel

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File


class DisplayVideosActivity : AppCompatActivity() {

    private lateinit var galleryView : RecyclerView
    private var adapter : DisplayVideosAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_videos)

        galleryView = findViewById(R.id.rvVidGallery)
            galleryView.layoutManager = GridLayoutManager(
                this,
                3,
                GridLayoutManager.VERTICAL,
                false
            )
            val arr = intent.getStringArrayExtra("videos")
            adapter = arr?.let { DisplayVideosAdapter(it) }
            galleryView.adapter = adapter
        }
}

class VideoItemViewHolder(val videoView: View): RecyclerView.ViewHolder(videoView) {
    private var url : String = ""
    fun bindVideo(videoUrl: String) {
        this.url = videoUrl
        //Picasso.get().load(this.url).resize(500, 500).into(videoView.findViewById(R.id.ivModel) as VideoView
        var vid =  (videoView.findViewById(R.id.vvModel) as VideoView)
        vid.setVideoPath(videoUrl)
        //(videoView.findViewById(R.id.vvModel) as VideoView).set
        val mediaController = MediaController(vid.context)
        mediaController.setAnchorView(vid)
        vid.setMediaController(mediaController)
        vid.requestFocus()

        //var img : Bitmap = ThumbnailUtils.createVideoThumbnail(File(videoUrl), Thu)
    }
}