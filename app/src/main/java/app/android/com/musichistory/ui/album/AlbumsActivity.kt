package app.android.com.musichistory.ui.album

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import app.android.com.musichistory.IOnRecycleItemClick
import app.android.com.musichistory.R
import app.android.com.musichistory.adapters.AlbumsRecyclerAdapter
import app.android.com.musichistory.models.SongHistory
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_albums.*

class AlbumsActivity : AppCompatActivity(), IOnRecycleItemClick {
    private lateinit var songAlbums: RealmResults<SongHistory>
    private lateinit var adapter: AlbumsRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_albums)
        rv_albums.layoutManager = GridLayoutManager(this, 2)
        getAlbums()
//        songAlbums.addChangeListener(mAlbumsListner)

    }

    private val mAlbumsListner = RealmChangeListener<RealmResults<SongHistory>> {
        adapter = AlbumsRecyclerAdapter(this@AlbumsActivity, songAlbums, this@AlbumsActivity)
        rv_albums.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun getAlbums() {
        Realm.getDefaultInstance().where(SongHistory::class.java).sort("albumName").distinctValues("albumName")
                .findAllAsync()
                .addChangeListener(mAlbumsListner)
    }

    override fun onRecycleItemClick(view: View?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRecycleItemLongClick(view: View?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
