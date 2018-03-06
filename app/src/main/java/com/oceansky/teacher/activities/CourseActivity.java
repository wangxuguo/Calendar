package com.oceansky.teacher.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.fragments.MineCourseFragment;
import com.oceansky.teacher.utils.LogHelper;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.viewpager.SViewPager;
import com.umeng.analytics.MobclickAgent;

public class CourseActivity extends BaseActivityWithLoadingState {

    private static final String TAG = CourseActivity.class.getSimpleName();

    private String[]           mTabNameArray;
    private IndicatorViewPager mIndicatorViewPager;
    private SViewPager         mViewPager;
    private IndicatorAdapter   mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        initView();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_bar_course));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mTabNameArray = getResources().getStringArray(R.array.course_tab_name);
        mViewPager = (SViewPager) findViewById(R.id.tabmain_viewPager);
        Indicator indicator = (Indicator) findViewById(R.id.tabmain_indicator);
        mIndicatorViewPager = new IndicatorViewPager(indicator, mViewPager);
        mAdapter = new IndicatorAdapter(getSupportFragmentManager());
        mIndicatorViewPager.setAdapter(mAdapter);
        // 设置viewpager保留界面不重新加载的页面数量
        mViewPager.setOffscreenPageLimit(mTabNameArray.length);
        mViewPager.setCanScroll(true);
        mIndicatorViewPager.setOnIndicatorPageChangeListener(new IndicatorViewPager.OnIndicatorPageChangeListener() {
            @Override
            public void onIndicatorPageChange(int preItem, int currentItem) {
                switch (currentItem) {
                    case 0:
                        MobclickAgent.onEvent(CourseActivity.this, "jhyx_tap_mine_course_all");
                        break;
                    case 1:
                        MobclickAgent.onEvent(CourseActivity.this, "jhyx_tap_mine_course_in");
                        break;
                    case 2:
                        MobclickAgent.onEvent(CourseActivity.this, "jhyx_tap_mine_course_pretend");
                        break;
                    case 3:
                        MobclickAgent.onEvent(CourseActivity.this, "jhyx_tap_mine_course_finished");
                        break;
                }
            }
        });
    }

    private class IndicatorAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {
        private LayoutInflater mInflater;

        public IndicatorAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            mInflater = LayoutInflater.from(getApplicationContext());
        }

        @Override
        public int getCount() {
            return mTabNameArray.length;
        }

        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_course_tab, container, false);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.course_tab_tv_title);
            textView.setText(mTabNameArray[position]);
            if (position == mTabNameArray.length - 1) {
                convertView.findViewById(R.id.course_tab_div_line).setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

        @Override
        public Fragment getFragmentForPage(int position) {
            LogHelper.d(TAG, "getFragmentForPage:" + position);
            Fragment mainFragment = null;
            switch (position) {
                case 0:
                    mainFragment = new MineCourseFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.COURSE_STATUS, Constants.COURSE_STATE_ALL);
                    mainFragment.setArguments(bundle);
                    break;
                case 1:
                    mainFragment = new MineCourseFragment();
                    Bundle bundle1 = new Bundle();
                    bundle1.putInt(Constants.COURSE_STATUS, Constants.COURSE_STATE_ING);
                    mainFragment.setArguments(bundle1);
                    break;
                case 2:
                    mainFragment = new MineCourseFragment();
                    Bundle bundle2 = new Bundle();
                    bundle2.putInt(Constants.COURSE_STATUS, Constants.COURSE_STATE_WAIT);
                    mainFragment.setArguments(bundle2);
                    break;
                case 3:
                    mainFragment = new MineCourseFragment();
                    Bundle bundle3 = new Bundle();
                    bundle3.putInt(Constants.COURSE_STATUS, Constants.COURSE_STATE_END);
                    mainFragment.setArguments(bundle3);
                    break;
            }
            return mainFragment;
        }
    }
}
