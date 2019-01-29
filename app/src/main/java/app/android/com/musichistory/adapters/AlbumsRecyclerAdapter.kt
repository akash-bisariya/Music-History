package app.android.com.musichistory.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import app.android.com.musichistory.IOnRecycleItemClick
import app.android.com.musichistory.R
import com.bumptech.glide.Glide
import io.realm.RealmResults
import app.android.com.musichistory.models.SongHistory
import java.lang.Exception


/**
 * Created by akash bisariya on 13-02-2018.
 */
class AlbumsRecyclerAdapter(val context: Context, private val arrayList: RealmResults<SongHistory>, private val onItemClick: IOnRecycleItemClick) : RecyclerView.Adapter<AlbumsRecyclerAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewHolderBind(context, arrayList[position], onItemClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.row_albums, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun getItemCount(): Int {
        return if (arrayList.size > 0) arrayList.size else 0
    }

    class ViewHolder(itemView: View?, private var onItemClick: IOnRecycleItemClick) : RecyclerView.ViewHolder(itemView), View.OnClickListener,View.OnLongClickListener {
        private val ivAlbumImage = itemView!!.findViewById(R.id.iv_album_image) as ImageView
        private val tvAlbumName = itemView!!.findViewById(R.id.tv_album_name) as TextView
        override fun onClick(view: View?) {
            onItemClick.onRecycleItemClick(view, adapterPosition)
        }

        override fun onLongClick(view: View?): Boolean {
            onItemClick.onRecycleItemLongClick(view,adapterPosition)
            return true
        }

        fun viewHolderBind( context: Context, songInfo: SongHistory?, listener: IOnRecycleItemClick) {
            onItemClick = listener
            tvAlbumName.text= songInfo!!.albumName
            val songHistory = songInfo.songImage
            if (songHistory == "") {
                Glide.with(context)
                        .load(R.drawable.music_icon)
                        .into(ivAlbumImage)
            } else {
                try {
                    Glide.with(context)
                            .load(songHistory)
                            .into(ivAlbumImage)
                }
                catch (exp:Exception)
                {
                    Glide.with(context)
                            .load(R.drawable.music_icon)
                            .into(ivAlbumImage)
                }
            }
            itemView.setOnClickListener(this)
//            itemView.setOnLongClickListener(this)
        }
    }
}