package app.android.com.musichistory.MusicActivity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_music.*
import android.media.MediaPlayer
import android.media.browse.MediaBrowser


import android.os.Handler
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v7.graphics.Palette
import android.view.View
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmResults
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import android.text.format.DateUtils
import android.util.Log
import android.widget.SeekBar
import app.android.com.musichistory.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


/**
 * Created by akash
 * on 22/2/18.
 */
class MusicActivity : AppCompatActivity(), MusicView, View.OnClickListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener, IMusicPlayerPlayback {
    private var mScheduleFuture: ScheduledFuture<*>? = null
    private var mediaPlayerPause = false
    private var audioFocusCanDuck = false
    private var repeatCount: Int = -1
    private var audioManager: AudioManager? = null
    private lateinit var songData: RealmResults<SongHistory>
    private val musicPlayer: MusicPlayer = MusicPlayer
    private val mTimer: Timer = Timer("SeekBarListener")
    private var mMediaBrowserCompat: MediaBrowserCompat? = null
    private var playbackStateCompat: PlaybackStateCompat? = null
    private val mHandler = Handler()
    private val mExecutorService = Executors.newSingleThreadScheduledExecutor()
    val mMediaControllerCompatCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onCaptioningEnabledChanged(enabled: Boolean) {
            super.onCaptioningEnabledChanged(enabled)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            super.onRepeatModeChanged(repeatMode)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            playbackStateCompat = state
            updateUIState(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
//            updateMediaDescription(metadata.getDescription())
            updateDuration(metadata)
        }
    }

        private fun updateDuration(metadata: MediaMetadataCompat?) {
            if (metadata == null) {
                return
            }
            Log.d("MusicHistory", "updateDuration called ")
            val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt()
            seek_bar.setMax(duration)
            tv_song_duration.text = DateUtils.formatElapsedTime((duration / 1000).toLong())
        }



    private fun scheduleSeekbarUpdate() {
        stopSeekbarUpdate()
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    Runnable {
                        mHandler.post(mUpdateProgressTask)
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
            currentPosition += (timeDelta.toInt() * playbackStateCompat!!.playbackSpeed).toLong()
        }
        seek_bar.progress = currentPosition.toInt()
//        tv_song_current_position.text = (currentPosition.toInt()/1000).toString()
    }


    private val mUpdateProgressTask = Runnable { updateProgress() }

    private fun stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture!!.cancel(false)
        }
    }

    private fun updateUIState(state: PlaybackStateCompat?) {
        when(state!!.state)
        {
            STATE_PLAYING->
            {
                iv_play_pause.setImageResource(R.drawable.ic_pause_circle_filled_red_400_48dp)
                scheduleSeekbarUpdate()
            }
            STATE_PAUSED->
                iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)
        }

    }

    private val mMediaBrowserCompatConnectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            val mMediaControllerCompat = MediaControllerCompat(this@MusicActivity, mMediaBrowserCompat!!.sessionToken)
            MediaControllerCompat.setMediaController(this@MusicActivity, mMediaControllerCompat)
            mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback)
//            mediaController=mMediaControllerCompat

            mMediaControllerCompatCallback.onMetadataChanged(
                    mMediaControllerCompat.metadata);
            mMediaControllerCompatCallback
                    .onPlaybackStateChanged(mMediaControllerCompat.playbackState);
//            mediaController.getTransportControls().playFromMediaId(String.valueOf(R.raw.warner_tautz_off_broadway), null);
//            MediaControllerCompat.getMediaController(this@MusicActivity).transportControls.playFromMediaId(intent.getStringExtra("songId"), null)

            val intent1 = Intent(this@MusicActivity, MusicService::class.java)
            intent1.putExtra("songId", intent.getStringExtra("songId"))
            startService(intent1)

        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        setSupportActionBar(toolbar)

        songData = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("songId", intent.getStringExtra("songId")).findAll()

        Glide.with(this)
                .applyDefaultRequestOptions(RequestOptions()
                        .placeholder(R.drawable.music_icon)
                        .useAnimationPool(true))
                .load(songData[0]!!.songImage)
                .into(iv_song_image)

        tv_song_artist.text = songData[0]!!.songArtist
        tv_song_duration.text = "%.2f".format((((songData[0]!!.songDuration))).toFloat() / (1000 * 60))
        tv_song_current_position.text = "0.00"
        tv_song_name.text = songData[0]!!.songName
        tv_song_play_count.text = songData[0]!!.playCount.toString()
        iv_like.setOnClickListener(this)
        iv_play_pause.setOnClickListener(this)
        iv_next.setOnClickListener(this)
        iv_repeat.setOnClickListener(this)

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
        var bitmap: Bitmap?
        val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
        if (!(songData[0]!!.songImage).equals("")) {
            bitmap = BitmapFactory.decodeFile(songData[0]!!.songImage, bmOptions)
            bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
            Palette.from(bitmap).generate(
                    {
                        music_constraint_layout.background = getGradientDrawable(getTopColor(it), getCenterLightColor(it), getBottomDarkColor(it))
                        toolbar.background = getGradientDrawable(getTopColor(it), getCenterLightColor(it), getBottomDarkColor(it))
                    })
        }

        mMediaBrowserCompat = MediaBrowserCompat(this, ComponentName(this, MusicService::class.java), mMediaBrowserCompatConnectionCallback, null)
        mMediaBrowserCompat!!.connect()


    }


    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        Log.d("MediaPlayerError", "$p1+$p2")
        p0!!.reset()
        return true
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.iv_play_pause -> {
                    if (!musicPlayer.isPlaying) {
                        if (mediaPlayerPause) {
                            musicPlayer.start()
                            iv_play_pause.setImageResource(R.drawable.ic_pause_circle_filled_red_400_48dp)

                        } else {
                            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            val result = audioManager!!.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                musicPlayer.setDataSource(songData[0]!!.songData)
                                musicPlayer.prepareAsync()
                                musicPlayer.setOnErrorListener(this)
                                musicPlayer.setOnPreparedListener {
                                    it.start()
                                    iv_play_pause.setImageResource(R.drawable.ic_pause_circle_filled_red_400_48dp)
                                    seek_bar.progress = 0
                                    seek_bar.max = songData[0]!!.songDuration.toInt()
//                                    mTimer.scheduleAtFixedRate(object : TimerTask() {
//                                        override fun run() {
//                                            seek_bar.progress = musicPlayer.currentPosition
//                                            onProgress(musicPlayer.currentPosition)
//                                            Log.e("onProgress", "Prog   ress" + musicPlayer.currentPosition)
//                                        }
//                                    }, 1000, 1000)
                                }
                            }
                        }
                    } else {
                        musicPlayer.pause()
                        mediaPlayerPause = true
                        iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)
                    }
                }

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
                }

                R.id.iv_next -> {
                    val result = audioManager!!.abandonAudioFocus(this)
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        musicPlayer.reset()
                        mediaPlayerPause = false
                        iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer.cancel()
        mTimer.purge()
    }


    override fun playSong() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stopSong() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nextSong() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prevSong() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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


    override fun onCompletion(p0: MediaPlayer?) {
        musicPlayer.stop()
        musicPlayer.reset()
        mTimer.cancel()
        mTimer.purge()
        seek_bar.progress = 0
    }

    override fun onPauseMusicPlayer(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStopMusicPlayer() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartMusicPlayer() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProgress(position: Int) {
        runOnUiThread({
            //            tv_song_current_position.text = "%.2f".format((musicPlayer.currentPosition).toFloat() / (1000 * 60))
//            tv_song_current_position.text = (musicPlayer.currentPosition / 1000).toString()
        })

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
                    musicPlayer.setDataSource(songData[0]!!.songData)
                    musicPlayer.prepareAsync()
                    musicPlayer.setOnErrorListener(this)
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


}