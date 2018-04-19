package app.android.com.musichistory
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.realm.*
import kotlinx.android.synthetic.main.fragment_music_history.*
/**
 * Created by akash on 12/2/18.
 */

class MusicHistoryFragment : Fragment(), IOnRecycleItemClick, View.OnDragListener {
    private lateinit var list: RealmResults<SongHistory>
    private lateinit var musicHistoryRecyclerAdapter: MusicHistoryRecyclerAdapter
    private lateinit var playListAdapter: PlayListAdapter
    private lateinit var playList:RealmResults<SongQueue>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvMusicHistory: RecyclerView = view.findViewById(R.id.rv_music_history)
        rvMusicHistory.layoutManager = LinearLayoutManager(activity)
        rv_play_list.layoutManager = LinearLayoutManager(activity)
        val realm: Realm = Realm.getDefaultInstance()
        list = realm.where(SongHistory::class.java).findAll().sort("playCount", Sort.DESCENDING)
        playList = Realm.getDefaultInstance().where(SongQueue::class.java).findAll()
        musicHistoryRecyclerAdapter = MusicHistoryRecyclerAdapter(activity!!.applicationContext, list, true, this)
        rvMusicHistory.adapter = musicHistoryRecyclerAdapter
        playListAdapter = PlayListAdapter(activity!!.applicationContext,playList,this)
        rv_play_list.adapter = playListAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(activity).inflate(R.layout.fragment_music_history, null)
    }


    override fun onRecycleItemClick(view: View?, position: Int) {
        val intent = Intent(activity, MusicActivity::class.java)
        intent.putExtra(MUSIC_HISTORY_REALM_FIELD_SONG_ID, list[position]!!.songId)
        (activity as MainActivity).onRecycleItemClick(null, list[position]!!.songId.toInt())
        Realm.getDefaultInstance().executeTransaction({
            list[position]!!.playCount++
        })
        startActivityForResult(intent, 1001)
    }

    override fun onRecycleItemLongClick(view: View?, position: Int) {
        rl_play_list.visibility = View.VISIBLE
        rl_play_list.setOnDragListener(this)
        val intent = Intent()
        intent.putExtra("songId", list[position]!!.songId)
        val clipDataItem: ClipData.Item = ClipData.Item(intent)
        clipDataItem.intent.putExtra("songId", list[position]!!.songId)
        val clipData = ClipData(ClipDescription("Music", arrayOf()), clipDataItem)
        val shadow = View.DragShadowBuilder((view as ViewGroup).getChildAt(0))
        view.startDrag(clipData, shadow, null, 0)
        Realm.getDefaultInstance().executeTransaction {
            it.insert(SongQueue(list[0]!!.songId,list[position]))
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        Realm.getDefaultInstance().close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        musicHistoryRecyclerAdapter.notifyDataSetChanged()
    }

    override fun onDrag(view: View?, dragEvent: DragEvent?): Boolean {
        when (dragEvent?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                Toast.makeText(activity, "Started", Toast.LENGTH_SHORT).show()
                return true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                Toast.makeText(activity, "ENTERED", Toast.LENGTH_SHORT).show()
                return true
            }
            DragEvent.ACTION_DROP -> {
                Toast.makeText(activity, "DROP", Toast.LENGTH_SHORT).show()
                val songId:String = dragEvent.clipData.getItemAt(0).intent.extras.get("songId").toString()
//                playList.add(Realm.getDefaultInstance().where(SongQueue::class.java).equalTo("songId",songId).findAll()[0]!!.songImage)
                playListAdapter.notifyDataSetChanged()
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                Toast.makeText(activity, "ENDED", Toast.LENGTH_SHORT).show()
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                Toast.makeText(activity, "EXITED", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> {
                return true
            }
        }
    }
}