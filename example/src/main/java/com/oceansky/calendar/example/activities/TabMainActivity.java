package com.oceansky.calendar.example.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.oceansky.calendar.example.R;
import com.oceansky.calendar.example.utils.LogHelper;
import com.oceansky.calendar.example.utils.ToastUtil;

import rx.Subscription;

public class TabMainActivity extends FragmentActivity {

    private static final String TAG                         = TabMainActivity.class.getSimpleName();
    public static final  int    TAB_COURES                  = 0;
    public static final  int    TAB_CLASS                   = 1;
    public static final  int    TAB_MINE                    = 2;
    private static final int    PERMISSIONS_REQUEST_STORAGE = 1;

//    private IndicatorViewPager mIndicatorViewPager;
//    private SViewPager         mViewPager;
//    private IndicatorAdapter   mAdapter;
//    private Indicator          mIndicator;
//    private String[]           mTabNameArray;
    private long               mFirstTime;
    private int                mTitleBarColor;
    private String             mPushEvent;
    private IntentFilter       mIntentFilter;
//    private BaseDataSubscriber mBaseDataSubscriber;
//    private Subscription       mBaseDataSubscription;
//    private BindCidSubscriber  mBindCidSubscriber;
    private Subscription       mBindCidSubscription;

//    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            switch (intent.getAction()) {
//                case Constants.LOGIN_SUCCESS_MSG_BROADCAST:
//                    LogHelper.d(TAG, "receive LOGIN_SUCCESS_MSG_BROADCAST");
//                    gotoMsgCenter();
//                    break;
//                case Constants.LOGIN_SUCCESS_BROADCAST:
//                    LogHelper.d(TAG, "receive LOGIN_SUCCESS_BROADCAST");
//                    //登录成功后绑定Cid
//                    bindCid();
//                    break;
//                case Constants.ACTION_RECEIVE_GETUI_CID:
//                    LogHelper.d(TAG, "receive ACTION_RECEIVE_GETUI_CID");
//                    if (!TextUtils.isEmpty(SecurePreferences.getInstance(TabMainActivity.this, true).getString(Constants.KEY_ACCESS_TOKEN))) {
//                        bindCid();
//                    }
//                    break;
//            }
//        }
//    };

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_main_tab);
//        initBaseData();
//        mViewPager = (SViewPager) findViewById(R.id.main_tab_viewPager);
//        mIndicator = (Indicator) findViewById(R.id.main_tab_indicator);
//
//        mTabNameArray = getResources().getStringArray(R.array.main_tab_name);
//        mTitleBar.setTitleVisibility(false);
//        mTitleBar.setTitle(mTabNameArray[0]);
//        mTitleBar.mBackButton.setVisibility(View.GONE);

//        mIndicatorViewPager = new IndicatorViewPager(mIndicator, mViewPager);
//        mAdapter = new IndicatorAdapter(getSupportFragmentManager());
//        mIndicatorViewPager.setAdapter(mAdapter);
//        // 禁止viewpager的滑动事件
//        mViewPager.setCanScroll(false);
//        // 设置viewpager保留界面不重新加载的页面数量
//        mViewPager.setOffscreenPageLimit(3);
//        mIndicatorViewPager.setOnIndicatorPageChangeListener(new IndicatorViewPager.OnIndicatorPageChangeListener() {
//            @Override
//            public void onIndicatorPageChange(int preItem, int currentItem) {
//                mTitleBar.setTitle(mTabNameArray[currentItem]);
//                switch (currentItem) {
//                    case TAB_COURES:
//                        LogHelper.d(TAG, "TAB_COURES");
//                        mTitleBar.setTitleVisibility(false);
//                        mTitleBar.setBackButton(0, null);
//                        mTitleBar.setSettingButton(0, null);
//                        MobclickAgent.onEvent(TabMainActivity.this, "jhyx_tap_tab_schedule");
//                        break;
//                    case TAB_CLASS:
//                        mTitleBar.setTitleVisibility(true);
//                        mTitleBar.setBackButton(0, null);
//                        mTitleBar.setSettingButton(0, null);
//                        MobclickAgent.onEvent(TabMainActivity.this, "jhyx_tap_tab_class");
//                        break;
//                    case TAB_MINE:
//                        mTitleBar.setTitleVisibility(false);
//                        MobclickAgent.onEvent(TabMainActivity.this, "jhyx_tap_tab_mine");
//                        break;
//                }
//            }
//        });
//        registerReceiver();
//        initGetui();

//        mPushEvent = getIntent().getStringExtra(Constants.PUSH_EVENT);
//        if (mPushEvent == null) {
//            //不是通过通知进入时，检查更新
////            cheackUpdate();
//            //首次进入默认选择今日
//            MobclickAgent.onEvent(TabMainActivity.this, "jhyx_tap_tab_schedule");
//            return;
//        }
//
//        boolean isLogined = !TextUtils.isEmpty(SecurePreferences.getInstance(this, true).getString(Constants.KEY_ACCESS_TOKEN));
//        switch (mPushEvent) {
//            case Constants.EVENT_ORDER:
//                mIndicatorViewPager.setCurrentItem(TAB_MINE, false);
////                if (isLogined) {
////                    startActivity(new Intent(this, OrdersActivity.class));
////                } else {
////                    startActivity(new Intent(this, LoginActivity.class));
////                }
//                break;
//            case Constants.EVENT_COURSE:
//                mIndicatorViewPager.setCurrentItem(TAB_MINE, false);
////                if (isLogined) {
////                    startActivity(new Intent(this, CourseActivity.class));
////                } else {
////                    startActivity(new Intent(this, LoginActivity.class));
////                }
//                break;
//            case Constants.EVENT_MSG_NOTIFY_BEGIN:
//            case Constants.EVENT_MSG_COURSE_DELAY:
//            case Constants.EVENT_MSG_COURSE_END:
//            case Constants.EVENT_MSG_SALARY:
//            case Constants.EVENT_MSG_EVALUATE:
//                mIndicatorViewPager.setCurrentItem(TAB_COURES, false);
//                if (isLogined) {
//                    gotoMsgCenter();
//                } else {
////                    Intent intent = new Intent(this, LoginActivity.class);
////                    intent.putExtra(Constants.REQUEST_CODE, Constants.REQUEST_MSG);
////                    startActivity(intent);
//                }
//                break;
//            case Constants.EVENT_MSG_OPERATION:
//            case Constants.EVENT_MSG_BLESSING:
//                mIndicatorViewPager.setCurrentItem(TAB_MINE, false);
//                if (isLogined) {
//                    gotoMsgCenter();
//                } else {
////                    Intent intent = new Intent(this, LoginActivity.class);
////                    intent.putExtra(Constants.REQUEST_CODE, Constants.REQUEST_MSG);
////                    startActivity(intent);
//                }
//                break;
//            case Constants.EVENT_MSG_HWREPORT:
//                mIndicatorViewPager.setCurrentItem(TAB_MINE, false);
//                String push_data = getIntent().getStringExtra(Constants.PUSH_DATA);
//                if (isLogined) {
////                    gotoHomeWorkReport(push_data);
//                } else {
////                    Intent intent = new Intent(this, LoginActivity.class);
////                    intent.putExtra(Constants.REQUEST_CODE, Constants.REQUEST_MSG);
////                    startActivity(intent);
//                }
//                break;
//        }
    }

    private void registerReceiver() {
        LogHelper.d(TAG, "registerReceiver");
//        mIntentFilter = new IntentFilter();
//        mIntentFilter.addAction(Constants.LOGIN_SUCCESS_MSG_BROADCAST);
//        mIntentFilter.addAction(Constants.ACTION_RECEIVE_GETUI_CID);
//        mIntentFilter.addAction(Constants.LOGIN_SUCCESS_BROADCAST);
//        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }




    // 需要先跳转到作业列表 ---已完成作业列表
//    private void gotoHomeWorkReport(String data) {
//        Intent blessingIntent = new Intent(this, HomeworkActivity.class);
//        try {
//            JSONObject jsonObject = new JSONObject(data);
//            int homework_id = jsonObject.getInt(Constants.HOMEWORK_ID);
//            int course_id = jsonObject.getInt(Constants.COURSE_ID);
//            String title = jsonObject.getString(Constants.WEBVIEW_TITLE);
//            blessingIntent.putExtra(Constants.HOMEWORK_ID, homework_id);
//            blessingIntent.putExtra(Constants.COURSE_ID, course_id);
//            blessingIntent.putExtra(Constants.WEBVIEW_TITLE, title + getString(R.string.homework_report));
//            LogHelper.d(TAG, "gotoHomeWorkReport  homework_id: " + homework_id + "  course_id: " + course_id + title);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        blessingIntent.putExtra(Constants.PUSH_DATA, data);
//        blessingIntent.putExtra(Constants.PUSH_EVENT, mPushEvent);
//        startActivity(blessingIntent);
//    }

    private void gotoMsgCenter() {
//        Intent blessingIntent = new Intent(this, MsgExpandCenterActivity.class);
//        blessingIntent.putExtra(Constants.PUSH_EVENT, mPushEvent);
//        startActivity(blessingIntent);
    }

//    @Override
//    protected void setStatusBar() {
//        mTitleBarColor = getResources().getColor(R.color.title_bar_bg);
//        StatusBarUtil.setColor(TabMainActivity.this, mTitleBarColor, mAlpha);
//    }

//    private class IndicatorAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {
//
//        private int[] mTabIcons = {R.drawable.main_tab_course_selector, R.drawable.main_tab_contact_selector,
//                R.drawable.main_tab_me_selector};
//        private LayoutInflater mInflater;
//
//        public IndicatorAdapter(FragmentManager fragmentManager) {
//            super(fragmentManager);
//            mInflater = LayoutInflater.from(getApplicationContext());
//        }
//
//        @Override
//        public int getCount() {
//            return 1;
//        }
//
//        @Override
//        public View getViewForTab(int position, View convertView, ViewGroup container) {
//            if (convertView == null) {
//                convertView = mInflater.inflate(R.layout.main_tab_item, container, false);
//            }
//            TextView textView = (TextView) convertView.findViewById(R.id.text);
//            textView.setText(mTabNameArray[position]);
//            ImageView icon = (ImageView) convertView.findViewById(R.id.img);
//            icon.setBackgroundResource(mTabIcons[position]);
//            return convertView;
//        }
//
//        @Override
//        public Fragment getFragmentForPage(int position) {
//            Fragment mainFragment = null;
//            switch (position) {
//                case 0:
//                    mainFragment = new TimeTableFragment();
//                    Bundle bundle = new Bundle();
//                    bundle.putBoolean("intent_boolean_lazyLoad", false);
//                    mainFragment.setArguments(bundle);
//                    break;
////                case 1:
////                    mainFragment = new ClassFragment();
////                    break;
////                case 2:
////                    mainFragment = new MineFragment();
////                    break;
//            }
//            return mainFragment;
//        }
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - mFirstTime > 2000) {
                ToastUtil.showToastBottom(this, "再按一次退出", Toast.LENGTH_SHORT);
                mFirstTime = secondTime;
                return true;
            } else {
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }





//    private void showTipDialog() {
//        CustomDialog.Builder builder = new CustomDialog.Builder(this);
//        builder.setTitle(R.string.prompt)
//                .setMessage(R.string.dialog_msg_update_permission_tip)
//                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
//                .setPositiveButton(R.string.btn_confirm_set, (dialog, which) -> {
//                    dialog.dismiss();
//                    startAppSettings();
//                }).create().show();
//    }

    /**
     * 启动当前应用设置页面
     */
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    /**
     * 确认所有的权限是否都已授权
     *
     * @param grantResults
     * @return
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        LogHelper.d(TAG, "onRequestPermissionsResult");
        if (requestCode == PERMISSIONS_REQUEST_STORAGE) {
            if (verifyPermissions(grantResults)) {
//                if (AndroidApplication.canUpdate) {
//                    BmobUpdateAgent.forceUpdate(this);
//                } else {
//                    BmobUpdateAgent.update(this);
//                }
            } else {
//                showTipDialog();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
//            unregisterReceiver(mBroadcastReceiver);
            LogHelper.d(TAG, "onDestroy unregisterReceiver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//



    @Override
    protected void onStop() {
        super.onStop();
//        if (mBaseDataSubscriber != null) {
//            mBaseDataSubscriber.onCancle();
//        }
//        if (mBaseDataSubscription != null && !mBaseDataSubscription.isUnsubscribed()) {
//            mBaseDataSubscription.unsubscribe();
//        }
//        if (mBindCidSubscriber != null) {
//            mBindCidSubscriber.onCancle();
//        }
        if (mBindCidSubscription != null && !mBindCidSubscription.isUnsubscribed()) {
            mBindCidSubscription.unsubscribe();
        }
    }
}
