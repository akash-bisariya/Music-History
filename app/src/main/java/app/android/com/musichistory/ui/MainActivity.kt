package app.android.com.musichistory.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.*
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v7.graphics.Palette
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ImageView
import app.android.com.musichistory.IOnRecycleItemClick
import app.android.com.musichistory.adapters.PagerAdapter
import app.android.com.musichistory.R
import app.android.com.musichistory.constants.MUSIC_HISTORY_CHANGE_OBSERVE_IN_AUDIO_FILES
import app.android.com.musichistory.constants.MUSIC_HISTORY_KEY_FOR_CHANGED_AUDIO_FILE_PATH
import app.android.com.musichistory.models.SongHistory
import app.android.com.musichistory.ui.album.AlbumsActivity
import app.android.com.musichistory.utils.Utils.Companion.getCroppedBitmap
import io.gresse.hugo.vumeterlibrary.VuMeterView
import io.realm.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

const val REQUEST_PERMISSION_STORAGE: Int = 30000

class MainActivity : AppCompatActivity(), IOnRecycleItemClick, View.OnClickListener, View.OnTouchListener, NavigationView.OnNavigationItemSelectedListener {
    override fun onRecycleItemLongClick(view: View?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var dX: Float = 0.toFloat()
    private var dY: Float = 0.toFloat()
    private var downRawX: Float = 0.toFloat()
    private var downRawY: Float = 0.toFloat()
    private val clickDragTolerance = 10f
    private var mSongId: String? = null
    private var viewPager: ViewPager? = null
    private lateinit var fabMusicPlaying: VuMeterView

    private var changeFileBroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(p0: Context?, intent : Intent?) {
            if (intent != null) {
                val uri: Uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                Log.d("MusicHistory",intent.action.toString()+""+intent.extras["MUSIC_HISTORY_KEY_FOR_CHANGED_AUDIO_FILE_PATH"])
                var changedUri = Uri.fromFile(File(intent.extras[MUSIC_HISTORY_KEY_FOR_CHANGED_AUDIO_FILE_PATH].toString()))
//                AND "+MediaStore.Audio.Media.DATA +" = '"+changedUri+"'"
                val cursor =this@MainActivity.contentResolver.query(  uri, null, MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null)
//                Realm.getDefaultInstance().beginTransaction()
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    do {
                        val albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
//                        realm.insert(SongHistory(
//                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
//                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
//                                        .replace(Regex("^(-+|\\d+)"), "").trim()
//                                        .replace(Regex("^-+"), "").trim()
//                                        .replace(Regex("_+"), " "),
//                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
//                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)).replace(Regex("^-+\\d+-+"), "").trim(),
//                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
//                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)),
//                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)),
//                                getSongImageIcon(albumId),
//                                0, false))
                    } while (cursor.moveToNext())
                }
                cursor.close()
//                realm.commitTransaction()
            }
        }

    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        val action = motionEvent!!.action
        if (action == MotionEvent.ACTION_DOWN) {

            downRawX = motionEvent.rawX
            downRawY = motionEvent.rawY
            dX = view!!.x - downRawX
            dY = view.y - downRawY

            return true // Consumed

        } else if (action == MotionEvent.ACTION_MOVE) {

            val viewWidth = view!!.width
            val viewHeight = view.height

            val viewParent = view.parent as View
            val parentWidth = viewParent.width
            val parentHeight = viewParent.height

            var newX = motionEvent.rawX + dX
            newX = Math.max(0f, newX) // Don't allow the FAB past the left hand side of the parent
            newX = Math.min((parentWidth - viewWidth).toFloat(), newX) // Don't allow the FAB past the right hand side of the parent

            var newY = motionEvent.rawY + dY
            newY = Math.max(0f, newY) // Don't allow the FAB past the top of the parent
            newY = Math.min((parentHeight - viewHeight).toFloat(), newY) // Don't allow the FAB past the bottom of the parent

            view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start()

            return true // Consumed

        } else if (action == MotionEvent.ACTION_UP) {

            val upRawX = motionEvent.rawX
            val upRawY = motionEvent.rawY

            val upDX = upRawX - downRawX
            val upDY = upRawY - downRawY

            return if (Math.abs(upDX) < clickDragTolerance && Math.abs(upDY) < clickDragTolerance) { // A click
                fabMusicPlaying.performClick()
            } else { // A drag
                true // Consumed
            }

        } else {
            return super.onTouchEvent(motionEvent)
        }
    }

    override fun onRecycleItemClick(view: View?, position: Int) {
        mSongId = position.toString()
        lateinit var songData: RealmResults<SongHistory>
        fab_music_playing.visibility = View.VISIBLE
        Realm.getDefaultInstance().use {
            songData = it.where(SongHistory::class.java).equalTo("songId", "" + position).findAll()
            it.executeTransaction { it1 ->
                val result = it1.where(SongHistory::class.java).equalTo("isCurrentlyPlaying", true).findAll()
                for (music in result) {
                    music.isCurrentlyPlaying = false
                }
                songData[0]!!.isCurrentlyPlaying = true
                it1.copyToRealmOrUpdate(songData[0])
            }
        }

//        customView(fab_music_playing, songData[0]!!.songImage)
        fabMusicPlaying.customView(songData[0]!!.songImage)

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val view = layoutInflater.inflate(R.layout.music_history_title_layout, null)
        toolbar.addView(view)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        fabMusicPlaying = findViewById(R.id.fab_music_playing)
        fabMusicPlaying.setOnTouchListener(this)
        fabMusicPlaying.color = R.color.lightest_gray_transparent
        fabMusicPlaying.blockNumber = 5
        fabMusicPlaying.blockSpacing = 10F
        fabMusicPlaying.stop(true)
        viewPager = vp_pager
        tb_music.setupWithViewPager(vp_pager)
        pb_music.visibility = View.VISIBLE
        vp_pager.visibility = View.GONE
        fab_music_playing.setOnClickListener(this)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view_new.setNavigationItemSelectedListener(this)
        var vibrantColor: Int
        var vibrantDarkColor: Int
        try {
            GlobalScope.launch(Dispatchers.Default) {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.screen_home)
            Palette.from(bitmap).generate { palette ->
                vibrantColor = palette.getVibrantColor(ContextCompat.getColor(this@MainActivity, R.color.color_red))
                vibrantDarkColor = palette.getDarkVibrantColor(ContextCompat.getColor(this@MainActivity, R.color.color_red))
                collapsing_toolbar.setContentScrimColor(vibrantColor)
                collapsing_toolbar.setStatusBarScrimColor(vibrantDarkColor)

            }}

        } catch (e: Exception) {
            collapsing_toolbar.setContentScrimColor(
                    ContextCompat.getColor(this, R.color.color_red)
            )
            collapsing_toolbar.setStatusBarScrimColor(
                    ContextCompat.getColor(this, R.color.color_red)
            )
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_STORAGE)
        } else {
            GlobalScope.launch(Dispatchers.Default) {
                getSong(applicationContext)
                Log.d("MusicHistory", "CoRoutine under launch method " + Thread.currentThread().name)
            }
        }
    }




    private fun registerReceiverForFileChange() {
        val f = IntentFilter()
        f.addAction(MUSIC_HISTORY_CHANGE_OBSERVE_IN_AUDIO_FILES)
        LocalBroadcastManager.getInstance(this).registerReceiver(changeFileBroadcastReceiver,f)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.nav_albums ->
            {
                val intent = Intent(this, AlbumsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fab_music_playing -> {
                val intent = Intent(this, MusicActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.putExtra("fromFloatingButton", true)
                startActivityForResult(intent,1000)
            }
        }
    }

    /**
     * getting album art from cursor
     */
    private fun getSongImageIcon(albumId: String): String {
        //Todo Use MediaMetaData Receiver to fetch album art using embedded image
        val uri: Uri = android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val cursor: Cursor = applicationContext.contentResolver.query(uri, null, MediaStore.Audio.Albums._ID + " = " + albumId, null, null)
        cursor.moveToFirst()
        if (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)) != null) {
            val albumArt = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
            cursor.close()
            return albumArt
        } else {
            val albumArt = ""
            cursor.close()
            return albumArt
        }

    }

    /**
     * Generated Floating View for the playing song
     */
    private fun customView(v: View, imagePath: String) {

        val drawable: Drawable? = if (imagePath != "") {
            val option = BitmapFactory.Options()
            option.inSampleSize = 2
            BitmapDrawable(resources, getCroppedBitmap(BitmapFactory.decodeFile(imagePath, option)))
        } else {
            BitmapDrawable(resources, getCroppedBitmap(BitmapFactory.decodeResource(resources, R.drawable.music_icon)))
        }
        v.background = drawable
    }

    private fun getSong(context: Context) {
        val realm: Realm = Realm.getDefaultInstance()
        if (realm.where(SongHistory::class.java).findAll().count() <= 0) {
            realm.beginTransaction()
            realm.deleteAll()
            realm.commitTransaction()
            val uri: Uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val cursor: Cursor = context.contentResolver.query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " = 1" , null, null)
            realm.beginTransaction()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                    realm.insert(SongHistory(
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                                    .replace(Regex("^(-+|\\d+)"), "").trim()
                                    .replace(Regex("^-+"), "").trim()
                                    .replace(Regex("_+"), " "),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)).replace(Regex("^-+\\d+-+"), "").trim(),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)),
                            getSongImageIcon(albumId),
                            0, false))
                } while (cursor.moveToNext())
            }
            cursor.close()
            realm.commitTransaction()
        }
        GlobalScope.launch(Dispatchers.Main) {
            viewPager!!.adapter = PagerAdapter(supportFragmentManager)
            tb_music.setupWithViewPager(vp_pager)
            pb_music.visibility = View.GONE
            vp_pager.visibility = View.VISIBLE
        }
    }


    override fun onResume() {
        super.onResume()
        registerReceiverForFileChange()
        val songData: RealmResults<SongHistory> = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("isCurrentlyPlaying", true).findAll()
        if (songData.size > 0) {
            customView(fab_music_playing, songData[0]!!.songImage)
            fab_music_playing.visibility = View.VISIBLE
        } else {
            fab_music_playing.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(changeFileBroadcastReceiver)
    }


    override fun onDestroy() {
        super.onDestroy()
        Realm.getDefaultInstance().close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if(it.getBooleanExtra("isMusicPlaying",false))
                fabMusicPlaying.resume(true)
            else
                fabMusicPlaying.stop(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode == REQUEST_PERMISSION_STORAGE) {
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                GlobalScope.launch(Dispatchers.Default) {
                    getSong(applicationContext)
                    Log.d("MusicHistory", "Coroutine under launch method " + Thread.currentThread().name)
                }
            }
            else -> {
                GlobalScope.launch(Dispatchers.Main) {
                    viewPager!!.adapter = PagerAdapter(supportFragmentManager)
                    tb_music.setupWithViewPager(vp_pager)
                    pb_music.visibility = View.GONE
                    vp_pager.visibility = View.VISIBLE
                }
            }
        }
    }

}


private fun VuMeterView.customView(imagePath: String) {
    val drawable: Drawable? = if (imagePath != "") {
        val option = BitmapFactory.Options()
        option.inSampleSize = 2
        BitmapDrawable(resources, getCroppedBitmap(BitmapFactory.decodeFile(imagePath, option)))
    } else {
        BitmapDrawable(resources, getCroppedBitmap(BitmapFactory.decodeResource(resources, R.drawable.music_icon)))
    }
    this.background = drawable
}






