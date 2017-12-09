package edu.osu.sphs.soundmap.util;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import edu.osu.sphs.soundmap.fragments.LoginFragment;

/**
 * Created by Gus on 11/23/2017. ViewPagerAdapter is the adapter for the viewPager object in the
 * MainActivity. The length of the fragments list must be at least three.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private static final int PAGE_COUNT = 3;
    private List<Fragment> fragments;

    public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (object instanceof LoginFragment) {
            return POSITION_NONE;
        }

        return super.getItemPosition(object);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}
