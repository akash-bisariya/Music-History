package app.android.com.musichistory

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import io.realm.RealmResults

/**
 * Created by akash bisariya on 18-04-2018.
 */
class PlayListAdapter(val context: Context, private val playList: RealmResults<SongQueue>, private val onItemClick: IOnRecycleItemClick) : View.OnDragListener, RecyclerView.Adapter<PlayListAdapter.ViewHolder>() {
    override fun onDrag(view: View?, dragEvent: DragEvent?): Boolean {
        when (dragEvent?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                Log.d("ACTION_DRAG_ENDED","DRAG_ENDED")
                return true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                Log.d("ACTION_DRAG_ENDED","DRAG_ENDED")
                return true
            }
            DragEvent.ACTION_DROP -> {
                Log.d("ACTION_DRAG_ENDED","DRAG_ENDED")
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                Log.d("ACTION_DRAG_ENDED","DRAG_ENDED")
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                Log.d("ACTION_DRAG_EXITED","DRAG_EXITED")
                return true
            }
            else -> return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.row_play_list, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun getItemCount() = if (playList.size > 0) playList.size else 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewHolderBind(context, playList[position]!!.song!!, onItemClick)
    }

    class ViewHolder(itemView: View, private var onItemClick: IOnRecycleItemClick) : RecyclerView.ViewHolder(itemView),View.OnTouchListener {
        private val ivSongImage = itemView.findViewById(R.id.iv_play_list_song_image) as ImageView
        private lateinit var song:SongHistory
        override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
            if(motionEvent!!.action== MotionEvent.ACTION_DOWN) {
                onItemClick.onRecycleItemTouch(view, motionEvent,song.songId)
            }
            return true
        }

        fun viewHolderBind(context: Context, song: SongHistory, listener: IOnRecycleItemClick) {
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
            itemView.setOnTouchListener(this)
        }
    }
}