package app.android.com.musichistory

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.realm.Realm

/**
 * Created by akash
 * on 8/5/18.
 */
class MediaNotificationManager(val mMusicService: MusicService) : BroadcastReceiver() {
    private val mNotificationManager: NotificationManager = mMusicService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private lateinit var mTransportControl: MediaControllerCompat.TransportControls
    private var mStarted = false
    private lateinit var mMediaSessionToken:MediaSessionCompat.Token
    private lateinit var mMediaControllerCompat:MediaControllerCompat
    private var songData:SongHistory
    private lateinit var mNotification: Notification
    private val REQUEST_CODE = 100
    private val MUSIC_HISTORY_NOTIFICATION_ID = 1001

    private var mPlaybackState: PlaybackStateCompat? = null

    init {
        mNotificationManager.cancelAll()
        updateSessionToken()
        songData = Realm.getDefaultInstance().where(SongHistory::class.java).equalTo("isCurrentlyPlaying",true).findFirst()!!
    }

    private val mMediaControllerCompatCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
        }

        override fun onSessionReady() {
            super.onSessionReady()
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            updateSessionToken()
        }
    }

    fun buildNotification() {
        val bitmap: Bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inSampleSize=2
        if (songData.songImage != "") {
            bitmap = BitmapFactory.decodeFile(songData.songImage, bmOptions)
        } else {
            bitmap = BitmapFactory.decodeResource(mMusicService.resources, R.drawable.music_icon)
        }

        val notificationIntent = Intent(mMusicService, MusicActivity::class.java)
        val backIntent = Intent(mMusicService, MainActivity::class.java)
        notificationIntent.putExtra("fromFloatingButton", true)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent = PendingIntent.getActivities(mMusicService, 2500, arrayOf(backIntent, notificationIntent), PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(mMusicService)
                .setContentTitle(songData.songName)
                .setAutoCancel(false)
                .setOngoing(mPlaybackState!!.state == PlaybackStateCompat.STATE_PLAYING)
                .setSmallIcon(R.drawable.screen_home)
                .setLargeIcon(bitmap)
                .setContentIntent(contentIntent)
                .setColor(mMusicService.resources.getColor(R.color.color_red))
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSessionToken)
                        .setShowCancelButton(true)
                        .setShowActionsInCompactView(1))
                .setContentText(songData.songArtist)
                .setContentInfo(songData.songName)

        val label: String?
        val icon: Int?
        val intent: PendingIntent?
        if (mPlaybackState!!.state == PlaybackStateCompat.STATE_PLAYING) {
            label = "pause"
            icon = R.drawable.ic_pause_circle_filled_red_400_48dp
            intent = playbackAction(0)
        } else {
            label = "play"
            icon = R.drawable.ic_play_circle_filled_red_400_48dp
            intent = playbackAction(3)
        }

        builder.addAction(R.drawable.ic_skip_previous_red_400_48dp, "previous", playbackAction(1))
        mNotification = builder.addAction(NotificationCompat.Action(icon, label, intent))
                .addAction(R.drawable.ic_skip_next_red_400_48dp, "next", playbackAction(2))
                .build()
    }


    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackActionIntent:Intent = Intent().setPackage(mMusicService.packageName)
        val pkg = mMusicService.packageName
        when (actionNumber) {
            3 -> {
                // Play
                playbackActionIntent.action = MUSIC_HISTORY_NOTIFICATION_ACTION_PLAY
                return PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, playbackActionIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            0 -> {
                // Pause
                playbackActionIntent.action = MUSIC_HISTORY_NOTIFICATION_ACTION_PAUSE
                return PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, playbackActionIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            2 -> {
                // Next track
                playbackActionIntent.action = MUSIC_HISTORY_NOTIFICATION_ACTION_NEXT
                return PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, playbackActionIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            1 -> {
                // Previous track
                playbackActionIntent.action = MUSIC_HISTORY_NOTIFICATION_ACTION_PREVIOUS
                return PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, playbackActionIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            else -> {
                return null
            }
        }
    }


    fun startNotification()
    {
        if(!mStarted)
        {
            mPlaybackState = mMediaControllerCompat.playbackState
            mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback)
            buildNotification()
            val filter = IntentFilter()
            filter.addAction(MUSIC_HISTORY_NOTIFICATION_ACTION_NEXT)
            filter.addAction(MUSIC_HISTORY_NOTIFICATION_ACTION_PAUSE)
            filter.addAction(MUSIC_HISTORY_NOTIFICATION_ACTION_PLAY)
            filter.addAction(MUSIC_HISTORY_NOTIFICATION_ACTION_PREVIOUS)
            mMusicService.registerReceiver(this, filter)

            mMusicService.startForeground(MUSIC_HISTORY_NOTIFICATION_ID, mNotification)
            mStarted = true

        }
    }


    fun updateSessionToken()
    {
        mMediaSessionToken= mMusicService.sessionToken!!
        mMediaControllerCompat= MediaControllerCompat(mMusicService,mMediaSessionToken)
        mMediaControllerCompat.unregisterCallback(mMediaControllerCompatCallback)
        mTransportControl=mMediaControllerCompat.transportControls
        if(mStarted)
        {
            mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback)
        }
    }

    override fun onReceive(p0: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                MUSIC_HISTORY_NOTIFICATION_ACTION_PLAY -> {
                    mTransportControl.play()
//                    playMediaPlayer()
                }
                MUSIC_HISTORY_NOTIFICATION_ACTION_PREVIOUS -> {
                    mTransportControl.skipToPrevious()
//                    handlePlayRequest(-1)
                }
                MUSIC_HISTORY_NOTIFICATION_ACTION_NEXT -> {
                    mTransportControl.skipToNext()
//                    handlePlayRequest(1)
                }
                MUSIC_HISTORY_NOTIFICATION_ACTION_PAUSE -> {
                    mTransportControl.pause()
//                    if (mMusicPlayer.isPlaying) pauseMediaPlayer() else playMediaPlayer()
                }
            }
        }
    }
}