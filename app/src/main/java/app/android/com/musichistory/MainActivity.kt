package app.android.com.musichistory

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.util.Log

import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.Serializable


class MainActivity : AppCompatActivity() {
    var viewPager: ViewPager? = null
    var tabLayout: TabLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager = vp_pager
        tb_music.setupWithViewPager(vp_pager)



        launch(UI){

            getSong(applicationContext)


            Log.d("MusicHistory","Coroutine under launch method "+Thread.currentThread().name)
        }



    }

    private fun getSong(context: Context)
    {

        val realm:Realm = Realm.getDefaultInstance()

        val songList: ArrayList<SongHistory> = ArrayList()
        realm.beginTransaction()
        realm.deleteAll()
        realm.commitTransaction()
        val uri: Uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor:Cursor = context.contentResolver.query(uri,null,MediaStore.Audio.Media.IS_MUSIC+ " = 1",null,null)
        realm.beginTransaction()
        if(cursor.count>0) {
            cursor.moveToFirst()
            do {

                realm.insert(SongHistory(
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA )),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)),
                        0))

            } while (cursor.moveToNext())
        }
        realm.commitTransaction()











        launch(UI) {
            viewPager!!.adapter = PagerAdapter(supportFragmentManager)
            tb_music.setupWithViewPager(vp_pager)
        }
    }


}


