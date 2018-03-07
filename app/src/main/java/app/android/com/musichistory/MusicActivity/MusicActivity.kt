package app.android.com.musichistory.MusicActivity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import app.android.com.musichistory.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_music.*
import android.media.MediaPlayer
import android.view.View
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import jp.wasabeef.glide.transformations.BlurTransformation


/**
 * Created by akash
 * on 22/2/18.
 */
class MusicActivity : AppCompatActivity(), MusicView, View.OnClickListener, MediaPlayer.OnErrorListener {
    var mediaPlayerPause = false
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

                        } else {
                            mediaPlayer.setDataSource(intent.getStringExtra("songData"))
                            mediaPlayer.prepareAsync()
                            mediaPlayer.setOnErrorListener(this)
                            mediaPlayer.setOnPreparedListener {
                                it.start()
                            }
                        }
                        iv_play_pause.setImageResource(R.drawable.ic_pause_circle_filled_red_400_48dp)
                    } else {
                        mediaPlayer.pause()
                        mediaPlayerPause = true
                        iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)


//                        mediaPlayer.reset()
//                        mediaPlayer.setDataSource(intent.getStringExtra("songData"))
//                        mediaPlayer.prepare()
                    }

                }

                R.id.iv_next -> {
                    mediaPlayer.reset()
                    mediaPlayerPause = false
                    iv_play_pause.setImageResource(R.drawable.ic_play_circle_filled_red_400_48dp)
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



//            val bmOptions:BitmapFactory.Options  = BitmapFactory.Options();
//            var bitmap:Bitmap  = BitmapFactory.decodeFile(intent.getStringExtra("songImage"),bmOptions);
//            bitmap = Bitmap.createScaledBitmap(bitmap,200,200,true);


//            Palette.from(bitmap).generate(Palette.PaletteAsyncListener {
//                iv_song_image_background.background = getGradientDrawable(getTopColor(it),getCenterLightColor(it),getBottomDarkColor(it))
//            })

//            Palette.from(BitmapFactory.decodeResource(resources, R.drawable.tiger1)).generate(object : Palette.PaletteAsyncListener() {
//                fun onGenerated(palette: Palette) {
//                    rlView1.setBackground(getGradientDrawable(getTopColor(palette), getCenterLightColor(palette), getBottomDarkColor(palette)))
//                }
//            })

        }


//        /**
//         * @param palette generated palette from image
//         * @return return center light color for gradient either muted or vibrant whatever is available
//         */
//        private int getCenterLightColor(Palette palette) {
//            if (palette.getLightMutedSwatch() != null || palette.getLightVibrantSwatch() != null)
//                return palette.getLightMutedSwatch() != null ? palette.getLightMutedSwatch().getRgb() : palette.getLightVibrantSwatch().getRgb();
//            else return Color.GREEN;
//        }
//
//        /**
//         * @param palette generated palette from image
//         * @return return bottom dark color for gradient either muted or vibrant whatever is available
//         */
//        private int getBottomDarkColor(Palette palette) {
//            if (palette.getDarkMutedSwatch() != null || palette.getDarkVibrantSwatch() != null)
//                return palette.getDarkMutedSwatch() != null ? palette.getDarkMutedSwatch().getRgb() : palette.getDarkVibrantSwatch().getRgb();
//            else return Color.BLUE;
//        }

    }
//    fun getGradientDrawable(topColor:Int, centerColor:Int,bottomColor:Int) :GradientDrawable{
//        val gradientDrawable =  GradientDrawable()
//        gradientDrawable.orientation = GradientDrawable.Orientation.TOP_BOTTOM
//        gradientDrawable.colors= intArrayOf(topColor,centerColor,bottomColor)
//        gradientDrawable.shape = GradientDrawable.RECTANGLE
//
//        return gradientDrawable
//    }
//
//
//    fun getTopColor(palette:Palette):Int {
//        if (palette.mutedSwatch != null || palette.vibrantSwatch != null)
//            when (palette.mutedSwatch){
//                null-> return palette.mutedSwatch!!.rgb
//                else-> return palette.vibrantSwatch!!.rgb
//            }
////                return palette.getMutedSwatch() != null ? palette.getMutedSwatch().getRgb() : palette.getVibrantSwatch().getRgb();
//        else return Color.RED
//
//    }
//
//
//    fun getCenterLightColor(palette:Palette):Int {
//        if (palette.lightMutedSwatch != null || palette.lightVibrantSwatch != null)
//            when (palette.lightMutedSwatch){
//                null-> return palette.lightMutedSwatch!!.rgb
//                else-> return palette.lightVibrantSwatch!!.rgb
//            }
////                return palette.getMutedSwatch() != null ? palette.getMutedSwatch().getRgb() : palette.getVibrantSwatch().getRgb();
//        else return Color.GREEN
//
//    }
//
//    fun getBottomDarkColor(palette:Palette):Int {
//        if (palette.darkMutedSwatch != null || palette.darkVibrantSwatch != null)
//            when (palette.darkMutedSwatch){
//                null-> return palette.darkMutedSwatch!!.rgb
//                else-> return palette.darkVibrantSwatch!!.rgb
//            }
////                return palette.getMutedSwatch() != null ? palette.getMutedSwatch().getRgb() : palette.getVibrantSwatch().getRgb();
//        else return Color.BLUE
//
//    }


}