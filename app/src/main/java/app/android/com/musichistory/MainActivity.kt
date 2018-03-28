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
import android.os.PersistableBundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View

import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import android.support.v7.graphics.Palette
import app.android.com.musichistory.MusicActivity.MusicActivity
import io.realm.RealmResults


const val REQUEST_PERMISSION_STORAGE: Int = 30000

class MainActivity : AppCompatActivity(), IOnRecycleItemClick, View.OnClickListener {
    private var mSongId: String? = null
    private var viewPager: ViewPager? = null

    override fun onRecycleItemClick(view: View?, position: Int) {
        mSongId = position.toString()
        fab_music_playing.visibility = View.VISIBLE
        var songData: RealmResults<SongHistory> = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", "" + position).findAll()
        customView(fab_music_playing,  songData[0]!!.songImage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        viewPager = vp_pager
        tb_music.setupWithViewPager(vp_pager)
        pb_music.visibility = View.VISIBLE
        vp_pager.visibility = View.GONE
        fab_music_playing.setOnClickListener(this)



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
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fab_music_playing -> {
                val intent = Intent(this, MusicActivity::class.java)
                intent.putExtra("songId", mSongId)
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


    private fun customView(v: View, imagePath: String) {
//        val shape = GradientDrawable()
//        shape.shape = GradientDrawable.OVAL
//        shape.setColor(backgroundColor)
////        shape.setStroke(3, borderColor)
        var drawable: Drawable? = null
        if (!imagePath.equals("")) {
            drawable = BitmapDrawable(resources, getCroppedBitmap(BitmapFactory.decodeFile(imagePath)))
        } else {
            drawable = BitmapDrawable(resources, getCroppedBitmap(BitmapFactory.decodeResource(resources,R.drawable.music_icon)))
        }

//        val layerDrawable= LayerDrawable(arrayOf(drawable,shape))
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
                            0))
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


    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putString("songId",mSongId)
        super.onSaveInstanceState(outState)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mSongId = savedInstanceState.getString("songId")
        }
        super.onRestoreInstanceState(savedInstanceState)
    }


    override fun onPause() {
        Log.d("MusicHistory_onBack","onBack called")
        val savedInstanceState= Bundle()
        savedInstanceState!!.putString("songId",mSongId)
        onSaveInstanceState(savedInstanceState)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if(mSongId!=null) {
            val m = MusicPlayer
            fab_music_playing.visibility = View.VISIBLE
            var songData: RealmResults<SongHistory> = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", "" + mSongId).findAll()
            customView(fab_music_playing, songData[0]!!.songImage)
            fab_music_playing.visibility = if (m.isPlaying) View.VISIBLE else View.GONE
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode == REQUEST_PERMISSION_STORAGE) {
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                launch {
                    getSong(applicationContext)
                    Log.d("MusicHistory", "Coroutine under launch method " + Thread.currentThread().name)
                }
            }
        }
    }
}


