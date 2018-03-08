package com.oceansky.calendar.example.fragments;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.igexin.sdk.PushManager;
import com.oceansky.calendar.example.constant.Constants;
import com.oceansky.calendar.example.constant.FeatureConfig;
import com.oceansky.calendar.example.network.http.ApiException;
import com.oceansky.calendar.example.network.response.TeacherInforEntity;
import com.oceansky.calendar.example.utils.ImageUtils;
import com.oceansky.calendar.example.utils.ToastUtil;
import com.oceansky.calendar.example.BuildConfig;
import com.oceansky.calendar.example.R;
import com.oceansky.calendar.example.customviews.BGAJHYXRefreshViewHolder;
import com.oceansky.calendar.example.customviews.RefreshScrollView;
import com.oceansky.calendar.example.network.http.HttpManager;
import com.oceansky.calendar.example.network.response.RedPointEntity;
import com.oceansky.calendar.example.network.subscribers.BaseSubscriber;
import com.oceansky.calendar.example.network.subscribers.LoadingSubscriber;
import com.oceansky.calendar.example.utils.LogHelper;
import com.oceansky.calendar.example.utils.NetworkUtils;
import com.oceansky.calendar.example.utils.SecurePreferences;
import com.oceansky.calendar.example.utils.SharePreferenceUtils;
import com.umeng.analytics.MobclickAgent;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscription;


public class MineFragment extends BaseLazyFragment implements View.OnClickListener,
        BGARefreshLayout.BGARefreshLayoutDelegate {
    private static final String TAG                     = MineFragment.class.getSimpleName();
    public static final  int    REQUEST_LOGIN           = 2000;
    public static final  int    REQUESR_TEACHER_PROFILE = 2001;
    private static final int    REQUEST_SETTING         = 2002;
    private static final int    REQUEST_MINE            = 2003;
    public static final  String ERROR_NO_INFOR          = "5000";
    public static final  String ERROR_TOKEN_INVALID     = "4013";

    private CircleImageView        mIvPhoto;
    private Context                mContext;
    private BGARefreshLayout       mRefreshLayout;
    private RefreshScrollView      mRefreshScrollView;
    private boolean                mIsRefreshing;
    private TextView               mVersionInfo;
    private IntentFilter           mIntentFilter;
    private boolean                mIsLogined;
    private TextView               mTvName;
    private RelativeLayout         mRlCourse;
    private RelativeLayout         mRlOrder;
    private RelativeLayout         mRlMsg;
    private RelativeLayout         mRlClass;
    private RelativeLayout         mRlHomework;
    private RelativeLayout         mRlSetting;
    private String                 mTeacherPhoto;
    private String                 mTeacherName;
    private View                   mTitlebar;
    private ImageView              mIndicator;
    private ImageView              mHomeWorkIndicator;
    private TeacherInforSubscriber mTeacherInforSubscriber;
    private int                    mPri;
    private int                    mPub;
    private Subscription           mTeacherInforSubscription;
    private RedPointSubscriber     mRedPointSubscriber;
    private Subscription           mRedPointSubscription;

    @Override
    void onErrorLayoutClick() {

    }

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_profile);
        super.onCreateViewLazy(savedInstanceState);
        mContext = getActivity();
        initView();
        initData();
        // 首次进入进行刷新
        refreshData();
    }

    @Override
    protected void onResumeLazy() {
        super.onResumeLazy();
        LogHelper.d(TAG, "onResumeLazy");
        if (mIsLogined) {
            getRedPointData();
        }
    }

    private void getRedPointData() {
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            final String token = "Bearer "
                    + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
            mRedPointSubscriber = new RedPointSubscriber(mContext);
            mRedPointSubscription = HttpManager.getRedPoint(token).subscribe(mRedPointSubscriber);
            //            mHomeWorkRedPointSubscriber = new HomeWorkRedPointSubscriber(mContext);
            //            mHomeWorkRedPointSubscription = HttpManager.getHomeWorkRedPoint(token).subscribe(mHomeWorkRedPointSubscriber);
        }
    }

    private void initView() {
        initTitleBar();
        mVersionInfo = (TextView) findViewById(R.id.tv_version_information);
        mIvPhoto = (CircleImageView) findViewById(R.id.mine_civ_photo);
        mTvName = (TextView) findViewById(R.id.mine_tv_name);
        mRefreshLayout = (BGARefreshLayout) findViewById(R.id.mine_layout_refresh);
        mRefreshScrollView = (RefreshScrollView) findViewById(R.id.mine_scrollview_refresh);
        mRlCourse = (RelativeLayout) findViewById(R.id.profile_rl_course);
        mRlOrder = (RelativeLayout) findViewById(R.id.profile_rl_order);
        mRlMsg = (RelativeLayout) findViewById(R.id.profile_rl_msg);
        mRlClass = (RelativeLayout) findViewById(R.id.profile_rl_class);
        mRlHomework = (RelativeLayout) findViewById(R.id.profile_rl_homework);
        mRlSetting = (RelativeLayout) findViewById(R.id.profile_rl_setting);
        mIndicator = (ImageView) findViewById(R.id.iv_indicator);
        mHomeWorkIndicator = (ImageView) findViewById(R.id.iv_homework_indicator);

        if (FeatureConfig.DEBUG_LOG) {
            mVersionInfo.setText("VERSION_NAME:" + BuildConfig.VERSION_NAME + "   "
                    + "VERSION_CODE:" + BuildConfig.VERSION_CODE);
            mVersionInfo.setVisibility(View.VISIBLE);
        } else {
            mVersionInfo.setVisibility(View.GONE);
        }

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.LOGIN_SUCCESS_BROADCAST);
        mIntentFilter.addAction(Constants.ACTION_RECEIVE_PUSH);
        mIntentFilter.addAction(Constants.ACTION_PASSWORD_CHANGED);
        mIntentFilter.addAction(Constants.BROAD_MSGCENTER_HAVEREADALL);
        mIntentFilter.addAction(Constants.BROAD_PRI_MSG_READED);
        mContext.registerReceiver(mBroadcastReceiver, mIntentFilter);

        initRefreshLayout();
        mRefreshScrollView.setOnScrollListener(new RefreshScrollView.OnScrollListener() {
            @Override
            public void onScroll(int scrollY) {
                LogHelper.d(TAG, "mIsRefreshing: " + mIsRefreshing + " scrollY: " + scrollY);
                if (scrollY > 0 && mIsRefreshing) {
                    mIsRefreshing = false;
                    mRefreshLayout.endRefreshing();
                }
            }
        });
        mIsLogined = !TextUtils.isEmpty(SecurePreferences.getInstance(getActivity(), false).getString(Constants.KEY_ACCESS_TOKEN));
        boolean haveNewCommonMsg = SharePreferenceUtils.getBooleanPref(mContext, Constants.HAVE_COMMON_MSG, false);
        LogHelper.d(TAG, "haveNewCommonMsg: " + haveNewCommonMsg);
        refreshIndicator(haveNewCommonMsg);
    }

    private void initTitleBar() {
        mTitlebar = findViewById(R.id.mine_title_bar);
        ImageButton leftBtn = (ImageButton) mTitlebar.findViewById(R.id.back);
        leftBtn.setImageResource(R.mipmap.icon_profile_homepage);
        leftBtn.setOnClickListener(this);
        ImageButton rightBtn = (ImageButton) mTitlebar.findViewById(R.id.settings);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setImageResource(R.mipmap.icon_profile_edit);
        rightBtn.setOnClickListener(this);
    }

    private void initData() {
        mTeacherName = SharePreferenceUtils.getStringPref(mContext, Constants.TEAHER_NAME, "");
        mTeacherPhoto = SharePreferenceUtils.getStringPref(mContext, Constants.TEACHER_PHOTO, "");
        refreshView(mIsLogined);
        setListener();
    }

    private void setListener() {
        mIvPhoto.setOnClickListener(this);
        mRlCourse.setOnClickListener(this);
        mRlOrder.setOnClickListener(this);
        mRlMsg.setOnClickListener(this);
        mRlClass.setOnClickListener(this);
        mRlHomework.setOnClickListener(this);
        mRlSetting.setOnClickListener(this);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.LOGIN_SUCCESS_BROADCAST:
                    mIsLogined = true;
                    refreshData();
                    break;
                case Constants.ACTION_RECEIVE_PUSH:
                    LogHelper.d(TAG, "ACTION_RECEIVE_PUSH");
                    mIndicator.setVisibility(View.VISIBLE);
                    getRedPointData();
                    break;
                case Constants.ACTION_HOMEWORK_MSG:
                    getRedPointData();
                    break;
                case Constants.ACTION_PASSWORD_CHANGED:
                    LogHelper.d(TAG, "ACTION_PASSWORD_CHANGED");
                    loginOut();
                    break;
                case Constants.BROAD_MSGCENTER_HAVEREADALL:
                    // 消息中心的消息已经阅读完
                    LogHelper.d(TAG, "BROAD_MSGCENTER_HAVEREADALL");
                    mIndicator.setVisibility(View.INVISIBLE);
                    break;
                case Constants.BROAD_PRI_MSG_READED:
                    mPri = 0;
            }
        }
    };

    private void refreshView(boolean isLogined) {
        if (isLogined) {
            mTvName.setText(mTeacherName);
            ImageUtils.loadImage(mTeacherPhoto, mIvPhoto, R.mipmap.profile_photo_default);
            mTitlebar.setVisibility(View.VISIBLE);
        } else {
            mTvName.setText("点击登录");
            mIvPhoto.setImageResource(R.mipmap.profile_photo_default);
            mTitlebar.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshData() {
        mRefreshLayout.beginRefreshing();
    }

    @Override
    public void onClick(View v) {
        if (mIsLogined) {
            switch (v.getId()) {
                case R.id.mine_civ_photo:
                    MobclickAgent.onEvent(mContext, "jhyx_tap_mine_header");
                    gotoEditActivity();
                    break;
                case R.id.profile_rl_course:
                    MobclickAgent.onEvent(mContext, "jhyx_tap_mine_course");
//                    startActivity(new Intent(mContext, CourseActivity.class));
                    break;
                case R.id.profile_rl_order:
                    MobclickAgent.onEvent(mContext, "jhyx_tap_mine_order");
//                    startActivity(new Intent(mContext, OrdersActivity.class));
                    break;
                case R.id.profile_rl_msg:
                    MobclickAgent.onEvent(mContext, "jhyx_tap_mine_message");
//                    Intent intent1 = new Intent(mContext, MsgExpandCenterActivity.class);
                    //                    Intent intent1 = new Intent(mContext, MsgCenterActivity.class);
//                    intent1.putExtra(Constants.COMMON_MSG_SUM, mPub);
//                    intent1.putExtra(Constants.PRI_MSG_SUM, mPri);
//                    startActivity(intent1);
                    break;
                case R.id.profile_rl_class:
                    MobclickAgent.onEvent(mContext, "jhyx_tap_mine_class");
//                    startActivity(new Intent(mContext, ClassListActivity.class));
                    break;
                case R.id.profile_rl_homework:
                    // TODO: 16/8/9 统计事件
                    MobclickAgent.onEvent(mContext, Constants.MINE_HOMEWORK);
//                    startActivity(new Intent(mContext, HomeworkActivity.class));
                    break;
                case R.id.profile_rl_setting:
                    MobclickAgent.onEvent(mContext, "jhyx_tap_mine_setting");
//                    Intent intent = new Intent(mContext, SettingActivity.class);
//                    startActivityForResult(intent, REQUEST_SETTING);
                    break;
                case R.id.back:
                    MobclickAgent.onEvent(mContext, "jhyx_tap_mine_homepage");
                    int teacherStatus = SharePreferenceUtils.getIntPref(mContext, Constants.TEACHER_STATUS, 0);
                    if (teacherStatus == Constants.TEACHER_STATE_PASS) {
//                        Intent mineIntent = new Intent(mContext, BaseWebViewActivity.class);
//                        final String teacher_uid = SecurePreferences.getInstance(mContext, true).getString(Constants.KEY_TEACHER_ID);
//                        mineIntent.putExtra(Constants.WEBVIEW_URL, Constants.TEACHER_BASE_URL + "/" + teacher_uid + "/homepage");
//                        mineIntent.putExtra(Constants.WEBVIEW_TITLE, "教师详情");
//                        mineIntent.putExtra(Constants.WEBVIEW_BUTTON, "我的");
//                        startActivityForResult(mineIntent, REQUEST_MINE);
                        LogHelper.d(TAG, "homepage");
                    } else {
                        gotoEditActivity();
                    }
                    break;
                case R.id.settings:
                    MobclickAgent.onEvent(mContext, "jhyx_tap_mine_edit");
                    gotoEditActivity();
                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.profile_rl_setting:
                    MobclickAgent.onEvent(mContext, "jhyx_tap_mine_setting");
//                    Intent intent = new Intent(mContext, SettingActivity.class);
//                    startActivity(intent);
                    break;
                case R.id.profile_rl_msg:
                    MobclickAgent.onEvent(mContext, "jhyx_tap_mine_message");
//                    Intent intent1 = new Intent(mContext, LoginActivity.class);
//                    intent1.putExtra(Constants.REQUEST_CODE, Constants.REQUEST_MSG);
//                    startActivity(intent1);
                    break;
                default:
//                    startActivity(new Intent(mContext, LoginActivity.class));
            }
        }
    }

    private void gotoEditActivity() {
//        Intent editIntent = new Intent(mContext, EditActivity.class);
//        startActivityForResult(editIntent, REQUESR_TEACHER_PROFILE);
    }

    /**
     * 初始化刷新头
     */
    private void initRefreshLayout() {
        mRefreshLayout.setDelegate(this);
        mRefreshLayout.setIsShowLoadingMoreView(false);
        BGAJHYXRefreshViewHolder bgaNormalRefreshViewHolder = new BGAJHYXRefreshViewHolder(mContext, true);
        bgaNormalRefreshViewHolder.setPullDownImageResource(R.mipmap.loading_logo_00025);
        bgaNormalRefreshViewHolder.setChangeToReleaseRefreshAnimResId(R.anim.bga_refresh_mt_refreshing);
        bgaNormalRefreshViewHolder.setRefreshingAnimResId(R.anim.bga_refresh_mt_refreshing);
        bgaNormalRefreshViewHolder.setRefreshViewBackgroundColorRes(R.color.activity_bg_gray);
        bgaNormalRefreshViewHolder.setSpringDistanceScale(0);
        mRefreshLayout.setRefreshViewHolder(bgaNormalRefreshViewHolder);
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        mIsRefreshing = true;
        if (!mIsLogined) {
            mIsRefreshing = false;
            refreshLayout.endRefreshing();
            return;
        }
        //发送GET请求，获取信息
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            final String token = "Bearer "
                    + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
            mTeacherInforSubscriber = new TeacherInforSubscriber(mContext);
            mTeacherInforSubscription = HttpManager.getTeacherInfor(token).subscribe(mTeacherInforSubscriber);
        } else {
            ToastUtil.showToastBottom(mContext, R.string.toast_error_no_net, Toast.LENGTH_SHORT);
            mIsRefreshing = false;
            mRefreshLayout.endRefreshing();
        }
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
    }

    class TeacherInforSubscriber extends LoadingSubscriber<TeacherInforEntity> {
        public TeacherInforSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            Toast.makeText(mContext, R.string.toast_error_time_out, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void showLoading() {

        }

        @Override
        protected void dismissLoading() {
            mRefreshLayout.endRefreshing();
            mIsRefreshing = false;
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ApiException.TOKEN_INVALID:
//                    startActivity(new Intent(mContext, TokenInvalidDialogActivity.class));
                    break;
                default:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, false);
            }
        }

        @Override
        public void onNext(TeacherInforEntity teacherInforEntity) {
            LogHelper.d(TAG, "TeacherInforBean: " + teacherInforEntity);
            if (teacherInforEntity == null) {
                throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
            }
            mTeacherName = teacherInforEntity.getLast_name();
            mTeacherPhoto = teacherInforEntity.getAvatar();
            int sex = teacherInforEntity.getSex();
            String birthday = teacherInforEntity.getBirthday();
            int first_year = teacherInforEntity.getFirst_year();
            String graduate = teacherInforEntity.getGraduate();
            int education = teacherInforEntity.getEducation();
            String wechat = teacherInforEntity.getWechat();
            String email = teacherInforEntity.getEmail();
            int qualification = teacherInforEntity.getQualification();
            int experience = teacherInforEntity.getExperience();
            int teacherId = teacherInforEntity.getId();
            int status = teacherInforEntity.getStatus();
            int lesson_id = teacherInforEntity.getLesson_id();
            int grade_id = teacherInforEntity.getGrade_id();
            SharePreferenceUtils.setStringPref(mContext, Constants.TEAHER_NAME, mTeacherName);
            SharePreferenceUtils.setStringPref(mContext, Constants.TEACHER_PHOTO, mTeacherPhoto);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_SEX, sex);
            SharePreferenceUtils.setStringPref(mContext, Constants.TEACHER_BIRTHDAY, birthday);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_FIRST_TEACH, first_year);
            SharePreferenceUtils.setStringPref(mContext, Constants.TEACHER_GRADUATE, graduate);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_EDUCATION, education);
            SharePreferenceUtils.setStringPref(mContext, Constants.TEACHER_WECHAT, wechat);
            SharePreferenceUtils.setStringPref(mContext, Constants.TEACHER_EMAIL, email);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_QUALIFICATION, qualification);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_EXPERIENCEN, experience);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_ID, teacherId);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_STATUS, status);
            SharePreferenceUtils.setIntPref(mContext, Constants.GRADE_ID, grade_id);
            SharePreferenceUtils.setIntPref(mContext, Constants.LESSON_ID, lesson_id);
            refreshView(mIsLogined);
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
                int total = msgBox.getTotal();
                refreshIndicator(total > 0);
                mPri = msgBox.getPri();
                mPub = msgBox.getPub();

                // TODO: 16/10/31 本期作业红点暂时不添加
               /* if (redPointEntity.getData().getPrivateEvents()!=null) {
                    HashMap<String, Integer> events = redPointEntity.getData().getPrivateEvents();
                    if((events.containsKey(Constants.EVENT_MSG_HWREPORT)&&events.get(Constants.EVENT_MSG_HWREPORT)>0))
                    {
                        refreshHomeWorkIndicator(true);
                    }else {
                        refreshHomeWorkIndicator(false);
                    }
                } else {
                    refreshHomeWorkIndicator(false);
                }*/
            }
        }
    }

    private void refreshIndicator(boolean isShowIndicator) {
        if (isShowIndicator) {
            mIndicator.setVisibility(View.VISIBLE);
        } else {
            mIndicator.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshHomeWorkIndicator(boolean isShowIndicator) {
        if (isShowIndicator) {
            mHomeWorkIndicator.setVisibility(View.VISIBLE);
        } else {
            mHomeWorkIndicator.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUESR_TEACHER_PROFILE:
                    mTeacherName = SharePreferenceUtils.getStringPref(mContext, Constants.TEAHER_NAME, "");
                    mTeacherPhoto = SharePreferenceUtils.getStringPref(mContext, Constants.TEACHER_PHOTO, "");
                    refreshView(mIsLogined);
                    break;
                case REQUEST_LOGIN:
                    refreshData();
                    break;
                case REQUEST_SETTING:
                    //修改密码成功后返回
                    loginOut();
//                    startActivity(new Intent(mContext, LoginActivity.class));
                    break;
            }
        } else if (resultCode == Activity.RESULT_FIRST_USER) {
            //点击设置里面的退出登录按钮
            loginOut();
        }
    }

    /**
     * 退出登录
     */
    private void loginOut() {
        mIsLogined = false;
        //友盟 结束账号统计
        MobclickAgent.onProfileSignOff();

        //解绑消息推送的UID
        String uid = SecurePreferences.getInstance(mContext, true).getString(Constants.KEY_USER_ID);
        PushManager pushManager = PushManager.getInstance();
        pushManager.unBindAlias(mContext, uid, true);
        //isSelf：是否只对当前 cid 有效，如果是 true，只对当前cid做解绑；
        // 如果是 false，对所有绑定该别名的cid列表做解绑

        // 清除sharePreference时不清除引导页的标志，防止重复进入引导页
        int versionCode = SharePreferenceUtils.getIntPref(mContext, Constants.KEY_GUID_SHOW, 0);
        String cid = SharePreferenceUtils.getStringPref(mContext, Constants.GT_CLIENT_ID, "");
        SharePreferenceUtils.clearPref(mContext);
        SharePreferenceUtils.setIntPref(mContext, Constants.KEY_GUID_SHOW, versionCode);
        SharePreferenceUtils.setStringPref(mContext, Constants.GT_CLIENT_ID, cid);

        refreshView(mIsLogined);

        Intent intent = new Intent(Constants.LOGOUT_SUCCESS_BROADCAST);
        mContext.sendBroadcast(intent);

        //请出缓存数据
        try {
            Reservoir.clear();
            //TODO 清除之后重新初始化（因为之前偶尔会出现LruDiskCache closed异常，预计是clear方法造成）
            Reservoir.init(mContext, Constants.CACHE_MAX_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        mIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
        LogHelper.d(TAG, "onDestroyViewLazy");
        try {
            mContext.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mTeacherInforSubscriber != null) {
            mTeacherInforSubscriber.onCancle();
            LogHelper.d(TAG, "mTeacherInforSubscriber onCancle");
        }
        if (mTeacherInforSubscription != null && !mTeacherInforSubscription.isUnsubscribed()) {
            mTeacherInforSubscription.unsubscribe();
            LogHelper.d(TAG, "mTeacherInforSubscription unsubscribe");
        }
        if (mRedPointSubscriber != null) {
            mRedPointSubscriber.onCancle();
            LogHelper.d(TAG, "mRedPointSubscriber onCancle");
        }
        if (mRedPointSubscription != null && !mRedPointSubscription.isUnsubscribed()) {
            mRedPointSubscription.unsubscribe();
            LogHelper.d(TAG, "mRedPointSubscriber unsubscribe");
        }
    }
}

