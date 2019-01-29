package app.android.com.musichistory.CurrentQueue

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import app.android.com.musichistory.adapters.CurrentQueueRecyclerAdapter
import app.android.com.musichistory.IOnRecycleItemClick
import app.android.com.musichistory.R
import app.android.com.musichistory.models.SongQueue
import app.android.com.musichistory.utils.SwipeRemoveSong
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_current_queue.*

class CurrentQueueActivity : AppCompatActivity(), IOnRecycleItemClick {
    private var mSongQueue: RealmResults<SongQueue>? = null
    lateinit var mMusicRecyclerAdapter: CurrentQueueRecyclerAdapter
    override fun onRecycleItemLongClick(view: View?, position: Int) {
    }

    override fun onRecycleItemClick(view: View?, position: Int) {
    }

    private val listener = RealmChangeListener<RealmResults<SongQueue>> {
        mMusicRecyclerAdapter.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_queue)
        rv_current_queue.layoutManager = LinearLayoutManager(this)
        mSongQueue = Realm.getDefaultInstance().where(SongQueue::class.java).findAll()
        mMusicRecyclerAdapter = CurrentQueueRecyclerAdapter(this, mSongQueue as RealmResults<SongQueue>, this)
        mSongQueue!!.addChangeListener(listener)
        rv_current_queue.adapter = mMusicRecyclerAdapter

        val swipeHandler = object : SwipeRemoveSong(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                val result = Realm.getDefaultInstance().where(SongQueue::class.java).findAll()
                if(result.size!=1) {
                    Realm.getDefaultInstance().executeTransaction {
                        (result as RealmResults).deleteFromRealm(viewHolder!!.adapterPosition)
                    }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rv_current_queue)
    }


    override fun onDestroy() {
        super.onDestroy()
        Realm.getDefaultInstance().close()
    }
}
