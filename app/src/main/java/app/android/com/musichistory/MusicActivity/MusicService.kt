package app.android.com.musichistory.MusicActivity

import android.app.Service
import android.content.Intent
import android.media.session.MediaController
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat


/**
 * Created by akash
 * on 5/3/18.
 */
class MusicService : MediaBrowserServiceCompat()  {
    private val MY_MEDIA_ROOT_ID = "media_root_id"
    private val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
    private var mMediaSession: MediaSessionCompat? = null
    private var mStateBuilder: PlaybackStateCompat.Builder? = null
    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, Bundle())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mMediaSession= MediaSessionCompat(this,"")
        mMediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        mMediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
//        mMediaSession.setCallback(MediaController.Callback)


        mStateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)

        mMediaSession!!.setPlaybackState(mStateBuilder!!.build())
        mMediaSession!!.setCallback(MediaCallbacks())
        sessionToken = mMediaSession!!.getSessionToken()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    private class MediaCallbacks: MediaSessionCompat.Callback()
    {

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            return super.onMediaButtonEvent(mediaButtonEvent)
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
}