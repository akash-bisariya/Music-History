package app.android.com.musichistory


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import app.android.com.musichistory.MusicActivity.MusicActivity
import io.realm.Realm
import io.realm.RealmResults


/**
 * Created by akash on 12/2/18.
 */

class MusicListFragment : Fragment() ,IOnRecycleItemClick {
    lateinit var list:RealmResults<SongHistory>
    override fun onRecycleItemClick(view: View?, position: Int) {
        val intent = Intent(activity,MusicActivity::class.java)
        intent.putExtra("songName", list[position]!!.songName)
        intent.putExtra("songData", list[position]!!.songData)
        intent.putExtra("songDuration", list[position]!!.songDuration)
        intent.putExtra("songImage", list[position]!!.songImage)
        intent.putExtra("songArtist", list[position]!!.songArtist)
        intent.putExtra("songId", list[position]!!.songId)
        startActivity(intent)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(activity).inflate(R.layout.fragment_music_history,null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvMusicHistory:RecyclerView= view.findViewById(R.id.rv_music_history)
        rvMusicHistory.layoutManager = LinearLayoutManager(activity)
        val realm: Realm = Realm.getDefaultInstance()
        list= realm.where(SongHistory::class.java).findAll()
        rvMusicHistory.adapter= MusicRecyclerAdapter(activity!!.applicationContext,list,false,this)
    }

}