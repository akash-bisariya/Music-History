package app.android.com.musichistory

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Created by akash bisariya on 18-01-2018.
 */
class PagerAdapter(val fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getPageTitle(position: Int): CharSequence {
        var title = ""
        when (position) {
            0 -> title = "All Music"
            1 -> title = "Music History"
        }
        return title
    }

    override fun getItem(position: Int): Fragment? {

        return when (position) {
            0 -> {
                MusicListFragment()
            }

            1 -> {
                MusicHistoryFragment()
            }
            else -> {
                MusicListFragment()
            }

        }

    }

    override fun getCount(): Int {
        return 2
    }
}