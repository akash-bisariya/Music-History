package app.android.com.musichistory

import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.RealmSchema

/**
 * Created by akash
 * on 22/2/18.
 */
class RealmChangeMigration:RealmMigration {
    override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {
//        val realmSchema:RealmSchema = realm!!.schema
//        if(oldVersion==2.toLong())
//        {
//            realmSchema.get("SongHistory")!!.addField("songImage", String::class.java)
//            var version = oldVersion
//            version++
//        }


        realm ?: return
        realm.schema.let {
            var migVersion = oldVersion
            // ver 6 -> 7
            if (migVersion == 2.toLong()) {
                // TabModel
                it.get("SongHistory")!!.addField("songImage", String::class.java)
                migVersion += 1
            }
        }
    }
}