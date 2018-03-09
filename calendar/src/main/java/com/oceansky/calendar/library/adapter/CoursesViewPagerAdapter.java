package com.oceansky.calendar.library.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.oceansky.calendar.library.constant.CaldroidCustomConstant;
import com.oceansky.calendar.library.fragments.BaseContentFragment;

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * 教师课表
 * User: 王旭国
 * Date: 16/6/16 10:42
 * Email:wangxuguo@jhyx.com.cn
 */
public class CoursesViewPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = CoursesViewPagerAdapter.class.getSimpleName();
    private DateTime dateTime;
    private static ArrayList<BaseContentFragment> fragments;
    public CoursesViewPagerAdapter(FragmentManager childFragmentManager, DateTime dateTime) {
        super(childFragmentManager);
        this.dateTime = dateTime;
        getFragments();
    }

    public ArrayList<BaseContentFragment> getFragments() {
        if (fragments == null) {
            fragments = new ArrayList<BaseContentFragment>();
            fragments.add(new BaseContentFragment());
            fragments.add(new BaseContentFragment());
            fragments.add(new BaseContentFragment());
            fragments.add(new BaseContentFragment());

        }
        return fragments;
    }
    @Override
    public int getCount() {
        // warning: scrolling to very high values (1,000,000+) results in
        // strange drawing behaviour
        return CaldroidCustomConstant.NUMBER_OF_PAGES;
//        return Integer.MAX_VALUE;
    }
    @Override
    public Fragment getItem(int position) {
//        Log.d(TAG,"getItem: "+position);
//        int virtualPosition = (position) % getRealCount();
        return getFragments().get(position);
    }

}
