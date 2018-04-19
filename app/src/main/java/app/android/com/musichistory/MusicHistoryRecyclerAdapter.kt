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
import java.util.concurrent.TimeUnit

/**
 * Created by akash bisariya on 13-02-2018.
 */
class MusicHistoryRecyclerAdapter(val context: Context, private val arrayList: RealmResults<SongHistory>, private val isHistory: Boolean, private val onItemClick: IOnRecycleItemClick) : RecyclerView.Adapter<MusicHistoryRecyclerAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewHolderBind(isHistory, context, arrayList[position], onItemClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.row_music_history, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun getItemCount(): Int {
        return if (arrayList.size > 0) arrayList.size else 0
    }

    class ViewHolder(itemView: View?, private var onItemClick: IOnRecycleItemClick) : RecyclerView.ViewHolder(itemView), View.OnClickListener,View.OnLongClickListener {
        private val tvSongName = itemView!!.findViewById(R.id.tv_song_name) as TextView
        private val tvArtistName = itemView!!.findViewById(R.id.tv_artist_name) as TextView
        private val tvSongDuration = itemView!!.findViewById(R.id.tv_list_song_duration) as TextView
        private val tvSongPlayCount = itemView!!.findViewById(R.id.tv_song_play_count) as TextView
        private val ivSongImage = itemView!!.findViewById(R.id.iv_song_image) as ImageView

        override fun onClick(view: View?) {
            onItemClick.onRecycleItemClick(view, adapterPosition)
        }

        override fun onLongClick(view: View?): Boolean {
            onItemClick.onRecycleItemLongClick(view,adapterPosition)
            return true
        }

        fun viewHolderBind(isHistory: Boolean, context: Context, songInfo: SongHistory?, listener: IOnRecycleItemClick): Unit {
            onItemClick = listener
            tvSongName.text = songInfo!!.songName
            tvArtistName.text = songInfo.songArtist
            tvSongDuration.text = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((((songInfo.songDuration))).toLong()) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds((((songInfo.songDuration))).toLong()) % TimeUnit.MINUTES.toSeconds(1))
            tvSongPlayCount.text = context.resources.getQuantityString(R.plurals.numberOfTimeSongPlayed, songInfo.playCount, songInfo.playCount)
            if (songInfo.songImage == "") {
                Glide.with(context)
                        .load(R.drawable.music_icon)
                        .into(ivSongImage)
            } else {
                Glide.with(context)
                        .load(songInfo.songImage)
                        .into(ivSongImage)
            }
            tvSongPlayCount.visibility = if (isHistory) View.VISIBLE else View.GONE
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
    }
}