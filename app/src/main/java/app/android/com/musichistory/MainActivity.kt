package app.android.com.musichistory

import android.content.Context
import android.content.Intent
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
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import android.support.v7.graphics.Palette
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ImageView
import app.android.com.musichistory.models.SongHistory
import io.realm.RealmResults

const val REQUEST_PERMISSION_STORAGE: Int = 30000

class MainActivity : AppCompatActivity(), IOnRecycleItemClick, View.OnClickListener,View.OnTouchListener, NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private var dX: Float = 0.toFloat()
    private var dY:Float = 0.toFloat()
    private var downRawX: Float = 0.toFloat()
    private var downRawY:Float = 0.toFloat()
    private val clickDragTolerance= 10f
    private var mSongId: String? = null
    private var viewPager: ViewPager? = null
    private lateinit var fabMusicPlaying: ImageView

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
        fab_music_playing.visibility = View.VISIBLE
        val songData: RealmResults<SongHistory> = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", "" + position).findAll()
        Realm.getDefaultInstance().executeTransaction({
            val result = it.where(SongHistory::class.java).equalTo("isCurrentlyPlaying", true).findAll()
            for (music in result) {
                music.isCurrentlyPlaying = false
            }
            songData[0]!!.isCurrentlyPlaying = true
            it.copyToRealmOrUpdate(songData[0])
        })
        customView(fab_music_playing, songData[0]!!.songImage)



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

        try {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.screen_home)
            Palette.from(bitmap).generate { palette ->
                val vibrantColor = palette.getVibrantColor(resources.getColor(R.color.color_red))
                val vibrantDarkColor = palette.getDarkVibrantColor(resources.getColor(R.color.color_red))
                collapsing_toolbar.setContentScrimColor(vibrantColor)
                collapsing_toolbar.setStatusBarScrimColor(vibrantDarkColor)
            }

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
            launch {
                getSong(applicationContext)
                Log.d("MusicHistory", "Coroutine under launch method " + Thread.currentThread().name)
            }
        }
    }

    override fun onRecycleItemLongClick(view: View?, position: Int) {
////        val queue = ArrayList<MediaSessionCompat.QueueItem>()
//        val songData: RealmResults<SongHistory> = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", "" + position).findAll()
//
//        Toast.makeText(this,""+(songData[0]!!.songId)+songData[0]!!.songDataPath+""+songData[0]!!.albumName, Toast.LENGTH_SHORT).show()
//        val track = MediaMetadataCompat.Builder()
//                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mSongId)
//                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,songData[0]!!.albumName)
//                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,songData[0]!!.songArtist)
//                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,songData[0]!!.songName)
//                .build()
//
//
//
//        val item = MediaSessionCompat.QueueItem(track.description,0)
////        queue.add(item)
////        (mMediaSession as MediaSessionCompat).setQueue(queue)
//        mMediaControllerCompat.queue.set(0,item)
    }

    /**
     * Created round bitmap image for floating view
     */
    private fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        paint.alpha = 180
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.width / 2.toFloat(), bitmap.height / 2f,
                bitmap.width / 2.toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fab_music_playing -> {
                val intent = Intent(this, MusicActivity::class.java)
                intent.putExtra("fromFloatingButton", true)
                startActivity(intent)
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
            option.inSampleSize=2
            BitmapDrawable(resources, getCroppedBitmap(BitmapFactory.decodeFile(imagePath,option)))
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
            val cursor: Cursor = context.contentResolver.query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null)
            realm.beginTransaction()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                    realm.insert(SongHistory(
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
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
        launch(UI) {
            viewPager!!.adapter = PagerAdapter(supportFragmentManager)
            tb_music.setupWithViewPager(vp_pager)
            pb_music.visibility = View.GONE
            vp_pager.visibility = View.VISIBLE
        }
    }


    override fun onResume() {
        super.onResume()
        val songData: RealmResults<SongHistory> = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("isCurrentlyPlaying", true).findAll()
        if (songData.size > 0) {
            customView(fab_music_playing, songData[0]!!.songImage)
            fab_music_playing.visibility = View.VISIBLE
        } else {
            fab_music_playing.visibility = View.GONE
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode == REQUEST_PERMISSION_STORAGE) {
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                launch {
                    getSong(applicationContext)
                    Log.d("MusicHistory", "Coroutine under launch method " + Thread.currentThread().name)
                }
            }
            else->
            {
                launch(UI) {
                    viewPager!!.adapter = PagerAdapter(supportFragmentManager)
                    tb_music.setupWithViewPager(vp_pager)
                    pb_music.visibility = View.GONE
                    vp_pager.visibility = View.VISIBLE
                }
            }
        }
    }
}


