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
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch


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


//        for (i: Int in 1..100000)
//        {
//            Log.d("Tag","Coroutine outside launch method loop "+Thread.currentThread().name)
//        }


        Log.d("MusicHistory","Coroutine outside launch method "+Thread.currentThread().name)

    }

    fun getSong(context: Context):List<SongHistory>
    {
        val songList: ArrayList<SongHistory> = ArrayList()
        val uri: Uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor:Cursor = context.contentResolver.query(uri,null,MediaStore.Audio.Media.IS_MUSIC+ " = 1",null,null)
        if(cursor.count>0) {
            cursor.moveToFirst()
            do {
                songList.add(SongHistory(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)), cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))))
            } while (cursor.moveToNext())


        }





        launch(UI) {
            viewPager!!.adapter = PagerAdapter(supportFragmentManager,songList)
            tb_music.setupWithViewPager(vp_pager)
        }
        return songList
    }


}

class SongHistory(songName:String,songArtist:String) {

}
