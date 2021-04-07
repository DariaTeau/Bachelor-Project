package com.example.rotravel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

class DisplayImgAdapter(private val photos: Array<String>) : RecyclerView.Adapter<ImageItemViewHolder>() {
    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        val inflatedView = parent.inflate(R.layout.img_item ,false)
        return ImageItemViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        val url = photos[position]
        holder.bindPhoto(url)
    }

    private fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }


}