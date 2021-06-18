package com.example.rotravel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.lang.Math.abs

class GalleryItemActivity : AppCompatActivity() {
    private lateinit var video: VideoView
    private lateinit var img: ImageView
    lateinit var fireAuth: FirebaseAuth
    private lateinit var fireDB: DatabaseReference
    private lateinit var user: TextView
    private lateinit var descr: TextView
    private lateinit var items: Array<String>
    private lateinit var details : HashMap<String, ItemDetails>
    private var isVid = false
    private var pos = -1
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
        getExtras()
        getItemInfo(intent.getStringExtra("user"), intent.getStringExtra("descr"))
        if (intent.getStringExtra("video") == null) {
            video.visibility = View.INVISIBLE
            itemUrl = intent.getStringExtra("photo")
            Picasso.get().load(itemUrl).into(img)
        } else {
            isVid = true
            img.visibility = View.INVISIBLE
            itemUrl = intent.getStringExtra("video")
            val mediaController = MediaController(this)
            mediaController.setAnchorView(video)
            video.setMediaController(mediaController)

            video.setVideoPath(itemUrl)
            video.requestFocus()
            video.start()

        }
        window.decorView.setOnTouchListener(object : OnSwipeTouchListener(this@GalleryItemActivity) {
            override fun onSwipeLeft() {
                //onBackPressed()
                Log.i("onSwipeLeft", "swiped left")
                if(pos+1 >= 0 && pos+1 <= items.size - 1) {
                    pos++
                    val url = items[pos]
                    getItemInfo(details[url]!!.uid!!, details[url]!!.description!!)
                    if(isVid) {
                        img.visibility = View.INVISIBLE
                        val mediaController = MediaController(this@GalleryItemActivity)
                        mediaController.setAnchorView(video)
                        video.setMediaController(mediaController)

                        video.setVideoPath(url)
                        video.requestFocus()
                        video.start()
                    } else {
                        video.visibility = View.INVISIBLE
                        Picasso.get().load(url).into(img)
                    }
                }

            }

            override fun onSwipeRight() {
                Log.i("onSwipeRight", "swiped right")
                if(pos-1 >= 0 && pos-1 <= items.size - 1) {
                    pos--
                    val url = items[pos]
                    getItemInfo(details[url]!!.uid!!, details[url]!!.description!!)
                    if(isVid) {
                        img.visibility = View.INVISIBLE
                        val mediaController = MediaController(this@GalleryItemActivity)
                        mediaController.setAnchorView(video)
                        video.setMediaController(mediaController)

                        video.setVideoPath(url)
                        video.requestFocus()
                        video.start()
                    } else {
                        video.visibility = View.INVISIBLE
                        Picasso.get().load(url).into(img)
                    }
                }
            }
        })
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

    private fun getItemInfo(uid : String, description : String) {
        //var uid = intent.getStringExtra("user")
        fireDB.child("Users").child(uid).child("username").get()
            .addOnSuccessListener {
                if(it.exists()) {
                    user.text = it.value.toString()
                    descr.text = description
                }
            }
    }

    override fun onBackPressed() {
        if(intent.getStringExtra("video") != null) {
            var back = Intent(this, DisplayVideosActivity::class.java).apply {}
            back.putExtra("videos", intent.getSerializableExtra("videos"))
            back.putExtra("details", intent.getSerializableExtra("details"))
            startActivity(back)
        } else {
            var back = Intent(this, DisplayImgsActivity::class.java).apply {}
            back.putExtra("photos", intent.getStringArrayExtra("photos"))
            back.putExtra("details", intent.getSerializableExtra("details"))
            startActivity(back)
        }
    }

    private fun getExtras() {
        pos = intent.getIntExtra("pos", -1)
        if(intent.getStringExtra("video") != null) {
            items = intent.getSerializableExtra("videos") as Array<String>
            details =  intent.getSerializableExtra("details") as HashMap<String, ItemDetails>
        } else {
            items = intent.getSerializableExtra("photos") as Array<String>
            details =  intent.getSerializableExtra("details") as HashMap<String, ItemDetails>
        }
    }
}

internal open class OnSwipeTouchListener(c: Context?) :
        View.OnTouchListener {
    private val gestureDetector: GestureDetector
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD: Int = 100
        private val SWIPE_VELOCITY_THRESHOLD: Int = 100
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick()
            return super.onSingleTapUp(e)
        }
        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return super.onDoubleTap(e)
        }
        override fun onLongPress(e: MotionEvent) {
            onLongClick()
            super.onLongPress(e)
        }
        override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
        ): Boolean {
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(
                                    velocityX
                            ) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffX > 0) {
                            onSwipeRight()
                        }
                        else {
                            onSwipeLeft()
                        }
                    }
                }
                else {
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(
                                    velocityY
                            ) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffY < 0) {
                            onSwipeUp()
                        }
                        else {
                            onSwipeDown()
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return false
        }
    }
    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeUp() {}
    open fun onSwipeDown() {}
    private fun onClick() {}
    private fun onDoubleClick() {}
    private fun onLongClick() {}
    init {
        gestureDetector = GestureDetector(c, GestureListener())
    }
}