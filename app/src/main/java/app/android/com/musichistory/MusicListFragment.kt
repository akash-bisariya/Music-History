package app.android.com.musichistory

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_music_history.*


/**
 * Created by akash on 12/2/18.
 */

class MusicListFragment : Fragment(), IOnRecycleItemClick, View.OnDragListener {

    private lateinit var list: RealmResults<SongHistory>
    private lateinit var playListAdapter: PlayListAdapter
    private lateinit var playList: RealmResults<SongQueue>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(activity).inflate(R.layout.fragment_music_history, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_music_history.layoutManager = LinearLayoutManager(activity)
        rv_play_list.layoutManager = LinearLayoutManager(activity)
        val realm: Realm = Realm.getDefaultInstance()
        list = realm.where(SongHistory::class.java).findAll().sort(MUSIC_HISTORY_REALM_FIELD_SONG_NAME, Sort.ASCENDING)
        rv_music_history.adapter = MusicRecyclerAdapter(activity!!.applicationContext, list, false, this)
        playList = Realm.getDefaultInstance().where(SongQueue::class.java).findAll()
        playListAdapter = PlayListAdapter(activity!!.applicationContext, playList, this)
        rv_play_list.adapter = playListAdapter
        if (playList.size > 0) rl_play_list.visibility = View.VISIBLE
    }


    override fun onDrag(view: View?, dragEvent: DragEvent?): Boolean {
        when (dragEvent?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                Log.d("ACTION_DRAG_STARTED", "DRAG_STARTED")
                return true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                Log.d("ACTION_DRAG_ENTERED", "DRAG_ENTERED")
                return true
            }
            DragEvent.ACTION_DROP -> {
                val position: String = dragEvent.clipData.getItemAt(0).intent.extras.get("position").toString()
                Realm.getDefaultInstance().executeTransaction {
                    it.insert(SongQueue(list[position.toInt()]!!.songId, list[position.toInt()]))
                    playListAdapter.notifyDataSetChanged()
                }
                (activity as MainActivity).onRecycleItemLongClick(null, list[(dragEvent.clipData.getItemAt(0).intent.extras.get("position")) as Int]!!.songId.toInt())
                Log.d("ACTION_DROP", "DROP")



                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                Log.d("ACTION_DRAG_ENDED", "DRAG_ENDED")
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                Log.d("ACTION_DRAG_EXITED", "DRAG_EXITED")
                return true
            }
            else -> return true
        }
    }

    override fun onRecycleItemClick(view: View?, position: Int) {
        (activity as MainActivity).onRecycleItemClick(null, list[position]!!.songId.toInt())
        val intent = Intent(activity, MusicActivity::class.java)
        intent.putExtra(MUSIC_HISTORY_REALM_FIELD_SONG_ID, list[position]!!.songId)
        Realm.getDefaultInstance().executeTransaction({
            list[position]!!.playCount++
        })
        startActivity(intent)
    }


    override fun onRecycleItemLongClick(view: View?, position: Int) {
        rl_play_list.visibility = View.VISIBLE
        rl_play_list.setOnDragListener(this)
        val intent = Intent()
        intent.putExtra("position", position)
        val clipDataItem: ClipData.Item = ClipData.Item(intent)
        val clipData = ClipData(ClipDescription("Music", arrayOf()), clipDataItem)
        val shadow = View.DragShadowBuilder((view as ViewGroup).getChildAt(0))
        view.startDrag(clipData, shadow, null, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        Realm.getDefaultInstance().close()
    }


}