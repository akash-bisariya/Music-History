package app.android.com.musichistory

/**
 * Created by akash
 * on 14/3/18.
 */
interface IMusicPlayerPlayback {

    fun onProgress(position:Int)

    fun onPauseMusicPlayer(position: Int)

    fun onStopMusicPlayer()

    fun onStartMusicPlayer()

}