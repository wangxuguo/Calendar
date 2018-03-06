package com.oceansky.teacher.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.igexin.sdk.PushManager;
import com.jaeger.library.StatusBarUtil;
import com.oceansky.teacher.AndroidApplication;
import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.customviews.CustomDialog;
import com.oceansky.teacher.fragments.ClassFragment;
import com.oceansky.teacher.fragments.MineFragment;
import com.oceansky.teacher.fragments.TimeTableFragment;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.BaseDataEntity;
import com.oceansky.teacher.network.response.SimpleResponse;
import com.oceansky.teacher.network.subscribers.BaseSubscriber;
import com.oceansky.teacher.network.transformer.DefaultSchedulerTransformer;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.SharePreferenceUtils;
import com.oceansky.teacher.utils.ToastUtil;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.viewpager.SViewPager;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import cn.bmob.v3.update.UpdateStatus;
import rx.Subscription;

public class TabMainActivity extends BaseActivityWithLoadingState {

    private static final String TAG                         = TabMainActivity.class.getSimpleName();
    public static final  int    TAB_COURES                  = 0;
    public static final  int    TAB_CLASS                   = 1;
    public static final  int    TAB_MINE                    = 2;
    private static final int    PERMISSIONS_REQUEST_STORAGE = 1;

    private IndicatorViewPager mIndicatorViewPager;
    private SViewPager         mViewPager;
    private IndicatorAdapter   mAdapter;
    private Indicator          mIndicator;
    private String[]           mTabNameArray;
    private long               mFirstTime;
    private int                mTitleBarColor;
    private String             mPushEvent;
    private IntentFilter       mIntentFilter;
    private BaseDataSubscriber mBaseDataSubscriber;
    private Subscription       mBaseDataSubscription;
    private BindCidSubscriber  mBindCidSubscriber;
    private Subscription       mBindCidSubscription;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.LOGIN_SUCCESS_MSG_BROADCAST:
                    LogHelper.d(TAG, "receive LOGIN_SUCCESS_MSG_BROADCAST");
                    gotoMsgCenter();
                    break;
                case Constants.LOGIN_SUCCESS_BROADCAST:
                    LogHelper.d(TAG, "receive LOGIN_SUCCESS_BROADCAST");
                    //登录成功后绑定Cid
                    bindCid();
                    break;
                case Constants.ACTION_RECEIVE_GETUI_CID:
                    LogHelper.d(TAG, "receive ACTION_RECEIVE_GETUI_CID");
                    if (!TextUtils.isEmpty(SecurePreferences.getInstance(TabMainActivity.this, true).getString(Constants.KEY_ACCESS_TOKEN))) {
                        bindCid();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_main_tab);
        initBaseData();
        mViewPager = (SViewPager) findViewById(R.id.main_tab_viewPager);
        mIndicator = (Indicator) findViewById(R.id.main_tab_indicator);

        mTabNameArray = getResources().getStringArray(R.array.main_tab_name);
        mTitleBar.setTitleVisibility(false);
        mTitleBar.setTitle(mTabNameArray[0]);
        mTitleBar.mBackButton.setVisibility(View.GONE);

        mIndicatorViewPager = new IndicatorViewPager(mIndicator, mViewPager);
        mAdapter = new IndicatorAdapter(getSupportFragmentManager());
        mIndicatorViewPager.setAdapter(mAdapter);
        // 禁止viewpager的滑动事件
        mViewPager.setCanScroll(false);
        // 设置viewpager保留界面不重新加载的页面数量
        mViewPager.setOffscreenPageLimit(3);
        mIndicatorViewPager.setOnIndicatorPageChangeListener(new IndicatorViewPager.OnIndicatorPageChangeListener() {
            @Override
            public void onIndicatorPageChange(int preItem, int currentItem) {
                mTitleBar.setTitle(mTabNameArray[currentItem]);
                switch (currentItem) {
                    case TAB_COURES:
                        LogHelper.d(TAG, "TAB_COURES");
                        mTitleBar.setTitleVisibility(false);
                        mTitleBar.setBackButton(0, null);
                        mTitleBar.setSettingButton(0, null);
                        MobclickAgent.onEvent(TabMainActivity.this, "jhyx_tap_tab_schedule");
                        break;
                    case TAB_CLASS:
                        mTitleBar.setTitleVisibility(true);
                        mTitleBar.setBackButton(0, null);
                        mTitleBar.setSettingButton(0, null);
                        MobclickAgent.onEvent(TabMainActivity.this, "jhyx_tap_tab_class");
                        break;
                    case TAB_MINE:
                        mTitleBar.setTitleVisibility(false);
                        MobclickAgent.onEvent(TabMainActivity.this, "jhyx_tap_tab_mine");
                        break;
                }
            }
        });
        registerReceiver();
        initGetui();

        mPushEvent = getIntent().getStringExtra(Constants.PUSH_EVENT);
        if (mPushEvent == null) {
            //不是通过通知进入时，检查更新
            cheackUpdate();
            //首次进入默认选择今日
            MobclickAgent.onEvent(TabMainActivity.this, "jhyx_tap_tab_schedule");
            return;
        }

        boolean isLogined = !TextUtils.isEmpty(SecurePreferences.getInstance(this, true).getString(Constants.KEY_ACCESS_TOKEN));
        switch (mPushEvent) {
            case Constants.EVENT_ORDER:
                mIndicatorViewPager.setCurrentItem(TAB_MINE, false);
                if (isLogined) {
                    startActivity(new Intent(this, OrdersActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
                break;
            case Constants.EVENT_COURSE:
                mIndicatorViewPager.setCurrentItem(TAB_MINE, false);
                if (isLogined) {
                    startActivity(new Intent(this, CourseActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
                break;
            case Constants.EVENT_MSG_NOTIFY_BEGIN:
            case Constants.EVENT_MSG_COURSE_DELAY:
            case Constants.EVENT_MSG_COURSE_END:
            case Constants.EVENT_MSG_SALARY:
            case Constants.EVENT_MSG_EVALUATE:
                mIndicatorViewPager.setCurrentItem(TAB_COURES, false);
                if (isLogined) {
                    gotoMsgCenter();
                } else {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra(Constants.REQUEST_CODE, Constants.REQUEST_MSG);
                    startActivity(intent);
                }
                break;
            case Constants.EVENT_MSG_OPERATION:
            case Constants.EVENT_MSG_BLESSING:
                mIndicatorViewPager.setCurrentItem(TAB_MINE, false);
                if (isLogined) {
                    gotoMsgCenter();
                } else {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra(Constants.REQUEST_CODE, Constants.REQUEST_MSG);
                    startActivity(intent);
                }
                break;
            case Constants.EVENT_MSG_HWREPORT:
                mIndicatorViewPager.setCurrentItem(TAB_MINE, false);
                String push_data = getIntent().getStringExtra(Constants.PUSH_DATA);
                if (isLogined) {
                    gotoHomeWorkReport(push_data);
                } else {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra(Constants.REQUEST_CODE, Constants.REQUEST_MSG);
                    startActivity(intent);
                }
                break;
        }
    }

    private void registerReceiver() {
        LogHelper.d(TAG, "registerReceiver");
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.LOGIN_SUCCESS_MSG_BROADCAST);
        mIntentFilter.addAction(Constants.ACTION_RECEIVE_GETUI_CID);
        mIntentFilter.addAction(Constants.LOGIN_SUCCESS_BROADCAST);
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    /**
     * 初始化个推SDK
     * 注：该方法必须在Activity或Service类内调用，一般情况下，可以在Activity的onCreate()方法中调用。
     * 由于应用每启动一个新的进程，就会调用一次Application的onCreate()方法，而个推SDK是一个独立的进程，
     * 因此如果在Application的onCreate()中调用intialize接口，会导致SDK初始化在一个应用中多次调用，
     * 所以不建议在Application继承类中调用个推SDK初始化接口。
     */
    private void initGetui() {
        LogHelper.d(TAG, "initGetui");
        PushManager pushManager = PushManager.getInstance();
        pushManager.initialize(this.getApplicationContext());
        String uid = SecurePreferences.getInstance(this, true).getString(Constants.KEY_USER_ID);
        LogHelper.d(TAG, "uid:" + uid);
        if (!TextUtils.isEmpty(uid)) {
            pushManager.bindAlias(this, uid);
        }
    }

    private void bindCid() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            LogHelper.d(TAG, "bindCid");
            final String token =
                    "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            String cid = SharePreferenceUtils.getStringPref(this, Constants.GT_CLIENT_ID, "");
            LogHelper.d(TAG, "cid: " + cid);
            if (!TextUtils.isEmpty(cid)) {
                mBindCidSubscriber = new BindCidSubscriber(this);
                mBindCidSubscription = HttpManager.bindGetuiCid(token, cid)
                        .subscribe(mBindCidSubscriber);
            }
        }
    }

    private void initBaseData() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            mBaseDataSubscriber = new BaseDataSubscriber(this);
            mBaseDataSubscription = HttpManager.getBaseData()
                    .doOnNext(this::cacheBaseData)
                    .compose(new DefaultSchedulerTransformer<>())
                    .subscribe(mBaseDataSubscriber);
        }
    }

    private void cacheBaseData(BaseDataEntity baseDatas) {
        LogHelper.d(TAG, "cacheBaseData");
        try {
            List<BaseDataEntity.Data> educations = baseDatas.getEducations();
            if (educations != null) {
                ArrayList<String> educationNames = new ArrayList<>(educations.size());
                ArrayList<Integer> educationIds = new ArrayList<>(educations.size());
                for (BaseDataEntity.Data data : educations) {
                    educationIds.add(data.getId());
                    educationNames.add(data.getName());
                }
                Reservoir.put(Constants.BASE_DATA_EDUCATIONS_NAME, educationNames);
                Reservoir.put(Constants.BASE_DATA_EDUCATIONS_ID, educationIds);
            }

            List<BaseDataEntity.Data> lessons = baseDatas.getLessons();
            if (lessons != null) {
                ArrayList<String> lessonNameList = new ArrayList<>(lessons.size());
                ArrayList<Integer> lessonIdList = new ArrayList<>(lessons.size());
                for (BaseDataEntity.Data data : lessons) {
                    lessonIdList.add(data.getId());
                    lessonNameList.add(data.getName());
                }
                Reservoir.put(Constants.BASE_DATA_LESSONS_NAME, lessonNameList);
                Reservoir.put(Constants.BASE_DATA_LESSONS_ID, lessonIdList);
            }

            List<BaseDataEntity.Data> grades = baseDatas.getGrades();
            if (grades != null) {
                ArrayList<String> gradeNameList = new ArrayList<>(grades.size());
                ArrayList<Integer> gradeIdList = new ArrayList<>(grades.size());
                for (BaseDataEntity.Data data : grades) {
                    gradeIdList.add(data.getId());
                    gradeNameList.add(data.getName());
                }
                Reservoir.put(Constants.BASE_DATA_GRADES_NAME, gradeNameList);
                Reservoir.put(Constants.BASE_DATA_GRADES_ID, gradeIdList);
            }
            List<BaseDataEntity.Data> textbooks = baseDatas.getTextbook();
            if (textbooks != null) {
                ArrayList<String> textbookNames = new ArrayList<>(textbooks.size());
                ArrayList<Integer> textbookIds = new ArrayList<>(textbooks.size());
                for (BaseDataEntity.Data data : textbooks) {
                    textbookIds.add(data.getId());
                    textbookNames.add(data.getName());
                }
                Reservoir.put(Constants.BASE_DATA_TEXTBOOK_NAME, textbookNames);
                Reservoir.put(Constants.BASE_DATA_TEXTBOOK_ID, textbookIds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 需要先跳转到作业列表 ---已完成作业列表
    private void gotoHomeWorkReport(String data) {
        Intent blessingIntent = new Intent(this, HomeworkActivity.class);
        try {
            JSONObject jsonObject = new JSONObject(data);
            int homework_id = jsonObject.getInt(Constants.HOMEWORK_ID);
            int course_id = jsonObject.getInt(Constants.COURSE_ID);
            String title = jsonObject.getString(Constants.WEBVIEW_TITLE);
            blessingIntent.putExtra(Constants.HOMEWORK_ID, homework_id);
            blessingIntent.putExtra(Constants.COURSE_ID, course_id);
            blessingIntent.putExtra(Constants.WEBVIEW_TITLE, title + getString(R.string.homework_report));
            LogHelper.d(TAG, "gotoHomeWorkReport  homework_id: " + homework_id + "  course_id: " + course_id + title);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        blessingIntent.putExtra(Constants.PUSH_DATA, data);
        blessingIntent.putExtra(Constants.PUSH_EVENT, mPushEvent);
        startActivity(blessingIntent);
    }

    private void gotoMsgCenter() {
        Intent blessingIntent = new Intent(this, MsgExpandCenterActivity.class);
        blessingIntent.putExtra(Constants.PUSH_EVENT, mPushEvent);
        startActivity(blessingIntent);
    }

    @Override
    protected void setStatusBar() {
        mTitleBarColor = getResources().getColor(R.color.title_bar_bg);
        StatusBarUtil.setColor(TabMainActivity.this, mTitleBarColor, mAlpha);
    }

    private class IndicatorAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {

        private int[] mTabIcons = {R.drawable.main_tab_course_selector, R.drawable.main_tab_contact_selector,
                R.drawable.main_tab_me_selector};
        private LayoutInflater mInflater;

        public IndicatorAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            mInflater = LayoutInflater.from(getApplicationContext());
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.main_tab_item, container, false);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(mTabNameArray[position]);
            ImageView icon = (ImageView) convertView.findViewById(R.id.img);
            icon.setBackgroundResource(mTabIcons[position]);
            return convertView;
        }

        @Override
        public Fragment getFragmentForPage(int position) {
            Fragment mainFragment = null;
            switch (position) {
                case 0:
                    mainFragment = new TimeTableFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("intent_boolean_lazyLoad", false);
                    mainFragment.setArguments(bundle);
                    break;
//                case 1:
//                    mainFragment = new ClassFragment();
//                    break;
//                case 2:
//                    mainFragment = new MineFragment();
//                    break;
            }
            return mainFragment;
        }
    }

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

    /**
     * 检查自动更新
     */
    private void cheackUpdate() {
        /** 初始化AppVersion表
         * 注：
         1、initAppVersion方法适合开发者调试自动更新功能时使用，一旦AppVersion表在后台创建成功，建议屏蔽或删除此方法，否则会生成多行记录。
         2、如果调用了此方法后，在管理后台没有看见AppVersion表生成，建议到手机的应用管理界面清除该应用的数据，并再次调用该方法，也可到LogCat中查看与bmob相关错误日志。
         3、如果2方法尝试多次之后仍然无效，请手动创建AppVersion表
         * */
        //BmobUpdateAgent.initAppVersion(this);
        //true ：在WiFi接入情况下才进行自动提醒
        BmobUpdateAgent.setUpdateOnlyWifi(true);
        BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                if (updateStatus == UpdateStatus.Yes) {
                    //版本有更新
                    AndroidApplication.canUpdate = true;
                } else if (updateStatus == UpdateStatus.No) {
                    //版本无更新
                    AndroidApplication.canUpdate = false;
                } else if (updateStatus == UpdateStatus.EmptyField) {
                    //此提示只是提醒开发者关注那些必填项，测试成功后，无需对用户提示
                    //"请检查你AppVersion表的必填项，1、target_size（文件大小）是否填写；
                    // 2、path或者android_url两者必填其中一项。"
                } else if (updateStatus == UpdateStatus.IGNORED) {
                    //该版本已被忽略更新
                    AndroidApplication.canUpdate = true;
                } else if (updateStatus == UpdateStatus.ErrorSizeFormat) {
                    //检查target_size填写的格式，请使用file.length()方法获取apk大小
                } else if (updateStatus == UpdateStatus.TimeOut) {
                    //查询出错或查询超时
                }
            }
        });
        bmobUpdate();
    }

    private void bmobUpdate() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //拥有权限
                if (AndroidApplication.canUpdate) {
                    BmobUpdateAgent.forceUpdate(this);
                } else {
                    BmobUpdateAgent.update(this);
                }
            } else {
                //没有权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_STORAGE);
            }
        } else {
            ToastUtil.showToastBottom(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT);
        }
    }

    private void showTipDialog() {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setTitle(R.string.prompt)
                .setMessage(R.string.dialog_msg_update_permission_tip)
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.btn_confirm_set, (dialog, which) -> {
                    dialog.dismiss();
                    startAppSettings();
                }).create().show();
    }

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
                if (AndroidApplication.canUpdate) {
                    BmobUpdateAgent.forceUpdate(this);
                } else {
                    BmobUpdateAgent.update(this);
                }
            } else {
                showTipDialog();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mBroadcastReceiver);
            LogHelper.d(TAG, "onDestroy unregisterReceiver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BaseDataSubscriber extends BaseSubscriber<BaseDataEntity> {

        public BaseDataSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {

        }

        @Override
        protected void handleError(Throwable e) {

        }

        @Override
        public void onNext(BaseDataEntity baseDataEntity) {

        }
    }

    private class BindCidSubscriber extends BaseSubscriber<SimpleResponse> {

        public BindCidSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {

        }

        @Override
        protected void handleError(Throwable e) {

        }

        @Override
        public void onNext(SimpleResponse simpleResponse) {

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBaseDataSubscriber != null) {
            mBaseDataSubscriber.onCancle();
        }
        if (mBaseDataSubscription != null && !mBaseDataSubscription.isUnsubscribed()) {
            mBaseDataSubscription.unsubscribe();
        }
        if (mBindCidSubscriber != null) {
            mBindCidSubscriber.onCancle();
        }
        if (mBindCidSubscription != null && !mBindCidSubscription.isUnsubscribed()) {
            mBindCidSubscription.unsubscribe();
        }
    }
}
