package app.android.com.musichistory

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.io.Serializable

/**
 * Created by akash
 * on 20/2/18.
 */
open class SongQueue(
        @Required
        @Index
        var songId: String="",
        var song: SongHistory?=SongHistory()
) : Serializable, RealmObject()
