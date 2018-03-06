package app.android.com.musichistory

import android.media.MediaPlayer

/**
 * Created by akash
 * on 5/3/18.
 */
object MusicPlayer : MediaPlayer(),MediaPlayer.OnErrorListener {
//    private lateinit var mediaPlayer:MediaPlayer
    object MusicPlayer
    override fun onError(mediaPlayer: MediaPlayer?, p1: Int, p2: Int): Boolean {
        if (mediaPlayer != null) {
            mediaPlayer.reset()

        }
        return true
    }

//    fun getMediaPlayer(): MediaPlayer {
//        if (mediaPlayer==null)
//        return MediaPlayer()
//    }

}