package app.android.com.musichistory.MusicActivity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import app.android.com.musichistory.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_music.*
import android.media.MediaPlayer
import android.support.v7.graphics.Palette
import android.view.View
import android.widget.Toast

/**
 * Created by akash
 * on 22/2/18.
 */
class MusicActivity : AppCompatActivity(), MusicView, View.OnClickListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    private var mediaPlayerPause = false
    private var audioManager: AudioManager? = null
    override fun onAudioFocusChange(result: Int) {

        when (result) {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                if (mediaPlayerPause)
                    mediaPlayer.start()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                mediaPlayer.reset()
                mediaPlayerPause = false
                iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer.setDataSource(intent.getStringExtra("songData"))
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnErrorListener(this)
                mediaPlayer.setOnPreparedListener {
                    it.start()
                    iv_play_pause.setImageResource(R.drawable.ic_pause_circle_filled_red_400_48dp)
                }
            }
            else -> {
                mediaPlayer.reset()
                mediaPlayerPause = false
                iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)
            }
        }
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        Toast.makeText(this, "" + p1 + " " + p2, Toast.LENGTH_SHORT).show()
        return true
    }


    private val mediaPlayer = MediaPlayer()
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.iv_play_pause -> {
                    if (!mediaPlayer.isPlaying) {
                        if (mediaPlayerPause) {
                            mediaPlayer.start()
                            iv_play_pause.setImageResource(R.drawable.ic_pause_circle_filled_red_400_48dp)
                        } else {

                            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            val result = audioManager!!.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

                            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                mediaPlayer.setDataSource(intent.getStringExtra("songData"))
                                mediaPlayer.prepareAsync()
                                mediaPlayer.setOnErrorListener(this)
                                mediaPlayer.setOnPreparedListener {
                                    it.start()
                                    iv_play_pause.setImageResource(R.drawable.ic_pause_circle_filled_red_400_48dp)
                                }
                            }
                        }
                    } else {
                        mediaPlayer.pause()
                        mediaPlayerPause = true
                        iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)
                    }
                }
                R.id.iv_next -> {
                    val result = audioManager!!.abandonAudioFocus(this)
                    if (result == AudioManager.AUDIOFOCUS_LOSS) {
                        mediaPlayer.reset()
                        mediaPlayerPause = false
                        iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)
                    }
                }
            }
        }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        setSupportActionBar(toolbar)
        if (intent != null) {
            Glide.with(this)
                    .applyDefaultRequestOptions(RequestOptions()
                            .placeholder(R.drawable.music_icon)
                            .useAnimationPool(true))
                    .load(intent.getStringExtra("songImage"))
                    .into(iv_song_image)

            tv_song_artist.text = intent.getStringExtra("songArtist")
            tv_song_duration.text = intent.getStringExtra("songduration")
            tv_song_name.text = intent.getStringExtra("songName")

            iv_play_pause.setOnClickListener(this)
            iv_next.setOnClickListener(this)

            var bitmap: Bitmap? = null
            val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
            if (intent.getStringExtra("songImage") != null && (!(intent.getStringExtra("songImage")).equals(""))) {
                bitmap = BitmapFactory.decodeFile(intent.getStringExtra("songImage"), bmOptions)
                bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
                Palette.from(bitmap).generate(
                        {
                            music_constraint_layout.background = getGradientDrawable(getTopColor(it), getCenterLightColor(it), getBottomDarkColor(it))
                            toolbar.background = getGradientDrawable(getTopColor(it), getCenterLightColor(it), getBottomDarkColor(it))
                        })
            }
        }
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
}