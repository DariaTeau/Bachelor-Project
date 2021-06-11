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
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.gms.nearby.connection.Payload
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class GalleryActivity : AppCompatActivity() {
    private lateinit var galleryButton: FloatingActionButton
    private lateinit var uploadButton : FloatingActionButton
    private lateinit var descr : EditText
    private lateinit var privacySwitch : androidx.appcompat.widget.SwitchCompat
    private var onlyFriends = false
    private lateinit var uploadedImg : ImageView
    private lateinit var createdVideo : VideoView
    private lateinit var fireStorage : FirebaseStorage
    private lateinit var fireAuth : FirebaseAuth
    private lateinit var fireDB : DatabaseReference
    private var videoImgs : Array<Uri> = arrayOf<Uri>()
    private var sendImgs : Array<Uri> = arrayOf<Uri>()
    private var paths : Array<String> = arrayOf<String>()
    private val imgReqCode = 100
    private var imgUri : Uri? = null
    private val TAG = "executing ffmpeg command"
    private var begin : Long = 0
    private var end : Long = 0
    private var single = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //NearbyCommunication.doInit(this)
        setContentView(R.layout.activity_gallery)
        title = "Upload your photos"
        galleryButton = findViewById(R.id.btUpload)
        uploadedImg = findViewById(R.id.ivUploadedImg)
        createdVideo = findViewById(R.id.video)
        uploadButton = findViewById(R.id.btFireUpload)
        privacySwitch = findViewById(R.id.swPrivacy)
        descr = findViewById(R.id.etDescription)
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
        var path = "photos/" + fireAuth.currentUser.uid + "/"
        if(privacySwitch.isChecked) {
            path += "friends/"
        }
        if (imgUri != null) {
            //path = "photos/" + fireAuth.currentUser.uid + "/" + time + ".jpg"
            path += "$time.jpg"
        } else {
            //path = "photos/" + fireAuth.currentUser.uid + "/" + time + ".mp4"
            path += "$time.mp4"
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
            if(single) {
                uploadTask = photoRef.putFile(Uri.fromFile(File("/storage/emulated/0/Videos/output.mp4")))
            } else {
                uploadTask = photoRef.putFile(Uri.fromFile(File("/storage/emulated/0/Videos/final_output.mp4")))
            }
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
                val info = ImgInfo(lat.toString(), lon.toString(), it.toString(), "spring", privacySwitch.isChecked.toString(), descr.text.toString())
                newEntryRef.setValue(info)
                Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
            }

        }
    }
    private fun edit() {

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
                begin = Date().time
                var count = data.clipData!!.itemCount
                if(count == 1) {
                    createdVideo.visibility = View.INVISIBLE
                    imgUri = data?.data
                    uploadedImg.setImageURI(imgUri)
                    return;
                }
                createdVideo.visibility = View.VISIBLE
//                for(i in 0..(count/2)) {
//                    val item = data.clipData?.getItemAt(i)
//                    if(item != null) {
//                        sendImgs += item.uri
//                    }
//                }
//                GlobalScope.launch(Dispatchers.IO) {
//                    Log.i("GalleryActivity", "launch -> ${Thread.currentThread().name}")
//                    NearbyCommunication.mutex.lock()
//                    createAndSendPayload()}
//                for (i in (count/2)+1..count - 1) {
//                    val item = data.clipData?.getItemAt(i)
//                    if(item != null) {
//                        videoImgs += item.uri
//                    }
//                }
//                GlobalScope.launch(Dispatchers.Default) {
//                    Log.i("GalleryActivity", "launch -> ${Thread.currentThread().name}")
//                    single = false
//                    initFfmpeg(false)}

                for (i in 0..count - 1) {
                    val item = data.clipData?.getItemAt(i)
                    if(item != null) {
                        videoImgs += item.uri
                    }
                }
                GlobalScope.launch(Dispatchers.Default) {
                    Log.i("GalleryActivity", "launch -> ${Thread.currentThread().name}")
                    initFfmpeg(true)}



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






    private suspend fun initFfmpeg(single : Boolean) {
        transpose()
        val command = createCommand()
        var rc = FFmpeg.execute(command)
        if (rc == RETURN_CODE_SUCCESS) {
            Log.i(TAG, "Command execution completed successfully.")
            if(single) {
                GlobalScope.launch(Dispatchers.Main) {
                    createdVideo.setVideoPath("/storage/emulated/0/Videos/output.mp4")
                    createdVideo.requestFocus()
                    createdVideo.start()
                    end = Date().time
                    Log.i("TimeTook", (end - begin).toString())
                }
            } else {
                concatVideos()
            }
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(TAG, "Command execution cancelled by user.");
        } else {
            Log.i(TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            //Config.printLastCommandOutput(Log.INFO);
        }

    }

    private suspend fun concatVideos() {
        val command = concatVideosCommand()
        var rc = FFmpeg.execute(command)
        if (rc == RETURN_CODE_SUCCESS) {
            Log.i(TAG, "Command execution completed successfully.")
            GlobalScope.launch(Dispatchers.Main) {
                createdVideo.setVideoPath("/storage/emulated/0/Videos/final_output.mp4")
                createdVideo.requestFocus()
                createdVideo.start()
                end = Date().time
                Log.i("TimeTook", (end - begin).toString())
            }

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

        param +=  "concat=n=" + count + ":v=1,format=yuv420p[v]"
        comm += "-filter_complex"
        comm +=  param
        comm += "-map"
        comm += "[v]"
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
        return result
    }

    private fun createAndSendPayload() {
        //selectMultiple()
        Log.i("createAndSendPayload", "am intrat")
        var send = arrayOf<Payload>()
        for (element in sendImgs) {
            var pfd : ParcelFileDescriptor? = contentResolver.openFileDescriptor(element, "r");
            var filePayload = Payload.fromFile(pfd!!)
            send += filePayload

        }
        NearbyCommunication.doDiscover(send)
        Log.i("createAndSendPayload", "am iesit")

    }

    private suspend fun concatVideosCommand() : Array<String> {
        var test : File = File("/storage/emulated/0/Videos");
        if (!test.exists()) {
            if (test.mkdirs())
                Log.e("FILE", "MADE IT!!")
            else
                Log.e("FILE", "DIDNT MAKE IT!!")
        }
        var comm = arrayOf<String>()
        comm += "-i"
        comm += "/storage/emulated/0/Videos/output.mp4"
        comm += "-i"
        //comm += "/storage/emulated/0/Download/18GP.mp4"
//        while (NearbyCommunication.transferBackEndpoint == "") {
//        }
        NearbyCommunication.mutex.lock()
        comm += "/storage/emulated/0/Download/" + NearbyCommunication.transferBackEndpoint + ".mp4"
        comm += "-filter_complex"
        comm += "[0][1]concat=n=2:v=1,format=yuv420p[v]"
        comm += "-map"
        comm += "[v]"
        comm += "-y"
        comm += "/storage/emulated/0/Videos/final_output.mp4"
        NearbyCommunication.mutex.unlock()

        return comm
    }

}