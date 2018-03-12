package app.android.com.musichistory

import android.media.MediaPlayer


/**
 * Created by akash
 * on 5/3/18.
 */
object MusicPlayer : MediaPlayer(), MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    override fun onCompletion(p0: MediaPlayer?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(mediaPlayer: MediaPlayer?, p1: Int, p2: Int): Boolean {
        mediaPlayer?.reset()
        return true
    }
}