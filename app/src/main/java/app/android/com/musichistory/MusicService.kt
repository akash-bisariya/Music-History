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
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_music.*


/**
 * Created by akash
 * on 5/3/18.
 */
class MusicService : MediaBrowserServiceCompat(), MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    private val MY_MEDIA_ROOT_ID = "media_root_id"
    private val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
    private val MUSIC_HISTORY_NOTIFICATION_ID = 10001
    private var mMediaSession: MediaSessionCompat? = null
    private var mStateBuilder: PlaybackStateCompat.Builder? = null
    private val mMusicPlayer: MusicPlayer = MusicPlayer
    private var mNotificationManager: NotificationManager? = null
    private var audioManager: AudioManager? = null
    lateinit var mNotification: Notification
    private var pendingIntent: PendingIntent? = null
    private var mediaPlayerPause = false
    private var audioFocusCanDuck = false
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
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
        mMediaSession!!.setPlaybackState(mStateBuilder!!.build())
        mMediaSession!!.setCallback(MediaCallbacks())
        sessionToken = mMediaSession!!.sessionToken


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(!mMusicPlayer.isPlaying) {

            songData = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", intent!!.getStringExtra("songId")).findAll()
            mMusicPlayer.stop()
            mMusicPlayer.reset()
            buildNotification()
            mNotificationManager = (getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val result = audioManager!!.requestAudioFocus(this@MusicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mMusicPlayer.setDataSource(songData[0]!!.songData)
                mMusicPlayer.prepareAsync()
                mMusicPlayer.setOnErrorListener(this@MusicService)
                mMusicPlayer.setOnPreparedListener {
                    it.start()
                    mNotificationManager!!.notify(MUSIC_HISTORY_NOTIFICATION_ID, mNotification)
                    mStateBuilder?.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                    mMediaSession?.setPlaybackState(mStateBuilder!!.build())

                }
            }
        }
        return Service.START_STICKY


    }

    inner class MediaCallbacks : MediaSessionCompat.Callback() {
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
                mNotificationManager!!.notify(MUSIC_HISTORY_NOTIFICATION_ID, mNotification)
                mStateBuilder?.setState(PlaybackStateCompat.STATE_PLAYING, 0, 0.0f)
                mMediaSession?.setPlaybackState(mStateBuilder!!.build())
            }
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            Log.d("MusicHistory onSeekTo:", "" + pos)
        }

        override fun onPrepare() {
            super.onPrepare()
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

        override fun onPlay() {
            super.onPlay()
            playMediaPlayer()
        }

        override fun onPause() {
            super.onPause()
            pauseMediaplayer()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        stopMusicPlayer()
    }

    fun playMediaPlayer() {
        val result = audioManager!!.requestAudioFocus(this@MusicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mStateBuilder?.setState(PlaybackStateCompat.STATE_PLAYING, mMusicPlayer.currentPosition.toLong(), 1.0f)
            mMediaSession?.setPlaybackState(mStateBuilder!!.build())
            mMusicPlayer.start()
        }
    }

    private fun pauseMediaplayer() {
        mMusicPlayer.pause()
        mStateBuilder?.setState(PlaybackStateCompat.STATE_PAUSED, mMusicPlayer.currentPosition.toLong(), 1.0f)
        mMediaSession?.setPlaybackState(mStateBuilder!!.build())
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopMusicPlayer()
        stopSelf()
    }


    override fun onAudioFocusChange(result: Int) {

        when (result) {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                if (mediaPlayerPause)
                    playMediaPlayer()
                Log.d("AudioFocus", "AUDIOFOCUS_GAIN_TRANSIENT")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mMusicPlayer.isPlaying) {
                    pauseMediaplayer()
                    mediaPlayerPause = true
                }
                Log.d("AudioFocus", "AUDIOFOCUS_LOSS_TRANSIENT")
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                audioFocusCanDuck = true
                Log.d("AudioFocus", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                pauseMediaplayer()
                mediaPlayerPause = true
                Log.d("AudioFocus", "AUDIOFOCUS_LOSS")
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!audioFocusCanDuck) {
                    mMusicPlayer.setDataSource(songData[0]!!.songData)
                    mMusicPlayer.prepareAsync()
                    mMusicPlayer.setOnErrorListener(this)
                    mMusicPlayer.setOnPreparedListener {
                        it.start()
                    }
                }
                audioFocusCanDuck = false
                Log.d("AudioFocus", "AUDIOFOCUS_GAIN")
            }
            else -> {
                stopMusicPlayer()
                mediaPlayerPause = false
            }
        }
    }

    override fun onCompletion(p0: MediaPlayer?) {
        stopMusicPlayer()
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        stopMusicPlayer()
        return false
    }


    //Creating mediaStyle notifications
    private fun buildNotification() {
        val bmOptions = BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeFile(songData[0]?.songImage, bmOptions)
        mNotification = NotificationCompat.Builder(this@MusicService)
                .setContentTitle(songData[0]?.songName)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.music_icon)
                .setLargeIcon(bitmap)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(sessionToken)
                        .setShowActionsInCompactView(0, 1, 2))
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
                playbackAction.action = "MUSIC_HISTORY_NOTIFICATION_ACTION_PLAY"
                return PendingIntent.getService(this@MusicService, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.action = "MUSIC_HISTORY_NOTIFICATION_ACTION_PAUSE"
                return PendingIntent.getService(this@MusicService, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.action = "MUSIC_HISTORY_NOTIFICATION_ACTION_NEXT"
                return PendingIntent.getService(this@MusicService, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = "MUSIC_HISTORY_NOTIFICATION_ACTION_PREVIOUS"
                return PendingIntent.getService(this@MusicService, actionNumber, playbackAction, 0)
            }
            else -> {
                return null
            }
        }
    }


    /**
     * Stop and release media player and session
     */
    private fun stopMusicPlayer() {
        mNotificationManager!!.cancel(MUSIC_HISTORY_NOTIFICATION_ID)
        mMusicPlayer.stop()
        mMusicPlayer.reset()
        mMediaSession?.release()
        audioManager?.abandonAudioFocus(this)
    }


}