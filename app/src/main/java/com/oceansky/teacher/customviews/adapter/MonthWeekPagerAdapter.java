package com.oceansky.teacher.customviews.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.oceansky.teacher.constant.CaldroidCustomConstant;
import com.oceansky.teacher.fragments.DateGridFragment;

import java.util.ArrayList;

/**
 * Created by 王旭国 on 16/6/12 11:00
 */
public class MonthWeekPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<DateGridFragment> fragments;
    public ArrayList<DateGridFragment> getFragments(){
        if (fragments == null) {
            fragments = new ArrayList<DateGridFragment>();
            for(int i =0; i<getCount();i++ ){
                fragments.add(new DateGridFragment());

            }
        }
        return fragments;
    }
    public  void setFragments(ArrayList<DateGridFragment> fragments){
        this.fragments = fragments;
    }
    public MonthWeekPagerAdapter(FragmentManager childFragmentManager) {
        super(childFragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        return getFragments().get(position);
    }

    @Override
    public int getCount() {
        // We need 4 gridviews for previous month, current month and next month,
        // and 1 extra fragment for fragment recycle
        return CaldroidCustomConstant.NUMBER_OF_PAGES;
    }
}
