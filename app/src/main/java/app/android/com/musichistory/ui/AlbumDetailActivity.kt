package app.android.com.musichistory.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import app.android.com.musichistory.R
import app.android.com.musichistory.models.SongHistory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_album_detail.*
import kotlinx.android.synthetic.main.activity_music.*

class AlbumDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)
        intent.extras["songId"].toString()
        var songAlbum = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId",intent.extras["songId"].toString()).findFirst()
        Glide.with(applicationContext)
                .applyDefaultRequestOptions(RequestOptions()
                        .placeholder(R.drawable.music_icon)
                        .useAnimationPool(true))
                .load(songAlbum!!.songImage)
                .into(iv_album_image)
    }
}
