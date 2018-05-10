package app.android.com.musichistory.utils

import android.nfc.Tag
import android.util.Log
import app.android.com.musichistory.constants.MUSIC_HISTORY_LOGGER_STATUS

/**
 * Created by akash
 * on 10/5/18.
 */
class Logger {
    companion object {
        const val TAG ="MusicHistory"
        fun error(s: String, throwable: Throwable) {
            if (MUSIC_HISTORY_LOGGER_STATUS)
                Log.e(TAG, s, throwable)
        }

        fun e(s: String) {
            if (MUSIC_HISTORY_LOGGER_STATUS)
                Log.e(TAG, "" + s)
        }

        fun w(s: String) {
            if (MUSIC_HISTORY_LOGGER_STATUS)
                Log.w(TAG, "" + s)
        }

        fun i(s: String) {
            if (MUSIC_HISTORY_LOGGER_STATUS)
                Log.i(TAG, "" + s)

        }

        fun v(s: String) {
            if (MUSIC_HISTORY_LOGGER_STATUS)
                Log.v(TAG, "" + s)
        }

        fun d(s: String) {
            if (MUSIC_HISTORY_LOGGER_STATUS)
                Log.d(TAG, "" + s)
        }

    }
}