package app.android.com.musichistory.MusicActivity

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by akash
 * on 5/3/18.
 */
class MusicService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}