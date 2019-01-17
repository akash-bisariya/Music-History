package app.android.com.musichistory.ui

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_music.*
import android.os.Handler
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v7.graphics.Palette
import android.view.View
import io.realm.Realm
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.text.format.DateUtils
import android.util.Log
import android.widget.SeekBar
import app.android.com.musichistory.CurrentQueue.CurrentQueueActivity
import app.android.com.musichistory.service.MusicService
import app.android.com.musichistory.R
import app.android.com.musichistory.constants.*
import app.android.com.musichistory.customViews.MusicPlayer
import app.android.com.musichistory.models.SongHistory
import app.android.com.musichistory.models.SongQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


/**
 * Created by akash
 * on 22/2/18.
 */
class MusicActivity : AppCompatActivity(), View.OnClickListener, AudioManager.OnAudioFocusChangeListener {
    private var mScheduleFuture: ScheduledFuture<*>? = null
    private var mediaPlayerPause = false
    private var audioFocusCanDuck = false
    private var repeatCount: Int = -1
    private lateinit var songData: SongHistory
    private val musicPlayer: MusicPlayer = MusicPlayer
    private var mMediaBrowserCompat: MediaBrowserCompat? = null
    private var playbackStateCompat: PlaybackStateCompat? = null
    private val mHandler = Handler()
    private lateinit var mNotification: Notification
    var mMediaControllerCompat: MediaControllerCompat? = null
    private var mNotificationManager: NotificationManager? = null
    private val mExecutorService = Executors.newSingleThreadScheduledExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        mNotificationManager = (getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager
        setData()
        iv_like.setOnClickListener(this)
        iv_play_pause.setOnClickListener(this)
        iv_next.setOnClickListener(this)
        iv_previous.setOnClickListener(this)
        iv_repeat.setOnClickListener(this)
        iv_back.setOnClickListener(this)
        iv_play_list.setOnClickListener(this)
        mMediaBrowserCompat = MediaBrowserCompat(this, ComponentName(this, MusicService::class.java), mMediaBrowserCompatConnectionCallback, null)
        repeatCount = getSharedPreferences(MUSIC_HISTORY_SHARED_PREFERENCE, Context.MODE_PRIVATE).getInt(PREFERENCE_KEY_REPEAT_COUNT, -1)

        when (repeatCount) {
            MUSIC_HISTORY_SONG_REPEAT_INFINITE -> {
                iv_repeat.setImageResource(R.drawable.ic_repeat_red_400_36dp)
                tv_repeat_count.text = ""
            }
            MUSIC_HISTORY_SONG_REPEAT_NEVER -> {
                iv_repeat.setImageResource(R.drawable.ic_repeat_grey_400_36dp)
                tv_repeat_count.text = ""
            }
            MUSIC_HISTORY_SONG_REPEAT_ONE_TIME -> {
                iv_repeat.setImageResource(R.drawable.ic_repeat_red_400_36dp)
                tv_repeat_count.text = "1"
            }
            MUSIC_HISTORY_SONG_REPEAT_TWO_TIME -> {
                iv_repeat.setImageResource(R.drawable.ic_repeat_red_400_36dp)
                tv_repeat_count.text = "2"
            }
            MUSIC_HISTORY_SONG_REPEAT_THREE_TIME -> {
                iv_repeat.setImageResource(R.drawable.ic_repeat_red_400_36dp)
                tv_repeat_count.text = "3"
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if(mMediaBrowserCompat!=null)
        mMediaBrowserCompat!!.connect()

    }


    override fun onStop() {
        super.onStop()
        if(mMediaBrowserCompat!=null)
            mMediaBrowserCompat!!.disconnect()
        var controllerCompat = MediaControllerCompat.getMediaController(this@MusicActivity);
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mMediaControllerCompatCallback)
        }

    }

    private fun setData() {
        if (intent.getStringExtra("songId") == null || intent.getStringExtra("songId") == "")
            songData = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("isCurrentlyPlaying", true).findAll()[0] as SongHistory
        else {
            songData = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", intent.getStringExtra("songId")).findAll()[0] as SongHistory
        }
        Glide.with(applicationContext)
                .applyDefaultRequestOptions(RequestOptions()
                        .placeholder(R.drawable.music_icon)
                        .useAnimationPool(true))
                .load(songData.songImage)
                .into(iv_song_image)

        tv_song_artist.text = songData.songArtist
        tv_song_current_position.text = getString(R.string.txt_initial_position_media_player)
        tv_song_name.text = songData.songName
        tv_song_album.text = songData.albumName
        tv_song_play_count.text = songData.playCount.toString()
        seek_bar.max = songData.songDuration.toInt()

        var bitmap: Bitmap?
        val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
        if (!(songData.songImage).equals("")) {
            bitmap = BitmapFactory.decodeFile(songData.songImage, bmOptions)
            bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
            Palette.from(bitmap).generate {
                music_constraint_layout.background = getGradientDrawable(getTopColor(it), getCenterLightColor(it), getBottomDarkColor(it))
            }
        } else {
            music_constraint_layout.background = getGradientDrawable(R.color.color_red, R.color.color_red, android.R.color.white)
        }

        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tv_song_current_position.text = DateUtils.formatElapsedTime((progress / 1000).toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                stopSeekbarUpdate()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                MediaControllerCompat.getMediaController(this@MusicActivity).transportControls.seekTo(seekBar.progress.toLong())
                scheduleSeekbarUpdate()
            }
        })
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.iv_play_pause -> {
                    if (musicPlayer.isPlaying) {
                        MediaControllerCompat.getMediaController(this@MusicActivity).transportControls.pause()
                        stopSeekbarUpdate()
                    } else {
                        if (MediaControllerCompat.getMediaController(this@MusicActivity).playbackState.state == STATE_STOPPED) {
                            startServiceToPlay()
                            scheduleSeekbarUpdate()
                        } else {
                            MediaControllerCompat.getMediaController(this@MusicActivity).transportControls.play()
                            scheduleSeekbarUpdate()
                        }
                    }
                }

                R.id.iv_back -> onBackPressed()

                R.id.iv_like -> {
                    iv_like.setImageResource(R.drawable.ic_thumb_up_red_400_36dp)
                }
                R.id.iv_repeat -> {
                    if (repeatCount == -1) {
                        iv_repeat.setImageResource(R.drawable.ic_repeat_red_400_36dp)
                        tv_repeat_count.text = ""
                        repeatCount++
                    } else {
                        repeatCount++
                        tv_repeat_count.text = (repeatCount).toString()
                        if (repeatCount > 3) {
                            repeatCount = -1
                            tv_repeat_count.text = ""
                            iv_repeat.setImageResource(R.drawable.ic_repeat_grey_400_36dp)
                        }
                    }


                    val preference: SharedPreferences = getSharedPreferences(MUSIC_HISTORY_SHARED_PREFERENCE, Context.MODE_PRIVATE)
                    preference.edit().putInt(PREFERENCE_KEY_REPEAT_COUNT, repeatCount).apply()

                    MediaControllerCompat.getMediaController(this@MusicActivity).transportControls.sendCustomAction(PlaybackStateCompat.CustomAction.Builder(MUSIC_HISTORY_MUSIC_REPEAT_COUNT_CUSTOM_ACTION, "REPEAT_SONG", 1).build(), null)
                }

                R.id.iv_next -> {
                    MediaControllerCompat.getMediaController(this@MusicActivity).transportControls.skipToNext()
                    stopSeekbarUpdate()
                }

                R.id.iv_previous -> {
                    MediaControllerCompat.getMediaController(this@MusicActivity).transportControls.skipToPrevious()
                    stopSeekbarUpdate()
                }

                R.id.iv_play_list -> {
                    startActivity(Intent(this@MusicActivity,CurrentQueueActivity::class.java))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSeekbarUpdate()
        Realm.getDefaultInstance().close()
    }

    private fun getGradientDrawable(topColor: Int, centerColor: Int, bottomColor: Int): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.orientation = GradientDrawable.Orientation.TOP_BOTTOM
        gradientDrawable.colors = intArrayOf(topColor, centerColor, bottomColor)
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        return gradientDrawable
    }


    private fun getTopColor(palette: Palette): Int {
        return if (palette.mutedSwatch != null || palette.vibrantSwatch != null)
            when (palette.mutedSwatch) {
                null -> palette.vibrantSwatch!!.rgb
                else -> palette.mutedSwatch!!.rgb
            }
        else Color.RED

    }


    private fun getCenterLightColor(palette: Palette): Int {
        return if (palette.lightMutedSwatch != null || palette.lightVibrantSwatch != null)
            when (palette.lightMutedSwatch) {
                null -> palette.lightVibrantSwatch!!.rgb
                else -> palette.lightMutedSwatch!!.rgb
            }
        else Color.GREEN

    }


    private fun getBottomDarkColor(palette: Palette): Int {
        return if (palette.darkMutedSwatch != null || palette.darkVibrantSwatch != null)
            when (palette.darkMutedSwatch) {
                null -> palette.darkVibrantSwatch!!.rgb
                else -> palette.darkMutedSwatch!!.rgb
            }
        else Color.BLUE
    }

    override fun onAudioFocusChange(result: Int) {
        when (result) {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                if (mediaPlayerPause)
                    musicPlayer.start()
                Log.d("AudioFocus", "AUDIOFOCUS_GAIN_TRANSIENT")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (musicPlayer.isPlaying) {
                    musicPlayer.pause()
                    mediaPlayerPause = true
                }
                Log.d("AudioFocus", "AUDIOFOCUS_LOSS_TRANSIENT")
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                audioFocusCanDuck = true
                Log.d("AudioFocus", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                musicPlayer.reset()
                mediaPlayerPause = false
                iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)
                Log.d("AudioFocus", "AUDIOFOCUS_LOSS")
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!audioFocusCanDuck) {
                    musicPlayer.setDataSource(songData.songDataPath)
                    musicPlayer.prepareAsync()
//                    musicPlayer.setOnErrorListener(this)
                    musicPlayer.setOnPreparedListener {
                        it.start()
                        iv_play_pause.setImageResource(R.drawable.ic_pause_circle_filled_red_400_48dp)
                    }
                }
                audioFocusCanDuck = false
                Log.d("AudioFocus", "AUDIOFOCUS_GAIN")
            }
            else -> {
                musicPlayer.reset()
                mediaPlayerPause = false
                iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)

            }
        }
    }

    val mMediaControllerCompatCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            playbackStateCompat = state
            updateUIState(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
        }
    }


    private val mMediaBrowserCompatConnectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            mMediaControllerCompat = MediaControllerCompat(this@MusicActivity, mMediaBrowserCompat!!.sessionToken)
            MediaControllerCompat.setMediaController(this@MusicActivity, mMediaControllerCompat)
            mMediaControllerCompat!!.registerCallback(mMediaControllerCompatCallback)
            mMediaControllerCompatCallback.onMetadataChanged(mMediaControllerCompat!!.metadata)
            mMediaControllerCompatCallback.onPlaybackStateChanged(mMediaControllerCompat!!.playbackState)
            startServiceToPlay()
            updateProgress()

            if (mMediaControllerCompat!!.playbackState != null && (mMediaControllerCompat!!.playbackState.state == PlaybackStateCompat.STATE_PLAYING || mMediaControllerCompat!!.playbackState.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
                scheduleSeekbarUpdate()
            }

        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }


    /**
     * started service to play music
     */
    private fun startServiceToPlay() {
        val intent1 = Intent(this@MusicActivity, MusicService::class.java)
        intent1.putExtra("songId", songData.songId)
        intent1.putExtra("fromFloatingButton", intent.getBooleanExtra("fromFloatingButton", false))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent1)
        } else
            startService(intent1)
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this, MusicService::class.java)
        when (actionNumber) {
            3 -> {
                // Play
                playbackAction.action = MUSIC_HISTORY_NOTIFICATION_ACTION_PLAY
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            0 -> {
                // Pause
                playbackAction.action = MUSIC_HISTORY_NOTIFICATION_ACTION_PAUSE
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            2 -> {
                // Next track
                playbackAction.action = MUSIC_HISTORY_NOTIFICATION_ACTION_NEXT
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            1 -> {
                // Previous track
                playbackAction.action = MUSIC_HISTORY_NOTIFICATION_ACTION_PREVIOUS
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            else -> {
                return null
            }
        }
    }

    private fun updateUIState(state: PlaybackStateCompat?) {
        when (state!!.state) {
            STATE_PLAYING -> {
                tv_song_duration.text = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(musicPlayer.duration.toLong()) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(musicPlayer.duration.toLong()) % TimeUnit.MINUTES.toSeconds(1))
                iv_play_pause.setImageResource(R.drawable.ic_pause_circle_filled_red_400_48dp)
                scheduleSeekbarUpdate()
                setData()
            }
            STATE_PAUSED -> {
                iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)
                stopSeekbarUpdate()
            }
            STATE_NONE,
            STATE_STOPPED -> {
                stopSeekbarUpdate()
                iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)
            }

            STATE_SKIPPING_TO_NEXT, STATE_SKIPPING_TO_PREVIOUS-> {
                val index: Int = state.extras?.getString("currentIndex", "0")!!.toInt()
                seek_bar.progress = 0
                scheduleSeekbarUpdate()
                if (Realm.getDefaultInstance().where(SongQueue::class.java).findAll().size > 0)
                    songData = Realm.getDefaultInstance().where(SongQueue::class.java).findAll()[index]!!.song as SongHistory
                Realm.getDefaultInstance().executeTransaction {
                    val result = it.where(SongHistory::class.java).equalTo("isCurrentlyPlaying", true).findAll()
                    for (music in result) {
                        music.isCurrentlyPlaying = false
                    }
                    songData.isCurrentlyPlaying = true
                    it.copyToRealmOrUpdate(songData)
                }
            }
        }
    }

    private fun scheduleSeekbarUpdate() {
        stopSeekbarUpdate()
        if (!mExecutorService.isShutdown) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    {
                        this.mHandler.post(mUpdateProgressTask)
                    }, 100,
                    1000, TimeUnit.MILLISECONDS)
        }
    }

    private fun updateProgress() {
        if (playbackStateCompat == null) {
            return
        }
        var currentPosition = playbackStateCompat!!.position
        if (playbackStateCompat!!.state == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            val timeDelta = SystemClock.elapsedRealtime() - playbackStateCompat!!.lastPositionUpdateTime
            currentPosition += (timeDelta.toInt() * playbackStateCompat!!.playbackSpeed).toInt()
        }
        seek_bar.progress = currentPosition.toInt()
    }

    private val mUpdateProgressTask = Runnable { updateProgress() }

    private fun stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture!!.cancel(false)
        }
    }
}