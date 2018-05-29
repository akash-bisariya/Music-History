package app.android.com.musichistory.CurrentQueue

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import app.android.com.musichistory.CurrentQueueRecyclerAdapter
import app.android.com.musichistory.IOnRecycleItemClick
import app.android.com.musichistory.MusicHistoryRecyclerAdapter
import app.android.com.musichistory.R
import app.android.com.musichistory.models.SongHistory
import app.android.com.musichistory.models.SongQueue
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_current_queue.*

class CurrentQueueActivity : AppCompatActivity(), IOnRecycleItemClick {
    override fun onRecycleItemLongClick(view: View?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRecycleItemClick(view: View?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var mSongQueue = ArrayList<SongHistory?>()
    lateinit var mMusicRecyclerAdapter: CurrentQueueRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_queue)
        rv_current_queue.layoutManager = LinearLayoutManager(this)
        val realmResult = Realm.getDefaultInstance().where(SongQueue::class.java).findAll()
        for (songQueue in realmResult)
            mSongQueue.add(songQueue.song)
        mMusicRecyclerAdapter = CurrentQueueRecyclerAdapter(this, mSongQueue, this)
        rv_current_queue.adapter=mMusicRecyclerAdapter


    }
}
