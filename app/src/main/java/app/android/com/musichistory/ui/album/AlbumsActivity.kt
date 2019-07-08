package app.android.com.musichistory.ui.album

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import app.android.com.musichistory.IOnRecycleItemClick
import app.android.com.musichistory.R
import app.android.com.musichistory.adapters.AlbumsRecyclerAdapter
import app.android.com.musichistory.models.SongHistory
import app.android.com.musichistory.ui.AlbumDetailActivity
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_albums.*
import app.android.com.musichistory.utils.Logger

class AlbumsActivity : AppCompatActivity(), IOnRecycleItemClick {
    private lateinit var songAlbums: RealmResults<SongHistory>
    private lateinit var adapter: AlbumsRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_albums)
        getAlbums()
    }

    private val mAlbumsListener = RealmChangeListener<RealmResults<SongHistory>> {
        adapter.notifyDataSetChanged()
    }

    private fun getAlbums() {
        songAlbums = Realm.getDefaultInstance().where(SongHistory::class.java).sort("albumName").distinctValues("albumName").findAllAsync()
        rv_albums.layoutManager = GridLayoutManager(this, 2)
        adapter = AlbumsRecyclerAdapter(this@AlbumsActivity, songAlbums, this@AlbumsActivity)
        rv_albums.adapter = adapter
        songAlbums.addChangeListener(mAlbumsListener)
    }

    override fun onRecycleItemClick(view: View?, position: Int) {
        var intent = Intent(this,AlbumDetailActivity::class.java)
        intent.putExtra( "songId",songAlbums[position]!!.songId)
        startActivity(intent)

    }

    override fun onRecycleItemLongClick(view: View?, position: Int) {
        Logger.d("long click on albums")
    }
}
