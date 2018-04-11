package app.android.com.musichistory


import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmResults


/**
 * Created by akash
 * on 5/3/18.
 */
class MusicService : MediaBrowserServiceCompat(), MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    private val MY_MEDIA_ROOT_ID = "media_root_id"
    private val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"


    private var mMediaSession: MediaSessionCompat? = null
    private var mStateBuilder: PlaybackStateCompat.Builder? = null
    private var mPlaybackStateCompat: PlaybackStateCompat? = null
    private val mMusicPlayer: MusicPlayer = MusicPlayer
    private var mAudioManager: AudioManager? = null
    private var mMediaPlayerPause = false
    private var mAudioFocusCanDuck = false
    private var mRepeatCount = -1
    private var mSongId: String? = null
    private val MUSIC_HISTORY_NOTIFICATION_ACTION_PAUSE = "MUSIC_HISTORY_NOTIFICATION_ACTION_PAUSE"
    private val MUSIC_HISTORY_NOTIFICATION_ACTION_NEXT = "MUSIC_HISTORY_NOTIFICATION_ACTION_NEXT"
    private val MUSIC_HISTORY_NOTIFICATION_ACTION_PREVIOUS = "MUSIC_HISTORY_NOTIFICATION_ACTION_PREVIOUS"
    private val MUSIC_HISTORY_NOTIFICATION_ACTION_PLAY = "MUSIC_HISTORY_NOTIFICATION_ACTION_PLAY"
    private val MUSIC_HISTORY_ACTION_REPEAT_ALL = "MUSIC_HISTORY_ACTION_REPEAT_ALL"
    val metaDataReceiver = MediaMetadataRetriever()
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
        mPlaybackStateCompat = mStateBuilder!!.build()
        mMediaSession!!.setPlaybackState(mPlaybackStateCompat)
        mMediaSession!!.setCallback(MediaCallbacks())
        sessionToken = mMediaSession!!.sessionToken
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null) {
            if (intent.getStringExtra("songId") != null) {
                if (!(mMusicPlayer.isPlaying && intent!!.getBooleanExtra("fromFloatingButton", false))) {
                    mSongId = intent.getStringExtra("songId")
                    songData = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", intent!!.getStringExtra("songId")).findAll()
                    mMusicPlayer.stop()
                    mMusicPlayer.reset()

                    val result = mAudioManager!!.requestAudioFocus(this@MusicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        mMusicPlayer.setDataSource(songData[0]!!.songData)
                        metaDataReceiver.setDataSource(songData[0]!!.songData)
                        mMusicPlayer.prepareAsync()
                        mMusicPlayer.setOnCompletionListener(this@MusicService)
                        mMusicPlayer.setOnErrorListener(this@MusicService)
                        mMusicPlayer.setOnPreparedListener {
                            it.start()
                            mStateBuilder?.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                            mMediaSession?.setPlaybackState(mStateBuilder!!.build())
                            mMediaSession!!.isActive = true

                            val metadata: MediaMetadataCompat = MediaMetadataCompat.Builder()
                                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mSongId)
                                    .build()

                            mMediaSession!!.setMetadata(metadata)

                        }
                    }
                }
            } else {
                when (intent.action) {
                    MUSIC_HISTORY_NOTIFICATION_ACTION_PLAY -> {
                        playMediaPlayer()
                    }
                    MUSIC_HISTORY_NOTIFICATION_ACTION_PREVIOUS -> {

                    }
                    MUSIC_HISTORY_NOTIFICATION_ACTION_NEXT -> {

                    }
                    MUSIC_HISTORY_NOTIFICATION_ACTION_PAUSE -> {
                        if (mMusicPlayer.isPlaying)
                            pauseMediaPlayer()
                        else
                            playMediaPlayer()
                    }
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

//            mNotificationManager = (getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager
            mMusicPlayer.setDataSource(songData[0]!!.songData)
            mMusicPlayer.prepareAsync()
            mMusicPlayer.setOnPreparedListener {
                it.start()
//                mNotificationManager!!.notify(MUSIC_HISTORY_NOTIFICATION_ID, mNotification)
                mStateBuilder?.setState(PlaybackStateCompat.STATE_PLAYING, 0, 0.0f)
                mMediaSession?.setPlaybackState(mStateBuilder!!.build())
//                buildNotification()
            }
        }


        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            Log.d("MusicHistory onSeekTo:", "" + pos)
        }

        override fun onPrepare() {
            super.onPrepare()
        }


        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
            if (extras != null && action.equals(MUSIC_HISTORY_ACTION_REPEAT_ALL)) {
                mRepeatCount = extras.getInt("Music_History_Repeat_Count", -1)
                when (mRepeatCount) {
                    0 -> {
//                        mMusicPlayer.isLooping=true
                        Toast.makeText(applicationContext, "Song will be repeated", Toast.LENGTH_SHORT).show()
                    }
                    -1 -> {
                        mMusicPlayer.isLooping = false
                        Toast.makeText(applicationContext, "Song will not be repeated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
            pauseMediaPlayer()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        stopMusicPlayer()
    }

    fun playMediaPlayer() {
        val result = mAudioManager!!.requestAudioFocus(this@MusicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (mPlaybackStateCompat!!.state != PlaybackStateCompat.STATE_STOPPED) {
                mStateBuilder?.setState(PlaybackStateCompat.STATE_PLAYING, mMusicPlayer.currentPosition.toLong(), 1.0f)
                mMediaSession?.setPlaybackState(mStateBuilder!!.build())
                mMusicPlayer.start()
            } else {
//                val intent1 = Intent(this, MusicService::class.java)
//                intent1.putExtra("songId", intent.getStringExtra("songId"))
//                intent1.putExtra("fromFloatingButton", intent.getBooleanExtra("fromFloatingButton", false))
//                startService(intent1)
            }
        }
    }

    private fun pauseMediaPlayer() {
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
                if (mMediaPlayerPause)
                    playMediaPlayer()
                Log.d("AudioFocus", "AUDIOFOCUS_GAIN_TRANSIENT")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mMusicPlayer.isPlaying) {
                    pauseMediaPlayer()
                    mMediaPlayerPause = true
                }
                Log.d("AudioFocus", "AUDIOFOCUS_LOSS_TRANSIENT")
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mAudioFocusCanDuck = true
                Log.d("AudioFocus", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                pauseMediaPlayer()
                mMediaPlayerPause = true
                Log.d("AudioFocus", "AUDIOFOCUS_LOSS")
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!mAudioFocusCanDuck) {
                    mMusicPlayer.setDataSource(songData[0]!!.songData)
                    mMusicPlayer.prepareAsync()
                    mMusicPlayer.setOnErrorListener(this)
                    mMusicPlayer.setOnPreparedListener {
                        it.start()
                    }
                }
                mAudioFocusCanDuck = false
                Log.d("AudioFocus", "AUDIOFOCUS_GAIN")
            }
            else -> {
                stopMusicPlayer()
                mMediaPlayerPause = false
            }
        }
    }

    override fun onCompletion(p0: MediaPlayer?) {
        mRepeatCount = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(PREFERENCE_KEY_REPEAT_COUNT,-1)
        when (mRepeatCount) {
            0,1,2,3 -> {
                p0!!.start()
                mStateBuilder?.setState(PlaybackStateCompat.STATE_PLAYING, mMusicPlayer.currentPosition.toLong(), 1.0f)
                mMediaSession?.setPlaybackState(mStateBuilder!!.build())
                if(mRepeatCount==1)
                {
                    getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putInt(PREFERENCE_KEY_REPEAT_COUNT,-1).apply()

                }
                else if(mRepeatCount==2)
                    getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putInt(PREFERENCE_KEY_REPEAT_COUNT,1).apply()
                else if(mRepeatCount==3)
                    getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putInt(PREFERENCE_KEY_REPEAT_COUNT,2).apply()
            }
            -1 -> stopMusicPlayer()


        }
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        stopMusicPlayer()
        return false
    }


    /**
     * Stop and release media player and session
     */
    private fun stopMusicPlayer() {
//        mNotificationManager!!.cancel(MUSIC_HISTORY_NOTIFICATION_ID)
        mMusicPlayer.stop()
        mStateBuilder?.setState(PlaybackStateCompat.STATE_STOPPED, mMusicPlayer.currentPosition.toLong(), 1.0f)
        mMediaSession?.setPlaybackState(mStateBuilder!!.build())
        mMusicPlayer.reset()
        mMediaSession?.release()
        mAudioManager?.abandonAudioFocus(this)
    }


}