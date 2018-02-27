package app.android.com.musichistory


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.realm.Realm
import io.realm.RealmResults


/**
 * Created by akash on 12/2/18.
 */

class MusicHistoryFragment: Fragment() ,IOnRecycleItemClick{
    override fun onRecycleItemClick(view: View?, position: Int) {
        val realm: Realm = Realm.getDefaultInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvMusicHistory:RecyclerView= view.findViewById(R.id.rv_music_history)
        rvMusicHistory.layoutManager = LinearLayoutManager(activity)
        val realm: Realm = Realm.getDefaultInstance()
        val list:RealmResults<SongHistory> = realm.where(SongHistory::class.java).findAll()
        rvMusicHistory.adapter= MusicRecyclerAdapter(activity!!.applicationContext,list,true,this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(activity).inflate(R.layout.fragment_music_history,null)
    }
}