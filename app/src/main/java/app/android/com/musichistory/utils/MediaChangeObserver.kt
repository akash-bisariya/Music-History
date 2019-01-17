package app.android.com.musichistory.utils

import android.content.Intent
import android.os.FileObserver
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import app.android.com.musichistory.MyApplication
import app.android.com.musichistory.constants.*
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.util.*

class MediaChangeObserver(val mPath: String?, event: Int) : FileObserver(mPath, event) {
    private var mObservers: List<SingleFileObserver>? = null


    override fun onEvent(event: Int, path: String?) {

        Log.d("MusicHistory", "$path = $event")
        when (event.and(FileObserver.ALL_EVENTS)) {
            FileObserver.CREATE -> {
                Log.d("MusicHistory", "CREATE FILE CHANGE OBSERVE :$mPath$path")
                if(path!!.contains("mp3")) {
                    val intent = Intent()
                    intent.action = MUSIC_HISTORY_CHANGE_OBSERVE_IN_AUDIO_FILES
                    intent.putExtra(MUSIC_HISTORY_KEY_FOR_CHANGED_AUDIO_FILE_PATH, path)
                    intent.putExtra(MUSIC_HISTORY_KEY_FOR_CHANGED_AUDIO_FILE_EVENT_NAME, "" + FileObserver.CREATE)
                    LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent)
                }
            }
            FileObserver.DELETE ->
                Log.d("MusicHistory", "DELETE:$mPath$path")
            FileObserver.DELETE_SELF ->
                Log.d("MusicHistory", "DELETE_SELF:$mPath$path")
            FileObserver.MODIFY -> {
                Log.d("MusicHistory", "MODIFY:$mPath$path")
                if(path!!.contains("mp3")) {
                    val intent = Intent()
                    intent.action = MUSIC_HISTORY_CHANGE_OBSERVE_IN_AUDIO_FILES
                    intent.putExtra(MUSIC_HISTORY_KEY_FOR_CHANGED_AUDIO_FILE_PATH, path)
                    intent.putExtra(MUSIC_HISTORY_KEY_FOR_CHANGED_AUDIO_FILE_EVENT_NAME, "" + FileObserver.MODIFY)
                    LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent)
                }
            }
            FileObserver.MOVED_FROM ->
                Log.d("MusicHistory", "MOVED_FROM:$mPath$path")
            FileObserver.MOVED_TO ->
                Log.d("MusicHistory", "MOVED_TO:$path")
            FileObserver.MOVE_SELF ->
                Log.d("MusicHistory", "MOVE_SELF:$path")
        }
    }

    override fun startWatching() {
        super.startWatching()
        if (mObservers != null) return
        launch {
            mObservers = ArrayList()
            val stack = Stack<String>()
            stack.push(mPath)

            while (!stack.empty()) {
                val parent = stack.pop()
                (mObservers as ArrayList<SingleFileObserver>).add(SingleFileObserver(parent))
                val path = File(parent)
                val files = path.listFiles() ?: continue
                for (i in files.indices) {
                    if (files[i].isDirectory) {
                        stack.push(files[i].path)
                    }
                }
            }
            for (i in 0 until (mObservers as ArrayList<SingleFileObserver>).size)
                (mObservers as ArrayList<SingleFileObserver>)[i].startWatching()
        }
    }

    inner class SingleFileObserver(path: String?) : FileObserver(path) {
        override fun onEvent(event: Int, path: String?) {
            val newPath = "$mPath/$path";
            this@MediaChangeObserver.onEvent(event, newPath)
        }

    }
}
