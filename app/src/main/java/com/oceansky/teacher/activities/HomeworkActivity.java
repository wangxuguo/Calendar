package com.oceansky.teacher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.fragments.HomeworkFragment;
import com.oceansky.teacher.utils.LogHelper;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.viewpager.SViewPager;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeworkActivity extends BaseActivityWithLoadingState {
    private static final String TAG = HomeworkActivity.class.getSimpleName();

    private String[]           mTabNameArray;
    private IndicatorViewPager mIndicatorViewPager;
    private IndicatorAdapter   mAdapter;

    @Bind(R.id.back)
    ImageButton mBtnBack;

    @Bind(R.id.settings)
    ImageButton mBtnNewHomework;

    @Bind(R.id.tabmain_indicator)
    Indicator mIndicator;

    @Bind(R.id.tabmain_viewPager)
    SViewPager mViewPager;
    private String mPushEvent;
    private int    mCurrentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework);
        Intent intent = getIntent();
        mPushEvent = intent.getStringExtra(Constants.PUSH_EVENT);
        if (mPushEvent != null) {
            intent.setClass(this, HomeWorkReportActivity.class);
            mCurrentItem = 1;
            startActivity(intent);
        }
        ButterKnife.bind(this);
        RxBus.getInstance().register(this);
        initView();
    }
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        LogHelper.d(TAG, "onAttachFragment: mPushEvent: " + mPushEvent + " mCurrentItem : " + mCurrentItem);
        if (mViewPager != null) {
            if (mPushEvent != null) {
                mViewPager.setCurrentItem(mCurrentItem);
                LogHelper.d(TAG, "mViewPager.setCurrentItem 1");
            } else {
                mViewPager.setCurrentItem(mCurrentItem);
            }
        }
    }

    private void initView() {
        mBtnBack.setImageResource(R.mipmap.icon_back_white);
        mBtnNewHomework.setImageResource(R.mipmap.btn_add);
        mTabNameArray = getResources().getStringArray(R.array.homework_tab_name);
        mIndicatorViewPager = new IndicatorViewPager(mIndicator, mViewPager);
        mAdapter = new IndicatorAdapter(getSupportFragmentManager());
        mIndicatorViewPager.setAdapter(mAdapter);
        // 设置viewpager保留界面不重新加载的页面数量
        mViewPager.setOffscreenPageLimit(mTabNameArray.length);
        mViewPager.setCanScroll(false);
        mIndicatorViewPager.setOnIndicatorPageChangeListener((preItem, currentItem) -> {
            switch (currentItem) {
                case 0:
                    MobclickAgent.onEvent(HomeworkActivity.this, Constants.HOMEWORK_PENDING_TAB_TOUCHED);
                    break;
                case 1:
                    MobclickAgent.onEvent(HomeworkActivity.this, Constants.HOMEWORK_DONE_TAB_TOUCHED);
                    break;
            }
        });
    }

    @Override
    protected void setTitleBar() {

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
                int item_tab = 0;
                switch (position) {
                    case 0:
                        item_tab = R.layout.item_homework_tab_left;
                        break;
                    case 1:
                        item_tab = R.layout.item_homework_tab_right;
                        break;
                }
                convertView = mInflater.inflate(item_tab, container, false);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.homework_tab_tv_title);
            textView.setText(mTabNameArray[position]);
            return convertView;
        }

        @Override
        public Fragment getFragmentForPage(int position) {
            LogHelper.d(TAG, "getFragmentForPage:" + position);
            Fragment mainFragment = null;
            switch (position) {
                case 0:
                    mainFragment = new HomeworkFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.HOMEWORK_STATE, Constants.HOMEWORK_STATE_PENDING);
                    bundle.putBoolean("intent_boolean_lazyLoad", false);//HomeworkFragmemt中的上拉加载更多控件需要传递此参数，否则无效
                    mainFragment.setArguments(bundle);
                    break;
                case 1:
                    mainFragment = new HomeworkFragment();
                    Bundle bundle1 = new Bundle();
                    bundle1.putInt(Constants.HOMEWORK_STATE, Constants.HOMEWORK_STATE_DONE);
                    bundle1.putBoolean("intent_boolean_lazyLoad", false);
                    mainFragment.setArguments(bundle1);
                    break;
            }
            return mainFragment;
        }
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    @OnClick(R.id.settings)
    public void newHomework() {
        MobclickAgent.onEvent(HomeworkActivity.this, Constants.CREATE_NEW_HOMEWORK_TOUCHED);
        startActivity(new Intent(this, CreatHomeworkActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(this);
    }
}
