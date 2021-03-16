package com.example.rotravel

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView

class GalleryActivity : AppCompatActivity() {
    private lateinit var galleryButton: Button
    private lateinit var uploadedImg : ImageView
    private val imgReqCode = 100
    private var imgUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        galleryButton = findViewById(R.id.btUpload)
        uploadedImg = findViewById(R.id.ivUploadedImg)

        galleryButton.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, imgReqCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == imgReqCode) {
            imgUri = data?.data
            uploadedImg.setImageURI(imgUri)
        }
    }
}