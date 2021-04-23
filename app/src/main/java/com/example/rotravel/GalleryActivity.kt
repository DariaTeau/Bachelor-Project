package com.example.rotravel

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
//import com.github.hiteshsondhi88.libffmpeg.FFmpeg
//import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
//import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class GalleryActivity : AppCompatActivity() {
    private lateinit var galleryButton: FloatingActionButton
    private lateinit var uploadButton : FloatingActionButton
    private lateinit var uploadedImg : ImageView
    private lateinit var createdVideo : VideoView
    private lateinit var fireStorage : FirebaseStorage
    private lateinit var fireAuth : FirebaseAuth
    private lateinit var fireDB : DatabaseReference
    private var videoImgs : Array<Uri> = arrayOf<Uri>()
    private var paths : Array<String> = arrayOf<String>()
    private val imgReqCode = 100
    private var imgUri : Uri? = null
    private val TAG = "executing ffmpeg command"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        galleryButton = findViewById(R.id.btUpload)
        uploadedImg = findViewById(R.id.ivUploadedImg)
        createdVideo = findViewById(R.id.video)
        uploadButton = findViewById(R.id.btFireUpload)
        fireAuth = Firebase.auth
        fireDB = Firebase.database("https://rotravel-14ed2-default-rtdb.europe-west1.firebasedatabase.app/").reference

        val mediaController = MediaController(this)
        mediaController.setAnchorView(createdVideo)
        createdVideo.setMediaController(mediaController)

        galleryButton.setOnClickListener {
            selectMultiple()
        }

        uploadButton.setOnClickListener { uploadToFirebase() }
    }

    private fun uploadToFirebase() {
        fireStorage = Firebase.storage("gs://rotravel-14ed2.appspot.com")
        val newEntryRef = fireDB.child("Photos").child(fireAuth.currentUser.uid).push()
        var time = Calendar.getInstance().time.toString()
        var path = ""
        if (imgUri != null) {
            path = "photos/" + fireAuth.currentUser.uid + "/" + time + ".jpg"
        } else {
            path = "photos/" + fireAuth.currentUser.uid + "/" + time + ".mp4"
        }

        val photoRef = fireStorage.reference.child(path)
        var uploadTask : UploadTask? = null
        if (imgUri != null) {
            val bitmap = (uploadedImg.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            uploadTask = photoRef.putBytes(data)
        } else {
            uploadTask = photoRef.putFile(Uri.fromFile(File("/storage/emulated/0/Videos/output.mp4")))
        }
        uploadTask?.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.w("uploadToFirebase", "upload failed")
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
        }?.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            val lat = intent.getDoubleExtra("latitude", 0.0)
            val lon = intent.getDoubleExtra("longitude", 0.0)
           // photoRef.downloadUrl.toString()
            photoRef.downloadUrl.addOnSuccessListener {
                val info = ImgInfo(lat.toString(), lon.toString(), it.toString(), "spring")
                newEntryRef.setValue(info)
                Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
            }

        }
    }
    private fun selectMultiple() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        // sau action pick?
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        //intent.addCategory(Intent.CATEGORY_OPENABLE)
        //intent.type = "image/*"
        startActivityForResult(intent, imgReqCode);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == imgReqCode){

            // if multiple images are selected
            if (data != null && data.clipData != null) {
                var count = data.clipData!!.itemCount
                if(count == 1) {
                    createdVideo.visibility = View.INVISIBLE
                    imgUri = data?.data
                    uploadedImg.setImageURI(imgUri)
                    return;
                }
                createdVideo.visibility = View.VISIBLE
                for (i in 0..count - 1) {
                    val item = data.clipData?.getItemAt(i)
                    if(item != null) {
                        videoImgs += item.uri
                    }
                }
                initFfmpeg()

            } else if (data != null && data.data != null) {
                // if single image is selected

                imgUri = data?.data
                uploadedImg.setImageURI(imgUri)

            }
        }
    }

//    String strCommand = "ffmpeg -loop 1 -t 3 -i " + /sdcard/videokit/1.jpg + " -loop 1 -t 3 -i " + /sdcard/videokit/2.jpg + " -loop 1 -t 3 -i " + /sdcard/videokit/3.jpg
//    + " -loop 1 -t 3 -i " + /sdcard/videokit/4.jpg +
//    " -filter_complex [0:v]trim=duration=3,fade=t=out:st=2.5:d=0.5[v0];[1:v]trim=duration=3,fade=t=in:st=0:d=0.5,fade=t=out:st=2.5:d=0.5[v1];[2:v]trim=duration=3,fade=t=in:st=0:d=0.5,fade=t=out:st=2.5:d=0.5[v2];[3:v]trim=duration=3,fade=t=in:st=0:d=0.5,fade=t=out:st=2.5:d=0.5[v3];[v0][v1][v2][v3]concat=n=4:v=1:a=0,format=yuv420p[v] -map [v] -preset ultrafast "
//    + /sdcard/videolit/output.mp4;






    private fun initFfmpeg() {
        transpose()
        val command = createCommand()
        var rc = FFmpeg.execute(command)
        if (rc == RETURN_CODE_SUCCESS) {
            Log.i(TAG, "Command execution completed successfully.")
            createdVideo.setVideoPath("/storage/emulated/0/Videos/output.mp4")
            createdVideo.requestFocus()
            createdVideo.start()
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(TAG, "Command execution cancelled by user.");
        } else {
            Log.i(TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            //Config.printLastCommandOutput(Log.INFO);
        }

    }

    private fun getOrientation(pathToImage : String) : String? {
        val exif = ExifInterface(pathToImage)
        var attr = exif.getAttribute(ExifInterface.TAG_ORIENTATION)
        if(attr != null) {
            Log.i("orientation", attr)
        }
        return attr
    }

    private fun transpose() {
        var test : File = File("/storage/emulated/0/Rotated");
        if (!test.exists()) {
            test.mkdirs()
        }
        for(i in 0..videoImgs.size - 1) {
            val path = getPath(this, videoImgs[i])
            paths += path
            if(getOrientation(path) == "6") {
                var comm = arrayOf<String>()
                comm += "-i"
                comm += path
                comm += "-vf"
                //scale=4640:3472,
                //val partial = "scale=4640:3472:force_original_aspect_ratio=decrease,pad=(ow-iw)/2:(oh-ih)/2"
                comm += ("transpose=dir=1")
                comm += "-y"
                comm += ("/storage/emulated/0/Rotated/output$i.jpeg")
                var rc = FFmpeg.execute(comm)
                if (rc == RETURN_CODE_SUCCESS) {
                    Log.i(TAG, "Command execution completed successfully when transposing.")
                    videoImgs[i] = Uri.fromFile(File("/storage/emulated/0/Rotated/output$i.jpeg"))
                    paths[i] = "/storage/emulated/0/Rotated/output$i.jpeg"
                }
            }
        }
    }

    private fun createCommand() : Array<String>{
        var comm = arrayOf<String>()
        val count = videoImgs.size
        var idx = 1;
        var param = ""
        var path = ""
        comm += "-noautorotate"
        for (i in 0..count - 1) {
            comm += "-framerate"
            comm += "25"
            comm += "-t"
            comm += "2"
            comm += "-loop"
            comm += "1"
            path = paths[i]  //getPath(this, videoImgs[i])
            comm += "-i"
            comm += path
            param += "[" + i + "]"

        }
        var test : File = File("/storage/emulated/0/Videos");
        if (!test.exists()) {
            if (test.mkdirs())
                Log.e("FILE", "MADE IT!!")
            else
                Log.e("FILE", "DIDNT MAKE IT!!")
        }
        //val partial = "[0:v] scale=iw*min(1920/iw\\\\,1080/ih):ih*min(1920/iw\\\\,1080/ih), pad=1920:1080:(1920-iw*min(1920/iw\\\\,1080/ih))/2:(1080-ih*min(1920/iw\\\\,1080/ih))/2,setsar=1:1[v0];[v0][0:a] "
        //:force_original_aspect_ratio=decrease
        //[1:v]scale=4640:3472,setsar=1,pad=4640:3472:(ow-iw)/2:(oh-ih)/2[1v];
        //[1v][1:a]
        //val partial = "[0:v]scale=4640:3472,setsar=1,pad=4640:3472:(ow-iw)/2:(oh-ih)/2[0v];[0v][0:a]"
        param +=  "concat=n=" + count + ":v=1,format=yuv420p[v]"
        comm += "-filter_complex"
        comm +=  param
        comm += "-map"
        comm += "[v]"
//        comm += "-map"
//        comm += "[a]"
//        comm += "-c:v"
//        comm += "-vcodec"
//        comm += "libx264"
//        comm += "-s"
//        comm += "640x480"
//        comm += "-vf"
//        comm += "transpose=dir=1:passthrough=landscape"
        comm += "-y"
        //comm += test.absolutePath
        comm += "/storage/emulated/0/Videos/output.mp4"
        return comm
    }

    private fun  getPath(context : Context, uri : Uri) : String{
        var result : String = ""
        //var proj = arrayOf<String>(MediaStore.Images.Media.DATA)
        var proj = arrayOf<String>(MediaStore.Images.Media.DATA)
        var cursor : Cursor? = context.contentResolver.query( uri, proj, null, null, null )
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                var column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result.isEmpty()) {
            result = "Not found";
        }
        //Runtime.getRuntime().exec("chmod 0777 "+result).waitFor();
        return result
    }

}