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
import kotlinx.android.synthetic.main.activity_music.*
import java.util.concurrent.TimeUnit

/**
 * Created by akash bisariya on 13-02-2018.
 */
class MusicRecyclerAdapter(val context: Context, private val arrayList: RealmResults<SongHistory>, val isHistory: Boolean, val onItemClick: IOnRecycleItemClick) : RecyclerView.Adapter<MusicRecyclerAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewHolderBind(isHistory, context, arrayList, onItemClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.row_music_history, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class ViewHolder(itemView: View?, var onItemClick: IOnRecycleItemClick) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(view: View?) {
            onItemClick.onRecycleItemClick(view, adapterPosition)
        }

        fun viewHolderBind(isHistory: Boolean, context: Context, arrayList: RealmResults<SongHistory>, listener: IOnRecycleItemClick): Unit {
            onItemClick = listener
            val tvSongName = itemView!!.findViewById(R.id.tv_song_name) as TextView
            val tvArtistName = itemView.findViewById(R.id.tv_artist_name) as TextView
            val tvSongDuration = itemView.findViewById(R.id.tv_song_duration) as TextView
            val tvSongPlayCount = itemView.findViewById(R.id.tv_song_play_count) as TextView
            val ivSongImage = itemView.findViewById(R.id.iv_song_image) as ImageView
            tvSongName.text = arrayList.get(adapterPosition)!!.songName
            tvArtistName.text = arrayList.get(adapterPosition)!!.songArtist
//            tvSongDuration.text = "%.2f".format((((arrayList[adapterPosition]!!.songDuration))).toFloat() / (1000 * 60))
            tvSongDuration.text = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((((arrayList[adapterPosition]!!.songDuration))).toLong()) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds((((arrayList[adapterPosition]!!.songDuration))).toLong()) % TimeUnit.MINUTES.toSeconds(1))
            tvSongPlayCount.text = context.resources.getQuantityString(R.plurals.numberOfTimeSongPlayed, arrayList.get(adapterPosition)!!.playCount, arrayList.get(adapterPosition)!!.playCount)
            if (arrayList[adapterPosition]!!.songImage == "") {
                Glide.with(context)
                        .load(R.drawable.music_icon)
                        .into(ivSongImage)
            } else {
                Glide.with(context)
                        .load(arrayList.get(adapterPosition)!!.songImage)
                        .into(ivSongImage)
            }
            if (isHistory)
                tvSongPlayCount.visibility = View.VISIBLE
            else
                tvSongPlayCount.visibility = View.GONE
            itemView.setOnClickListener(this)
        }
    }
}