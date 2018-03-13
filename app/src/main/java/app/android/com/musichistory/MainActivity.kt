package app.android.com.musichistory

import android.R.color.white
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.database.Cursor
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
import android.graphics.BitmapFactory
import android.graphics.Bitmap


const val REQUEST_PERMISSION_STORAGE: Int = 30000

class MainActivity : AppCompatActivity() {
    var viewPager: ViewPager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        viewPager = vp_pager
        tb_music.setupWithViewPager(vp_pager)
        pb_music.visibility = View.VISIBLE
        vp_pager.visibility = View.GONE



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

    private fun getSongImageIcon(albumId: String): String {
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
                            "%.2f".format((((cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))).toFloat() / (1000 * 60)))),
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


