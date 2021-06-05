package com.example.rotravel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

class DisplayFriendReqAdapter(private val names: Array<String>, private val uids: Array<String?>) : RecyclerView.Adapter<DisplayFriendRequestsActivity.FriendRequestViewHolder>() {
    override fun getItemCount(): Int {
        return names.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisplayFriendRequestsActivity.FriendRequestViewHolder {
        val inflatedView = parent.inflate(R.layout.friend_request ,false)
        return DisplayFriendRequestsActivity.FriendRequestViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: DisplayFriendRequestsActivity.FriendRequestViewHolder, position: Int) {
        val name = names[position]
        val uid = uids[position]
        holder.init(name, uid!!)
    }

    private fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

}