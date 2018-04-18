package app.android.com.musichistory

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import io.realm.RealmResults

/**
 * Created by akash bisariya on 18-04-2018.
 */
class PlayListAdapter(val context: Context, private val playList: ArrayList<String>, private val onItemClick: IOnRecycleItemClick):View.OnDragListener, RecyclerView.Adapter<PlayListAdapter.ViewHolder>()
{
    override fun onDrag(view: View?, dragEvent: DragEvent?): Boolean {
            when (dragEvent?.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    Toast.makeText(context, "Started", Toast.LENGTH_SHORT).show()
                    return true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    Toast.makeText(context, "ENTERED", Toast.LENGTH_SHORT).show()
                    return true
                }
                DragEvent.ACTION_DROP -> {
                    Toast.makeText(context, "DROP", Toast.LENGTH_SHORT).show()
                    return true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    Toast.makeText(context, "ENDED", Toast.LENGTH_SHORT).show()
                    return true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    Toast.makeText(context, "EXITED", Toast.LENGTH_SHORT).show()
                    return true
                }
                else -> {
                    return true
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.row_play_list, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun getItemCount(): Int {
        return if(playList.size>0) playList.size else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewHolderBind(context,playList[position],onItemClick)
    }

    class ViewHolder(itemView:View,private var onItemClick: IOnRecycleItemClick) :RecyclerView.ViewHolder(itemView)
    {
        private val ivSongImage = itemView.findViewById(R.id.iv_play_list_song_image) as ImageView
        fun viewHolderBind(context: Context,songImage:String,listener:IOnRecycleItemClick)
        {
            if (songImage == "") {
                Glide.with(context)
                        .load(R.drawable.music_icon)
                        .into(ivSongImage)
            } else {
                Glide.with(context)
                        .load(songImage)
                        .into(ivSongImage)
            }        }
    }
}