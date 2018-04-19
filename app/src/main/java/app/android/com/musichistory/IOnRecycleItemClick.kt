package app.android.com.musichistory

import android.view.MotionEvent
import android.view.View

/**
 * Created by akash
 * on 27/2/18.
 */
interface IOnRecycleItemClick {
    fun onRecycleItemClick(view: View?, position:Int)
    fun onRecycleItemLongClick(view: View?, position:Int)
    fun onRecycleItemTouch(view: View?, motionEvent:MotionEvent?,songId:String)
}