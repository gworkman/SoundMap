package edu.osu.sphs.soundmap.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import edu.osu.sphs.soundmap.fragments.MapFragment;
import edu.osu.sphs.soundmap.fragments.MeasureFragment;
import edu.osu.sphs.soundmap.fragments.ProfileFragment;

/**
 * Created by Gus on 11/23/2017. ViewPagerAdapter is the adapter for the viewPager object in the
 * MainActivity. Each of the fragments returned by the adapter implement NavigationFragment
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final int FRAGMENT_COUNT = 3;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MapFragment.newInstance();
            case 1:
                return MeasureFragment.newInstance();
            case 2:
                return ProfileFragment.newInstance();
            default:
                Log.e("ViewPagerAdapter", "Returned a null fragment, index out of range");
                return null;
        }
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }
}
