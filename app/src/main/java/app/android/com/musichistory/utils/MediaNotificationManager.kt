package app.android.com.musichistory.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import app.android.com.musichistory.MainActivity
import app.android.com.musichistory.MusicActivity
import app.android.com.musichistory.MusicService
import app.android.com.musichistory.R
import app.android.com.musichistory.constants.*
import app.android.com.musichistory.customViews.MusicPlayer
import app.android.com.musichistory.models.SongHistory
import app.android.com.musichistory.models.SongQueue
import io.realm.Realm

/**
 * Created by akash
 * on 8/5/18.
 */
class MediaNotificationManager(private val mMusicService: MusicService) : BroadcastReceiver() {
    private val mNotificationManager: NotificationManager = mMusicService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private lateinit var mTransportControl: MediaControllerCompat.TransportControls
    private var mStarted = false
    private lateinit var mMediaSessionToken: MediaSessionCompat.Token
    private lateinit var mMediaControllerCompat: MediaControllerCompat
    private lateinit var songData: SongHistory

    private lateinit var mNotification: Notification
    private val REQUEST_CODE = 100
    private lateinit var mPlaybackState: PlaybackStateCompat
    private val mMediaControllerCompatCallback: MediaControllerCompat.Callback

    init {
        mMediaControllerCompatCallback = object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                super.onPlaybackStateChanged(state)
                mPlaybackState = state!!
                Logger.d("""PlayBack State Changed${state.actions}""")
                if (state.state == PlaybackStateCompat.STATE_STOPPED || state.state == PlaybackStateCompat.STATE_NONE) {
                    stopNotification()
                } else if (state.state == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT) {
                    val index: Int = state.extras?.getString("currentIndex", "0")!!.toInt()
//                    songData = Realm.getDefaultInstance().where(SongQueue::class.java).findAll()[index]!!.song as SongHistory
                    buildNotification(index)
                    mNotificationManager.notify(MUSIC_HISTORY_NOTIFICATION_ID, mNotification)
                } else if (state.state == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS) {
                    val index: Int = state.extras?.getString("currentIndex", "0")!!.toInt()
                    buildNotification(index)
                    mNotificationManager.notify(MUSIC_HISTORY_NOTIFICATION_ID, mNotification)
                }

            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                super.onMetadataChanged(metadata)
//                buildNotification()
//                mNotificationManager.notify(MUSIC_HISTORY_NOTIFICATION_ID,mNotification)
            }

            override fun onSessionReady() {
                super.onSessionReady()
                updateSessionToken()
            }

            override fun onSessionDestroyed() {
                super.onSessionDestroyed()
                Logger.d("Session Destroyed")
                updateSessionToken()
            }
        }
        mNotificationManager.cancelAll()
        updateSessionToken()
    }


    /**
     * Posts the notification start tracking the music session
     */
    fun startNotification(isPlaybackStateChanged: Boolean, currentIndex: Int) {
        if (!mStarted) {
            mPlaybackState = mMediaControllerCompat.playbackState
            mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback)
            buildNotification(currentIndex)
            val filter = IntentFilter()
            filter.addAction(MUSIC_HISTORY_NOTIFICATION_ACTION_NEXT)
            filter.addAction(MUSIC_HISTORY_NOTIFICATION_ACTION_PAUSE)
            filter.addAction(MUSIC_HISTORY_NOTIFICATION_ACTION_PLAY)
            filter.addAction(MUSIC_HISTORY_NOTIFICATION_ACTION_PREVIOUS)
            mMusicService.registerReceiver(this, filter)
            mMusicService.startForeground(MUSIC_HISTORY_NOTIFICATION_ID, mNotification)
            mStarted = true
        } else if (isPlaybackStateChanged) {
            buildNotification(currentIndex)
            mNotificationManager.notify(MUSIC_HISTORY_NOTIFICATION_ID, mNotification)
        }
    }


    /**
     * Removes the notification and stops tracking the music session
     */
    fun stopNotification() {
        if (mStarted) {
            mStarted = false
            mMediaControllerCompat.unregisterCallback(mMediaControllerCompatCallback)
            mNotificationManager.cancel(MUSIC_HISTORY_NOTIFICATION_ID)
            mMusicService.unregisterReceiver(this)
            mMusicService.stopForeground(true)

        }
    }

    /**
     *   Create mediaStyle notifications
     */
    private fun buildNotification(currentIndex: Int) {
        songData = Realm.getDefaultInstance().where(SongQueue::class.java).findAll()[currentIndex]!!.song as SongHistory
        val bitmap: Bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inSampleSize = 2
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
                .setOngoing(mPlaybackState.state == PlaybackStateCompat.STATE_PLAYING)
                .setSmallIcon(R.drawable.circular_img)
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
        if (mPlaybackState.state == PlaybackStateCompat.STATE_PLAYING) {
            label = mMusicService.getString(R.string.label_txt_pause)
            icon = R.drawable.ic_pause_circle_filled_red_400_48dp
            intent = playbackAction(0)
        } else {
            label = mMusicService.getString(R.string.label_txt_play)
            icon = R.drawable.ic_play_circle_filled_red_400_48dp
            intent = playbackAction(3)
        }

        builder.addAction(R.drawable.ic_skip_previous_red_400_48dp, "previous", playbackAction(1))

        mNotification = builder.addAction(NotificationCompat.Action(icon, label, intent))
                .addAction(R.drawable.ic_skip_next_red_400_48dp, "next", playbackAction(2))
                .build()
    }


    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackActionIntent: Intent = Intent().setPackage(mMusicService.packageName)
        when (actionNumber) {
            0 -> {
                // Pause
                playbackActionIntent.action = MUSIC_HISTORY_NOTIFICATION_ACTION_PAUSE
                return PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, playbackActionIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            1 -> {
                // Previous track
                playbackActionIntent.action = MUSIC_HISTORY_NOTIFICATION_ACTION_PREVIOUS
                return PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, playbackActionIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            2 -> {
                // Next track
                playbackActionIntent.action = MUSIC_HISTORY_NOTIFICATION_ACTION_NEXT
                return PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, playbackActionIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            3 -> {
                // Play
                playbackActionIntent.action = MUSIC_HISTORY_NOTIFICATION_ACTION_PLAY
                return PendingIntent.getBroadcast(mMusicService, REQUEST_CODE, playbackActionIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            else -> {
                return null
            }
        }
    }


    fun updateSessionToken() {
        mMediaSessionToken = mMusicService.sessionToken!!
        mMediaControllerCompat = MediaControllerCompat(mMusicService, mMediaSessionToken)
        mMediaControllerCompat.unregisterCallback(mMediaControllerCompatCallback)
        mTransportControl = mMediaControllerCompat.transportControls
        if (mStarted) {
            mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback)
        }
    }

    override fun onReceive(p0: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                MUSIC_HISTORY_NOTIFICATION_ACTION_PLAY -> {
                    mTransportControl.play()
                }
                MUSIC_HISTORY_NOTIFICATION_ACTION_PREVIOUS -> {
                    mTransportControl.skipToPrevious()
                }
                MUSIC_HISTORY_NOTIFICATION_ACTION_NEXT -> {
                    mTransportControl.skipToNext()
                }
                MUSIC_HISTORY_NOTIFICATION_ACTION_PAUSE -> {
                    if (MusicPlayer.isPlaying) mTransportControl.pause() else mTransportControl.play()
                }
            }
        }
    }
}