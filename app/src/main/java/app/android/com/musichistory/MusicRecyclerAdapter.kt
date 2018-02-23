package app.android.com.musichistory

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import io.realm.RealmResults

/**
 * Created by akash bisariya on 13-02-2018.
 */
class MusicRecyclerAdapter(val context: Context, val arrayList: RealmResults<SongHistory>, val isHistory: Boolean) : RecyclerView.Adapter<MusicRecyclerAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.tvSongName.text = arrayList.get(position)!!.songName
        holder.tvArtistName.text = arrayList.get(position)!!.songArtist
        holder.tvSongDuration.text = arrayList.get(position)!!.songDuration
        holder.tvSongPlayCount.text = arrayList.get(position)!!.playCount.toString()
//        Glide.with(context)
//                .load(arrayList.get(position)!!.songImage)
//                .into(holder.ivSongImage)
        if (isHistory)
            holder.tvSongPlayCount.visibility = View.VISIBLE
        else
            holder.tvSongPlayCount.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.row_music_history, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var tvSongName = itemView!!.findViewById(R.id.tv_song_name) as TextView
        var tvArtistName = itemView!!.findViewById(R.id.tv_artist_name) as TextView
        var tvSongDuration = itemView!!.findViewById(R.id.tv_song_duration) as TextView
        var tvSongPlayCount = itemView!!.findViewById(R.id.tv_song_play_count) as TextView
        var ivSongImage = itemView!!.findViewById(R.id.iv_song_image) as ImageView

    }
}