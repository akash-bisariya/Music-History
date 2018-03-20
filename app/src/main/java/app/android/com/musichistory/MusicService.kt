package app.android.com.musichistory


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.realm.Realm
import io.realm.RealmResults


/**
 * Created by akash
 * on 5/3/18.
 */
class MusicService : MediaBrowserServiceCompat(),MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener {
    override fun onCompletion(p0: MediaPlayer?) {
        mMusicPlayer.stop()
        mMusicPlayer.reset()
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val MY_MEDIA_ROOT_ID = "media_root_id"
    private val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
    private val MUSIC_HISTORY_NOTIFICATION_ID=10001
    private var mMediaSession: MediaSessionCompat? = null
    private var mStateBuilder: PlaybackStateCompat.Builder? = null
    private val mMusicPlayer: MusicPlayer = MusicPlayer
    private  var mNotificationManager: NotificationManager? = null
    lateinit var mNotification: Notification
    private var pendingIntent:PendingIntent?=null
    private lateinit var songData: RealmResults<SongHistory>
    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, Bundle())

    }


    override fun onCreate() {
        super.onCreate()
        mMediaSession = MediaSessionCompat(this, "MusicHistoryMediaSession")
        mMediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        mMediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mStateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)

        mMediaSession!!.setPlaybackState(mStateBuilder!!.build())
        mMediaSession!!.setCallback(MediaCallbacks())
        sessionToken = mMediaSession!!.sessionToken




    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        songData = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", intent!!.getStringExtra("songId")).findAll()
        mMusicPlayer.stop()
        mMusicPlayer.reset()

        buildNotification()
        mNotificationManager = (getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager
        mMusicPlayer.setDataSource(songData[0]!!.songData)
        mMusicPlayer.prepareAsync()
        mMusicPlayer.setOnErrorListener(this@MusicService)
        mMusicPlayer.setOnPreparedListener {
            it.start()
            mNotificationManager!!.notify(MUSIC_HISTORY_NOTIFICATION_ID,mNotification)
        }
        return Service.START_STICKY



    }

    inner class MediaCallbacks : MediaSessionCompat.Callback() {


        //Creating mediaStyle notifications


        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            return super.onMediaButtonEvent(mediaButtonEvent)
        }


        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            songData = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", mediaId).findAll()
            mMusicPlayer.stop()
            mMusicPlayer.reset()
            buildNotification()
            mNotificationManager = (getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager
            mMusicPlayer.setDataSource(songData[0]!!.songData)
            mMusicPlayer.prepareAsync()
            mMusicPlayer.setOnErrorListener(this@MusicService)
            mMusicPlayer.setOnPreparedListener {
                it.start()
                mNotificationManager!!.notify(MUSIC_HISTORY_NOTIFICATION_ID,mNotification)
//                iv_play_pause.setImageResource(R.drawable.ic_pause_circle_filled_red_400_48dp)
//                seek_bar.progress = 0
//                seek_bar.max = songData[0]!!.songDuration.toInt()
//                mTimer.scheduleAtFixedRate(object : TimerTask() {
//                    override fun run() {
//                        seek_bar.progress = mMusicPlayer.currentPosition
//                        onProgress(mMusicPlayer.currentPosition)
//                        Log.e("onProgress","Progress"+mMusicPlayer.currentPosition)
//                    }
//                }, 1000, 1000)
            }
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
        }

        override fun onPrepare() {
            super.onPrepare()
        }




        override fun onPlay() {
            super.onPlay()
        }

        override fun onStop() {
            super.onStop()
        }

        override fun onSkipToQueueItem(id: Long) {
            super.onSkipToQueueItem(id)
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
            super.onRemoveQueueItem(description)
        }

        override fun onRemoveQueueItemAt(index: Int) {
            super.onRemoveQueueItemAt(index)
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
        }

        override fun onPause() {
            super.onPause()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mMediaSession?.release()
        mMusicPlayer.stop()
        mMusicPlayer.release()
        mNotificationManager?.cancel(MUSIC_HISTORY_NOTIFICATION_ID)
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        mNotificationManager!!.cancel(MUSIC_HISTORY_NOTIFICATION_ID)
        mMediaSession?.release()
        mMusicPlayer.stop()
        mMusicPlayer.release()
        stopSelf()
    }


    private fun buildNotification()
    {
        val bmOptions = BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeFile(songData[0]?.songImage, bmOptions)
        mNotification = NotificationCompat.Builder(this@MusicService)
                .setContentTitle("MusicHistory")
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.music_icon)
                .setLargeIcon(bitmap)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(sessionToken)
                        .setShowActionsInCompactView(0,1,2))
                .setContentText(songData[0]?.songArtist)
                .setContentInfo(songData[0]?.songName)
                .addAction(R.drawable.ic_skip_previous_red_400_48dp, "previous", playbackAction(3))
                .addAction(R.drawable.ic_pause_circle_filled_red_400_48dp, "pause", playbackAction(1))
                .addAction(R.drawable.ic_skip_next_red_400_48dp, "next", playbackAction(2))
                .build()
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this@MusicService, MusicService::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.setAction("ACTION_PLAY")
                return PendingIntent.getService(this@MusicService, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.setAction("ACTION_PAUSE")
                return PendingIntent.getService(this@MusicService, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.setAction("ACTION_NEXT")
                return PendingIntent.getService(this@MusicService, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.setAction("ACTION_PREVIOUS")
                return PendingIntent.getService(this@MusicService, actionNumber, playbackAction, 0)
            }
            else -> {
            }
        }
        return null
    }

}