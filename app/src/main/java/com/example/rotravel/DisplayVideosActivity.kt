package com.example.rotravel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File


class DisplayVideosActivity : AppCompatActivity() {

    private lateinit var galleryView : RecyclerView
    private var adapter : DisplayVideosAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_videos)
        var details = intent.getSerializableExtra("details") as HashMap<String, ItemDetails>
        galleryView = findViewById(R.id.rvVidGallery)
            galleryView.layoutManager = GridLayoutManager(
                this,
                3,
                GridLayoutManager.VERTICAL,
                false
            )
            val arr = intent.getStringArrayExtra("videos")
            adapter = arr?.let { DisplayVideosAdapter(it, details) }
            galleryView.adapter = adapter
        }
}

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class VideoItemViewHolder(val videoView: View): RecyclerView.ViewHolder(videoView) {
    private var url : String = ""
    fun bindVideo(videoUrl: String) {
        this.url = videoUrl
        var vid =  (videoView.findViewById(R.id.vvModel) as VideoView)
        vid.setVideoPath(videoUrl)
        var imgView : ImageView = videoView.findViewById(R.id.ivThumbnail)

        imgView.visibility = View.INVISIBLE
        vid.background = imgView.drawable


//        val mediaController = MediaController(vid.context)
//        mediaController.setAnchorView(vid)
//        vid.setMediaController(mediaController)
        //vid.requestFocus()
        vid.setOnPreparedListener {
            vid.background = null
        }
        vid.start()

    }
}