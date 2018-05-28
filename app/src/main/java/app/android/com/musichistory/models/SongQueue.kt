package app.android.com.musichistory.models

import app.android.com.musichistory.models.SongHistory
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.Required
import java.io.Serializable

/**
 * Created by akash
 * on 20/2/18.
 */
open class SongQueue(
        var song: SongHistory?= SongHistory()
) : Serializable, RealmObject()
