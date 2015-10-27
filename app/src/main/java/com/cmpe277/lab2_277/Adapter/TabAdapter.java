package com.cmpe277.lab2_277.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.cmpe277.lab2_277.mytube.PlayListFragment;
import com.cmpe277.lab2_277.mytube.SearchFragment;


public class TabAdapter extends FragmentPagerAdapter {
    //No of tabs
    final int PAGE_COUNT = 2;

    //Tab titles
    private String tabTitles[] = new String[] { "Search", "Favourites" };
    private Context context;

    public TabAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Log.i("" +position,"ahhhh");
        if(position==0) //Tab 1: Search
            return SearchFragment.newInstance();

        else   //Tab 2: Favourites

             return PlayListFragment.newInstance();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }


}

