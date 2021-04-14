package com.example.rotravel

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class DisplayImgsActivity : AppCompatActivity() {
    private lateinit var galleryView : RecyclerView
    private var adapter : DisplayImgAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_imgs)

        galleryView = findViewById(R.id.rvGallery)
        galleryView.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        val arr = intent.getStringArrayExtra("photos")
        adapter = arr?.let { DisplayImgAdapter(it) }
        galleryView.adapter = adapter
    }
}

class ImageItemViewHolder(val imgView: View): RecyclerView.ViewHolder(imgView) {
    private var url : String = ""
    fun bindPhoto(photoUrl: String) {
        this.url = photoUrl
        Picasso.get().load(this.url).resize(500, 500).into(imgView.findViewById(R.id.ivModel) as ImageView)
    }
}