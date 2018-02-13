package app.android.com.musichistory


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_music_history.*

/**
 * Created by akash on 12/2/18.
 */

class MusicHistoryFragment: Fragment() {
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val view: View = LayoutInflater.from(activity).inflate(R.layout.fragment_music_history,null)
        rv_music_history.layoutManager = LinearLayoutManager(activity)
        val list:ArrayList<SongHistory> = arguments.getSerializable("SongsList") as ArrayList<SongHistory>
// arguments.getSerializable("SongsList")
        val rv_music_history:RecyclerView=view.findViewById(R.id.rv_music_history)
        rv_music_history.adapter=RecyclerAdapter(activity,list)
        super.onCreate(savedInstanceState)
    }


    override  fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}