package app.android.com.musichistory

import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by akash
 * on 21/2/18.
 */
class MyApplication: Application() {
    companion object {
        lateinit var application_context: Context
        fun getAppContext():Context
        {
            return application_context
        }
    }
    var mediaChangeObserver:MediaChangeObserver? = null
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        application_context=this
        val configuration:RealmConfiguration=RealmConfiguration.Builder()
                .name("songHistory.realm")
                .schemaVersion(3)
//                .migration(RealmChangeMigration())
                .deleteRealmIfMigrationNeeded()
                .build()

        Realm.setDefaultConfiguration(configuration)
        mediaChangeObserver = MediaChangeObserver(Environment.getExternalStorageDirectory().absolutePath,
                FileObserver.CREATE
                        or FileObserver.DELETE
                        or FileObserver.MODIFY
        )
        mediaChangeObserver!!.startWatching()
    }

    override fun onTerminate() {
        mediaChangeObserver!!.stopWatching()
        Log.d("MusicHistory","Stop watching files")
        super.onTerminate()
    }
}