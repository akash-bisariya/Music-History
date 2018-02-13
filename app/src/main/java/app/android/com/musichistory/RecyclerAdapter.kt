package app.android.com.musichistory

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.row_music_history.view.*
/**
 * Created by akash bisariya on 13-02-2018.
 */
class RecyclerAdapter(val context:Context, val arrayList:ArrayList<SongHistory>): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        var tv:android.widget.TextView = holder!!.view.findViewById(R.id.tv_song_name)
        tv.text=arrayList.get(position).toString()
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
        var view:View = itemView!!


    }
}