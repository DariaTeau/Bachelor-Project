package com.example.rotravel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

class FriendsAdapter(private val names: Array<String>, private val uids: Array<String?>) : RecyclerView.Adapter<UserProfileActivity.FriendViewHolder>() {
    override fun getItemCount(): Int {
        return names.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProfileActivity.FriendViewHolder {
        val inflatedView = parent.inflate(R.layout.friend_request ,false)
        return UserProfileActivity.FriendViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: UserProfileActivity.FriendViewHolder, position: Int) {
        val name = names[position]
        val uid = uids[position]
        holder.init(name, uid!!)
    }

    private fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

}