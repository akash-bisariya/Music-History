package app.android.com.musichistory

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Created by akash bisariya on 18-01-2018.
 */
class PagerAdapter(val fm:FragmentManager,val songsList: ArrayList<SongHistory> ) : FragmentStatePagerAdapter(fm) {
    override fun getPageTitle(position: Int): CharSequence {
        var title:String=""
        when(position)
        {
            0-> title ="All Music"
            1->title = "Music History"
        }
        return title
    }

    override fun getItem(position: Int): Fragment? {
        when(position)
        {
            0-> {
                val fragment = MusicHistoryFragment()
                val args = Bundle()
                args.putSerializable("SongsList", songsList)
                fragment.arguments = args
                return fragment
            }

        }

        return MusicHistoryFragment()


    }

    override fun getCount(): Int {
        return 2
    }
}