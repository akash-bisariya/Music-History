package app.android.com.musichistory


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.android.com.musichistory.MusicActivity.MusicActivity
import io.realm.*


/**
 * Created by akash on 12/2/18.
 */

class MusicHistoryFragment: Fragment() ,IOnRecycleItemClick{
    private lateinit var list:RealmResults<SongHistory>
    private lateinit var musicHistoryRecyclerAdapter: MusicHistoryRecyclerAdapter
    override fun onRecycleItemClick(view: View?, position: Int) {

        val intent = Intent(activity, MusicActivity::class.java)
        intent.putExtra(REALM_FIELD_SONG_ID, list[position]!!.songId)
        (activity as MainActivity).onRecycleItemClick(null,list[position]!!.songId.toInt())
        Realm.getDefaultInstance().executeTransaction({
            list[position]!!.playCount++
        })

        startActivityForResult(intent,1001)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvMusicHistory:RecyclerView = view.findViewById(R.id.rv_music_history)
        rvMusicHistory.layoutManager = LinearLayoutManager(activity)
        val realm: Realm = Realm.getDefaultInstance()
        list = realm.where(SongHistory::class.java).findAll().sort("playCount",Sort.DESCENDING)
        musicHistoryRecyclerAdapter= MusicHistoryRecyclerAdapter(activity!!.applicationContext,list,true,this)
        rvMusicHistory.adapter=musicHistoryRecyclerAdapter

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(activity).inflate(R.layout.fragment_music_history,null)
    }

    override fun onDestroy() {
        super.onDestroy()
        Realm.getDefaultInstance().close()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        musicHistoryRecyclerAdapter.notifyDataSetChanged()
    }
}