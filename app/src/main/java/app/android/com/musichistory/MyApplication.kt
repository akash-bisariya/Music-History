package app.android.com.musichistory

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by akash
 * on 21/2/18.
 */
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

        val configuration:RealmConfiguration=RealmConfiguration.Builder()
                .name("songHistory.realm")
                .schemaVersion(1)
                .build()

        Realm.setDefaultConfiguration(configuration)


    }
}