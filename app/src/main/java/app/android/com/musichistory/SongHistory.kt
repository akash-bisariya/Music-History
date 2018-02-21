package app.android.com.musichistory

import io.realm.RealmModel
import io.realm.RealmObject
import java.io.Serializable

/**
* Created by akash
* on 20/2/18.
*/
open class SongHistory(
        var songName:String,
        var songArtist:String,
        var albumName:String,
        var songData:String,
        var songDuration:String,
        var lastPlayed:String,
        var playCount:Int
        ): Serializable, RealmObject()
{
    constructor() : this(songName="",songArtist = "",albumName = "",songData = "",songDuration = "",lastPlayed = "",playCount = 0)
}