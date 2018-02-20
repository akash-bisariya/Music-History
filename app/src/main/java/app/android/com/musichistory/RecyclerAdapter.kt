package app.android.com.musichistory

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.row_music_history.view.*
/**
 * Created by akash bisariya on 13-02-2018.
 */
class RecyclerAdapter(val context:Context, val arrayList:ArrayList<SongHistory>): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.tvSongName.text=arrayList.get(position).songName
        holder.tvArtistName.text=arrayList.get(position).songArtist
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(context).inflate(R.layout.row_music_history,parent,false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
    {
        var tvSongName = itemView!!.findViewById(R.id.tv_song_name) as TextView
        var tvArtistName = itemView!!.findViewById(R.id.tv_artist_name) as TextView


    }
}