package com.example.rotravel

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class DisplayImgAdapter(private val photos: Array<String>, private val details : HashMap<String, ItemDetails>) : RecyclerView.Adapter<ImageItemViewHolder>() {
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
        var img = holder.imgView.findViewById(R.id.ivModel) as ImageView
        img.setOnClickListener {
            val intent = Intent(img.context, GalleryItemActivity::class.java).apply {}
            var bundle : Bundle = Bundle()
            //val key = marker.position.latitude.toString() + "&" + marker.position.longitude.toString()
            //bundle.putStringArray("videos", videoMap[key])
            bundle.putString("photo", url)
            intent.putExtras(bundle)
            intent.putExtra("descr", details[url]?.description)
            intent.putExtra("user", details[url]?.uid)
            ContextCompat.startActivity(img.context, intent, bundle)
        }
    }

    private fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }


}