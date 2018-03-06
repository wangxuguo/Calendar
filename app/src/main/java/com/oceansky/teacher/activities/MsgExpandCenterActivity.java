package com.oceansky.teacher.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.fragments.MSGFragment;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.RedPointEntity;
import com.oceansky.teacher.network.subscribers.BaseSubscriber;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.viewpager.SViewPager;
import com.umeng.analytics.MobclickAgent;

import rx.Subscription;

/**
 * 新的消息中心,和家长端保持一致
 * User: 王旭国
 * Date: 16/8/18 15:09
 * Email:wangxuguo@jhyx.com.cn
 */
public class MsgExpandCenterActivity extends BaseActivity {
    private static final String TAG           = MsgExpandCenterActivity.class.getSimpleName();
    private static final int    TAB_COMMONMSG = 0;
    private static final int    TAB_PERSONAL  = 1;


    private String[]           mTabNameArray;
    private IndicatorViewPager mIndicatorViewPager;
    private SViewPager         mViewPager;
    private IndicatorAdapter   mAdapter;
    private View               mIndicatorCommon;
    private View               mIndicatorPersonal;
    private int                mPriMsgCount;
    private int                mPubMsgCount;
    private String             mPushEvent;
    private int                mCurrentItem;
    private boolean mIsFirstRequest = true;
    private RedPointSubscriber mRedPointSubscriber;
    private Subscription       mRedPointSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_msg);
        super.onCreate(savedInstanceState);
        int pubMsgCount = getIntent().getIntExtra(Constants.COMMON_MSG_SUM, 0);
        int priMsgCount = getIntent().getIntExtra(Constants.PRI_MSG_SUM, 0);
        mPushEvent = getIntent().getStringExtra(Constants.PUSH_EVENT);
        Log.d(TAG, "mPushEvent " + mPushEvent + " pubMsgCount:  " + pubMsgCount + "  priMsgCount:  " + priMsgCount);
        if (mPushEvent != null) {
            switch (mPushEvent) {
                case Constants.EVENT_ORDER:
                case Constants.EVENT_COURSE:
                case Constants.EVENT_MSG_NOTIFY_BEGIN:
                case Constants.EVENT_MSG_COURSE_DELAY:
                case Constants.EVENT_MSG_COURSE_END:
                case Constants.EVENT_MSG_SALARY:
                case Constants.EVENT_MSG_EVALUATE:
                case Constants.EVENT_MSG_OPERATION:
                case Constants.EVENT_MSG_HWREPORT:
                    mCurrentItem = 1;
                    break;
                case Constants.EVENT_MSG_BLESSING:
                    mCurrentItem = 0;
                    break;
            }
        } else {  //不是从推送进入就是点击进入的,需要遵循 点击逻辑1
            if (pubMsgCount > 0) {
                mCurrentItem = 0;
            } else if (priMsgCount == 0) {
                mCurrentItem = 0;
            } else {
                mCurrentItem = 1;
            }

        }
        initView();
        if (priMsgCount > 0) {
            mIndicatorPersonal.setVisibility(View.VISIBLE);
        } else {
            mIndicatorPersonal.setVisibility(View.GONE);
        }
        if (pubMsgCount > 0) {
            mIndicatorCommon.setVisibility(View.VISIBLE);
        } else {
            mIndicatorCommon.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        Log.d(TAG, "onAttachFragment: mPushEvent: " + mPushEvent + " mCurrentItem : " + mCurrentItem);
        if (mViewPager != null) {
            if (mPushEvent != null) {
                //                mIndicatorViewPager.setCurrentItem(mCurrentItem, false);
                mViewPager.setCurrentItem(mCurrentItem);
                Log.d(TAG, "mViewPager.setCurrentItem 1");
            } else {
                mViewPager.setCurrentItem(mCurrentItem);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshRedPointStatus();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.msgcenter));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mTabNameArray = getResources().getStringArray(R.array.msgcenter_tab_name);
        mViewPager = (SViewPager) findViewById(R.id.tabmain_viewPager);
        Indicator indicator = (Indicator) findViewById(R.id.tabmain_indicator);
        mIndicatorViewPager = new IndicatorViewPager(indicator, mViewPager);
        mAdapter = new IndicatorAdapter(getSupportFragmentManager());
        mIndicatorViewPager.setAdapter(mAdapter);
        // 设置viewpager保留界面不重新加载的页面数量
        mViewPager.setOffscreenPageLimit(mTabNameArray.length);
        mViewPager.setCanScroll(true);
        mIndicatorCommon = indicator.getItemView(TAB_COMMONMSG).findViewById(R.id.iv_msg_indicator);
        mIndicatorPersonal = indicator.getItemView(TAB_PERSONAL).findViewById(R.id.iv_msg_indicator);
        String pushEvent = getIntent().getStringExtra(Constants.PUSH_EVENT);
        LogHelper.d(TAG, "pushEvent: " + pushEvent);
        if (mPubMsgCount > 0) {
            mIndicatorCommon.setVisibility(View.VISIBLE);
        } else {
            mIndicatorCommon.setVisibility(View.GONE);
        }
        if (mPriMsgCount > 0) {
            mIndicatorPersonal.setVisibility(View.VISIBLE);
        } else {
            mIndicatorPersonal.setVisibility(View.GONE);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_PUBMSG_COUNT);
        intentFilter.addAction(Constants.ACTION_PRIMSG_COUNT);
//        intentFilter.addAction(Constants.ACTION_COURES_CHANGE);
//        intentFilter.addAction(Constants.ACTION_ORDER_CHANGE_INVALID);
//        intentFilter.addAction(Constants.ACTION_ORDER_CHANGE_REFUND);
//        intentFilter.addAction(Constants.ACTION_HOMEWORK_NEWRECEIVE);
//        intentFilter.addAction(Constants.ACTION_HOMEWORK_WILLDEADLINE);
//        intentFilter.addAction(Constants.ACTION_COMMON_MSG);
//        intentFilter.addAction(Constants.SUBMIT_SUCCESS_BROADCAST);
//        intentFilter.addAction(Constants.ACTION_FINISH_HOMEWORK);
        intentFilter.addAction(Constants.ACTION_RECEIVE_PUSH);
        intentFilter.addAction(Constants.ACTION_MSG_COUNTDOWN);
        registerReceiver(mBroadcastReceiver, intentFilter);
        mIndicatorViewPager.setOnIndicatorPageChangeListener(new IndicatorViewPager.OnIndicatorPageChangeListener() {
            boolean isCommonFirst = true;
            boolean isPersonFirst = true;

            @Override
            public void onIndicatorPageChange(int i, int i1) {
                LogHelper.d(TAG, "i: " + i + "   i1: " + i1);
                if (i == 1) {
                    if (!isCommonFirst) {
                        MobclickAgent.onEvent(MsgExpandCenterActivity.this, "jhyx_tap_mine_message_public");
                    }
                    isCommonFirst = false;
                } else if (i == 0) {
                    if (!isPersonFirst) {
                        MobclickAgent.onEvent(MsgExpandCenterActivity.this, "jhyx_tap_mine_message_personal");
                    }
                    isPersonFirst = false;
                }
            }
        });
    }

    public void refreshRedPointStatus() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            final String token = "Bearer "
                    + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            mRedPointSubscriber = new MsgExpandCenterActivity.RedPointSubscriber(this);
            mRedPointSubscription = HttpManager.getRedPoint(token).subscribe(mRedPointSubscriber);

        }
    }

    class RedPointSubscriber extends BaseSubscriber<RedPointEntity> {
        public RedPointSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {

        }

        @Override
        protected void handleError(Throwable e) {

        }

        @Override
        public void onNext(RedPointEntity redPointEntity) {
            LogHelper.d(TAG, "redPointEntity: " + redPointEntity);
            if (redPointEntity != null) {
                RedPointEntity.DataBean.MsgBox msgBox = redPointEntity.getData().getMsgBox();
                if (msgBox == null) {
                    return;
                }
                if (msgBox.getTotal() > 0) {
                    int total = msgBox.getTotal();
                    mPriMsgCount = msgBox.getPri();
                    mPubMsgCount = msgBox.getPub();
                    if (mPriMsgCount > 0) {
                        mIndicatorPersonal.setVisibility(View.VISIBLE);
                    } else {
                        mIndicatorPersonal.setVisibility(View.GONE);
                    }
                    if (mPubMsgCount > 0) {
                        mIndicatorCommon.setVisibility(View.VISIBLE);
                    } else {
                        mIndicatorCommon.setVisibility(View.GONE);
                    }
                    if (mPriMsgCount == 0 && mPubMsgCount == 0) {  // 发送广播,防止我的界面再次请求红点
                        Intent intent = new Intent(Constants.ACTION_REDPOINT_STATE);
                        intent.putExtra(Constants.ACTION_REDPOINT_STATE, false);
                        sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(Constants.ACTION_REDPOINT_STATE);
                        intent.putExtra(Constants.ACTION_REDPOINT_STATE, true);
                        sendBroadcast(intent);
                    }
                } else {
                    mIndicatorPersonal.setVisibility(View.GONE);
                    mIndicatorCommon.setVisibility(View.GONE);
                }
            }
            mIsFirstRequest = false;
        }

    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
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
                convertView = mInflater.inflate(R.layout.item_msg_tab, container, false);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.msg_tab_tv_title);
            textView.setText(mTabNameArray[position]);
            if (position == mTabNameArray.length - 1) {
                convertView.findViewById(R.id.msg_tab_div_line).setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

        @Override
        public Fragment getFragmentForPage(int position) {
            LogHelper.d(TAG, "getFragmentForPage:" + position);
            Fragment mainFragment = null;
            switch (position) {
                case 0:
                    mainFragment = new MSGFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("intent_boolean_lazyLoad", false);
                    bundle.putString(Constants.MSG_TYPE, Constants.MSG_COMON);
                    mainFragment.setArguments(bundle);
                    break;
                case 1:
                    mainFragment = new MSGFragment();
                    Bundle bundle1 = new Bundle();
                    bundle1.putBoolean("intent_boolean_lazyLoad", false);
                    bundle1.putString(Constants.MSG_TYPE, Constants.MSG_PERSONAL);
                    mainFragment.setArguments(bundle1);
                    break;
            }
            return mainFragment;
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogHelper.d(TAG, "onReceive: " + intent.getAction());
            if (intent.getAction().equals(Constants.ACTION_PUBMSG_COUNT)) {
                boolean b = intent.getBooleanExtra(Constants.IS_HAVE_UNREADMSG, false);
                if (b) {
                    mIndicatorCommon.setVisibility(View.VISIBLE);
                } else {
                    refreshRedPointStatus();
                }
            }
            if (intent.getAction().equals(Constants.ACTION_PRIMSG_COUNT)) {
                boolean b = intent.getBooleanExtra(Constants.IS_HAVE_UNREADMSG, false);
                if (b) {
                    mIndicatorPersonal.setVisibility(View.VISIBLE);
                } else {
                    refreshRedPointStatus();
                }
            }

            String action = intent.getAction();
            switch (action) {
                case Constants.ACTION_MSG_COUNTDOWN:
                    String type = intent.getStringExtra(Constants.MSG_TYPE);
                    if (type.equals(Constants.MSG_COMON)) {
                        mPubMsgCount--;
                        if (mPubMsgCount == 0) {
                            mIndicatorCommon.setVisibility(View.GONE);
                        }
                    } else {
                        mPriMsgCount--;
                        if (mPriMsgCount == 0) {
                            mIndicatorPersonal.setVisibility(View.GONE);
                        }
                    }
                    if (mPriMsgCount == 0 && mPubMsgCount == 0) {  // 发送广播,防止我的界面再次请求红点
                        Intent mIntent = new Intent(Constants.ACTION_REDPOINT_STATE);
                        mIntent.putExtra(Constants.ACTION_REDPOINT_STATE, false);
                        sendBroadcast(mIntent);
                    }
                    break;
//                case Constants.ACTION_COURES_CHANGE:
//                case Constants.ACTION_ORDER_CHANGE_INVALID:
//                case Constants.ACTION_ORDER_CHANGE_REFUND:
//                case Constants.ACTION_HOMEWORK_NEWRECEIVE:
//                case Constants.ACTION_HOMEWORK_WILLDEADLINE:
//                case Constants.ACTION_FINISH_HOMEWORK:
//                case Constants.SUBMIT_SUCCESS_BROADCAST:  //作业提交
                case Constants.ACTION_RECEIVE_PUSH:
                    refreshRedPointStatus();
                    break;
                case Constants.ACTION_REDPOINT_STATE:
                    mIndicatorPersonal.setVisibility(View.VISIBLE);
                    break;
                case Constants.EVENT_MSG_BLESSING:     // 9001  教师端公共消息推送
                    mIndicatorCommon.setVisibility(View.VISIBLE);
                    break;
            }

        }
    };
}
