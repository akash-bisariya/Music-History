package app.android.com.musichistory.service


import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
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
import app.android.com.musichistory.constants.*
import app.android.com.musichistory.customViews.MusicPlayer
import app.android.com.musichistory.models.SongHistory
import app.android.com.musichistory.models.SongQueue
import app.android.com.musichistory.utils.MediaNotificationManager
import io.realm.Realm
import io.realm.RealmResults


/**
 * Created by akash
 * on 5/3/18.
 */
class MusicService : MediaBrowserServiceCompat(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    private val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
    private var mSongQueueRealmResult: RealmResults<SongQueue>? = null
    private var mMediaSession: MediaSessionCompat? = null
    private var mStateBuilder: PlaybackStateCompat.Builder? = null
    private var mPlaybackStateCompat: PlaybackStateCompat? = null
    private val mMusicPlayer: MusicPlayer = MusicPlayer
    private var mAudioManager: AudioManager? = null
    private var mMediaPlayerPause = false
    private var mAudioFocusCanDuck = false
    private var isNeededResumeOnFocusGain = false
    private var mRepeatCount = -1
    private var mSongId: String? = null
    private var mCurrentSongIndex = 0
    private lateinit var mMediaNotificationManager: MediaNotificationManager
    private val metaDataReceiver = MediaMetadataRetriever()
    private var isFromFloatingButton = false
    private lateinit var songData: SongHistory

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, Bundle())

    }


    override fun onCreate() {
        super.onCreate()
        mMediaSession = MediaSessionCompat(this, MUSIC_HISTORY_MEDIA_SESSION)
        mMediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        mMediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mStateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
        mPlaybackStateCompat = mStateBuilder!!.build()
        mMediaSession!!.setPlaybackState(mPlaybackStateCompat)
        mMediaSession!!.setCallback(MediaCallbacks())
        sessionToken = mMediaSession!!.sessionToken
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mSongQueueRealmResult = Realm.getDefaultInstance().where(SongQueue::class.java).findAll()

        if ((mSongQueueRealmResult as RealmResults<SongQueue>).size > 0) {
            songData = (mSongQueueRealmResult as RealmResults<SongQueue>)[mCurrentSongIndex]!!.song as SongHistory
        }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getStringExtra("songId") != null) {
            isFromFloatingButton = intent.getBooleanExtra("fromFloatingButton", false)
            if (!(mMusicPlayer.isPlaying && intent.getBooleanExtra("fromFloatingButton", false))) {
                mSongId = intent.getStringExtra("songId")
                songData = (Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", intent.getStringExtra("songId")).findAll()[0]) as SongHistory
                mMusicPlayer.stop()
                mMusicPlayer.reset()
                mRepeatCount = getSharedPreferences(MUSIC_HISTORY_SHARED_PREFERENCE, Context.MODE_PRIVATE).getInt(PREFERENCE_KEY_REPEAT_COUNT, -1)
                val result = mAudioManager!!.requestAudioFocus(this@MusicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mMusicPlayer.setDataSource(songData.songDataPath)
                    metaDataReceiver.setDataSource(songData.songDataPath)
                    mMusicPlayer.setOnPreparedListener(this@MusicService)
                    mMusicPlayer.prepareAsync()
                    mMusicPlayer.setOnCompletionListener(this@MusicService)
                    mMusicPlayer.setOnErrorListener(this@MusicService)
                }
            }
        }
        return Service.START_REDELIVER_INTENT
    }

    inner class MediaCallbacks : MediaSessionCompat.Callback() {
        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            return super.onMediaButtonEvent(mediaButtonEvent)
        }


        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            songData = (Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", mediaId).findAll()[0]) as SongHistory
            mMusicPlayer.stop()
            mMusicPlayer.reset()
            mMusicPlayer.setOnPreparedListener(this@MusicService)
            mMusicPlayer.setOnCompletionListener(this@MusicService)
            mMusicPlayer.setDataSource(songData.songDataPath)
            mMusicPlayer.prepareAsync()
        }


        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            Log.d("MusicHistory onSeekTo:", "" + pos)
            if (mMusicPlayer.isPlaying)
                mMusicPlayer.seekTo(pos.toInt())
        }

        override fun onPrepare() {
            super.onPrepare()
        }


        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
            if (action.equals(MUSIC_HISTORY_MUSIC_REPEAT_COUNT_CUSTOM_ACTION))
                mRepeatCount = getSharedPreferences(MUSIC_HISTORY_SHARED_PREFERENCE, Context.MODE_PRIVATE).getInt(PREFERENCE_KEY_REPEAT_COUNT, -1)
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
            handlePlayRequest(1)
        }

        override fun onSkipToPrevious() {
            handlePlayRequest(-1)
        }

        override fun onPlay() {
            playMediaPlayer()
            mMediaNotificationManager.startNotification(true, mCurrentSongIndex)
        }

        override fun onPause() {
            pauseMediaPlayer()
            mMediaNotificationManager.startNotification(true, mCurrentSongIndex)
        }
    }

    /**
     * Prepared listener for the music player
     */
    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        mediaPlayer!!.start()
        Realm.getDefaultInstance().executeTransaction {
            songData.playCount++
            it.copyToRealmOrUpdate(songData)
        }
        mStateBuilder?.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
        mMediaSession?.setPlaybackState(mStateBuilder!!.build())
        mMediaSession!!.isActive = true


        mMediaSession!!.setMetadata(MediaMetadataCompat.Builder().putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mSongId).build())
        Realm.getDefaultInstance().executeTransactionAsync {
            if (!isFromFloatingButton) {
                it.delete(SongQueue::class.java)
                it.insertOrUpdate(SongQueue(songData))
            }
            mMediaNotificationManager = MediaNotificationManager(this)
            mMediaNotificationManager.startNotification(false, mCurrentSongIndex)
        }
    }


    private fun handlePlayRequest(amount: Int) {
        mCurrentSongIndex += amount
        if (mCurrentSongIndex < 0) mCurrentSongIndex = 0 else mCurrentSongIndex %= mSongQueueRealmResult!!.size
        val extras = Bundle()
        extras.putString("currentIndex", mCurrentSongIndex.toString())
        if (amount > 0)
            mStateBuilder?.setState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, mCurrentSongIndex.toLong(), 1.0f)!!.setExtras(extras)
        else
            mStateBuilder?.setState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS, mCurrentSongIndex.toLong(), 1.0f)!!.setExtras(extras)

        mMediaSession?.setPlaybackState(mStateBuilder!!.build())
        songData = (mSongQueueRealmResult as RealmResults<SongQueue>)[mCurrentSongIndex]!!.song as SongHistory
        mMusicPlayer.reset()
        mMusicPlayer.setDataSource(songData.songDataPath)
        mMusicPlayer.setOnPreparedListener(this@MusicService)
        mMusicPlayer.setOnCompletionListener(this@MusicService)
        mMusicPlayer.prepareAsync()
    }

    fun playMediaPlayer() {
        if (getAudioFocus()) {
            if (mPlaybackStateCompat!!.state != PlaybackStateCompat.STATE_STOPPED) {
                mStateBuilder?.setState(PlaybackStateCompat.STATE_PLAYING, mMusicPlayer.currentPosition.toLong(), 1.0f)
                mMediaSession?.setPlaybackState(mStateBuilder!!.build())
                mMusicPlayer.start()
                mMediaSession!!.isActive = true
            }
        }
    }

    private fun pauseMediaPlayer() {
        mMusicPlayer.pause()
        mStateBuilder?.setState(PlaybackStateCompat.STATE_PAUSED, mMusicPlayer.currentPosition.toLong(), 1.0f)
        mMediaSession?.setPlaybackState(mStateBuilder!!.build())
        mMediaSession!!.isActive = false

    }

    private fun getAudioFocus(): Boolean = (mAudioManager!!.requestAudioFocus(this@MusicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)

    override fun onAudioFocusChange(result: Int) {
        when (result) {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                if (mMediaPlayerPause) {
                    playMediaPlayer()
                    mMediaSession!!.isActive = true
                }
                Log.d("AudioFocus", "AUDIOFOCUS_GAIN_TRANSIENT")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mMusicPlayer.isPlaying) {
                    isNeededResumeOnFocusGain = mMusicPlayer.isPlaying
                    pauseMediaPlayer()
                    mMediaPlayerPause = true
                    mMediaSession!!.isActive = false
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
                mMediaSession!!.isActive = false
                Log.d("AudioFocus", "AUDIOFOCUS_LOSS")
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!mAudioFocusCanDuck && isNeededResumeOnFocusGain) {
                    playMediaPlayer()
                    mMusicPlayer.setOnPreparedListener(this@MusicService)
                    mMusicPlayer.setOnCompletionListener(this@MusicService)
                    mMusicPlayer.setOnErrorListener(this)
                }
                mAudioFocusCanDuck = false
                isNeededResumeOnFocusGain = false
                Log.d("AudioFocus", "AUDIOFOCUS_GAIN")
            }
            else -> {
                if (mMusicPlayer.isPlaying)
                    stopMusicPlayer()
                mMediaPlayerPause = false
                mMediaSession!!.isActive = false
            }
        }
    }

    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        when (mRepeatCount) {
            MUSIC_HISTORY_SONG_REPEAT_INFINITE -> {
                if (getAudioFocus()) {
                    handlePlayRequest(1)
                }
            }
            MUSIC_HISTORY_SONG_REPEAT_TWO_TIME,
            MUSIC_HISTORY_SONG_REPEAT_THREE_TIME -> {
                if (getAudioFocus()) {
                    mediaPlayer!!.start()
                    mMediaSession!!.isActive = true
                    mStateBuilder?.setState(PlaybackStateCompat.STATE_PLAYING, mMusicPlayer.currentPosition.toLong(), 1.0f)
                    mMediaSession?.setPlaybackState(mStateBuilder!!.build())
                    mRepeatCount--
                }
            }
            MUSIC_HISTORY_SONG_REPEAT_ONE_TIME -> {
                if (getAudioFocus()) {
                    mediaPlayer?.start()
                    mMediaSession!!.isActive = true
                    mStateBuilder?.setState(PlaybackStateCompat.STATE_PLAYING, mMusicPlayer.currentPosition.toLong(), 1.0f)
                    mMediaSession?.setPlaybackState(mStateBuilder!!.build())
                    mRepeatCount = -1
                }
            }
            MUSIC_HISTORY_SONG_REPEAT_NEVER -> stopMusicPlayer()
        }
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        if (mMusicPlayer.isPlaying) stopMusicPlayer()
        Log.d("MusicHistoryError", "Error occurred - p1 $p1 p2 $p2")
        return false
    }

    /**
     * Stop and release media player and session
     */
    private fun stopMusicPlayer() {
        mMusicPlayer.stop()
        mStateBuilder?.setState(PlaybackStateCompat.STATE_STOPPED, mMusicPlayer.currentPosition.toLong(), 1.0f)
        mMediaSession?.setPlaybackState(mStateBuilder!!.build())
        mMusicPlayer.reset()
        mMediaSession!!.isActive = false
        mMediaSession?.release()
        mAudioManager?.abandonAudioFocus(this)
        try {
            mMediaNotificationManager.stopNotification()
        } catch (exp: UninitializedPropertyAccessException) {

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        stopMusicPlayer()
        Realm.getDefaultInstance().close()
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopMusicPlayer()
        Realm.getDefaultInstance().close()
        stopSelf()
    }

}