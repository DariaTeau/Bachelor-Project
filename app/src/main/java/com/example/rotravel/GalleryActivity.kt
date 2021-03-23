package com.example.rotravel

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

class GalleryActivity : AppCompatActivity() {
    private lateinit var galleryButton: Button
    private lateinit var uploadButton : Button
    private lateinit var uploadedImg : ImageView
    private lateinit var fireStorage : FirebaseStorage
    private lateinit var fireAuth : FirebaseAuth
    private lateinit var fireDB : DatabaseReference
    private val imgReqCode = 100
    private var imgUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        galleryButton = findViewById(R.id.btUpload)
        uploadedImg = findViewById(R.id.ivUploadedImg)
        uploadButton = findViewById(R.id.btFireUpload)
        fireAuth = Firebase.auth
        fireDB = Firebase.database("https://rotravel-14ed2-default-rtdb.europe-west1.firebasedatabase.app/").reference

        galleryButton.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, imgReqCode)
        }

        uploadButton.setOnClickListener { uploadToFirebase() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == imgReqCode) {
            imgUri = data?.data
            uploadedImg.setImageURI(imgUri)
        }
    }

    private fun uploadToFirebase() {
        fireStorage = Firebase.storage("gs://rotravel-14ed2.appspot.com")
        val newEntryRef = fireDB.child("Photos").child(fireAuth.currentUser.uid).push()
        var time = Calendar.getInstance().time.toString()
        val path = "photos/" + fireAuth.currentUser.uid + "/" + time + ".jpg"
        val photoRef = fireStorage.reference.child(path)
        //uploadedImg.isDrawingCacheEnabled = true
        //uploadedImg.draw
        //uploadedImg.buildDrawingCache()
        val bitmap = (uploadedImg.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = photoRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.w("uploadToFirebase", "upload failed")
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            val info = ImgInfo("1234", "1234", path, "spring")
            newEntryRef.setValue(info)
            Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
        }


    }

}