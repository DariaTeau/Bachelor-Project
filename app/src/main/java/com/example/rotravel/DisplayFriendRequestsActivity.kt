package com.example.rotravel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DisplayFriendRequestsActivity : AppCompatActivity() {
    private lateinit var list: RecyclerView
    private var adapter : DisplayFriendReqAdapter? = null
    lateinit var fireAuth : FirebaseAuth
    private lateinit var fireDB : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_fr_req)
        val requests = intent.getSerializableExtra("requests") as HashMap<String, String>
        list = findViewById(R.id.rvRequests)
        var arr = arrayOf<String>()
        var uids = arrayOf<String?>()
        for(key in requests.keys) {
            arr += key
            uids += requests[key]
        }
        Log.i("display friends", arr.size.toString())
        //adapter = arr?.let { DisplayFriendReqAdapter(it, uids) }
        adapter = DisplayFriendReqAdapter(arr, uids)
        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        fireAuth = Firebase.auth
        fireDB = Firebase.database("https://rotravel-14ed2-default-rtdb.europe-west1.firebasedatabase.app/").reference

    }

    class FriendRequestViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        private lateinit var name : TextView
        private lateinit var accept : ImageButton
        private lateinit var decline : ImageButton
        lateinit var fireAuth : FirebaseAuth
        private lateinit var fireDB : DatabaseReference
        fun init(friendName : String, friendUid : String) {
            fireAuth = Firebase.auth
            fireDB = Firebase.database("https://rotravel-14ed2-default-rtdb.europe-west1.firebasedatabase.app/").reference
            name = view.findViewById(R.id.tvFriendName)
            accept = view.findViewById(R.id.ibAccept)
            decline = view.findViewById(R.id.ibDecline)
            name.text = friendName
            accept.setOnClickListener {
                updateFriendList(friendUid, friendName)
                removePendingReq(friendUid, friendName)
                Log.i("addRequests", "accepted for " + name.text)
            }
            decline.setOnClickListener {
                removePendingReq(friendUid, friendName)
                Log.i("addRequests", "declined for " + name.text)
            }
        }

        private fun removePendingReq(friendUid : String, name : String) {
            fireDB.child("Users").child(fireAuth.currentUser.uid).child("Pending").child(name).removeValue()
        }
        private fun updateFriendList(friendUid : String, friendName : String) {
            fireDB.child("Users").child(fireAuth.currentUser.uid).child("Friends")
                .child(friendName).setValue(friendUid)
            fireDB.child("Users").child(fireAuth.currentUser.uid).child("username").get().addOnSuccessListener {
                if(it.exists()) {
                    fireDB.child("Users").child(friendUid).child("Friends")
                        .child(it.value.toString()).setValue(fireAuth.currentUser.uid)
                }
            }
        }
    }




}