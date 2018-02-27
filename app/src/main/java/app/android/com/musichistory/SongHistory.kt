package app.android.com.musichistory

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.Required
import java.io.Serializable

/**
* Created by akash
* on 20/2/18.
*/
open class SongHistory(
        @Required
        var songId:String,
        var songName:String,
        var songArtist:String,
        var albumName:String,
        var songData:String,
        var songDuration:String,
        var lastPlayed:String,
        var songImage:String,
        var playCount:Int
        ): Serializable, RealmObject()
{
    constructor() : this(songId="",songName="",songArtist = "",albumName = "",songData = "",songDuration = "",lastPlayed = "",songImage = "",playCount = 0)
}