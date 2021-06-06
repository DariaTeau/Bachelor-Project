package com.example.rotravel

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class DisplayVideosAdapter(private val videos: Array<String>, private val details : HashMap<String, ItemDetails>) : RecyclerView.Adapter<VideoItemViewHolder>() {
    override fun getItemCount(): Int {
        return videos.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {
        val inflatedView = parent.inflate(R.layout.video_item ,false)
        return VideoItemViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
        val url = videos[position]
        holder.bindVideo(url)
        var vid = holder.videoView.findViewById(R.id.vvModel) as VideoView
        vid.setOnClickListener {
            val intent = Intent(vid.context, GalleryItemActivity::class.java).apply {}
            var bundle : Bundle = Bundle()
            //val key = marker.position.latitude.toString() + "&" + marker.position.longitude.toString()
            //bundle.putStringArray("videos", videoMap[key])
            bundle.putString("video", url)
            intent.putExtras(bundle)
            intent.putExtra("descr", details[url]?.description)
            intent.putExtra("user", details[url]?.uid)
            startActivity(vid.context, intent, bundle)
        }
    }

    private fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }
}