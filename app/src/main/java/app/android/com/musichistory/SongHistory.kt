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
open class SongHistory(
        @Required
        @PrimaryKey
        @Index
        var songId: String = "",
        var songName: String = "",
        var songArtist: String = "",
        var albumName: String = "",
        var songDataPath: String = "",
        var songDuration: String = "",
        var lastPlayed: String = "",
        var songImage: String = "",
        var playCount: Int = 0,
        var isCurrentlyPlaying: Boolean = false
) : Serializable, RealmObject()
