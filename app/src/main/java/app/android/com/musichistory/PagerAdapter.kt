package app.android.com.musichistory

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Created by akash bisariya on 18-01-2018.
 */
class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> MusicListFragment()
        1 -> MusicHistoryFragment()
        else -> MusicListFragment()
    }

    override fun getCount()=2

    override fun getPageTitle(position: Int) = when (position) {
        0 -> "All Music"
        1 -> "Music History"
        else -> ""
    }
}