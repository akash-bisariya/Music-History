package app.android.com.musichistory.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.AdapterView
import android.widget.ImageView
import app.android.com.musichistory.IOnRecycleItemClick
import app.android.com.musichistory.R
import app.android.com.musichistory.models.SongHistory
import app.android.com.musichistory.models.SongQueue
import com.bumptech.glide.Glide
import io.realm.RealmResults

/**
 * Created by akash bisariya on 18-04-2018.
 */
class PlayListAdapter(val context: Context, private val playList: RealmResults<SongQueue>, private val onItemClick: IOnRecycleItemClick) : RecyclerView.Adapter<PlayListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.row_play_list, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun getItemCount() = if (playList.size > 0) playList.size else 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewHolderBind(context, playList[position]!!.song!!)
    }

    class ViewHolder(itemView: View,  var onItemClick: IOnRecycleItemClick) : RecyclerView.ViewHolder(itemView),AdapterView.OnItemClickListener {
        private val ivSongImage = itemView.findViewById(R.id.iv_play_list_song_image) as ImageView
        private lateinit var song: SongHistory

        fun viewHolderBind(context: Context, song: SongHistory) {
            this.song=song
            if (song.songImage == "") {
                Glide.with(context)
                        .load(R.drawable.music_icon)
                        .into(ivSongImage)
            } else {
                Glide.with(context)
                        .load(song.songImage)
                        .into(ivSongImage)
            }
        }
        override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            onItemClick.onRecycleItemClick(itemView,adapterPosition)
        }
    }
}