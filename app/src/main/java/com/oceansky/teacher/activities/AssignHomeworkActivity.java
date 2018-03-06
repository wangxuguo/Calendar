package com.oceansky.teacher.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.customviews.CustomDialog;
import com.oceansky.teacher.customviews.CustomProgressDialog;
import com.oceansky.teacher.customviews.pickerview.popwindow.DatePickerPopWin;
import com.oceansky.teacher.event.AssignHomeworkEvent;
import com.oceansky.teacher.event.ModifyHomeworkTitleEvent;
import com.oceansky.teacher.event.RefreshAssignedHomeworkListEvent;
import com.oceansky.teacher.event.RefreshUnassignedHomeworkListEvent;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.HomeworkClassEntity;
import com.oceansky.teacher.network.response.SimpleResponse;
import com.oceansky.teacher.network.subscribers.LoadingSubscriber;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.MyHashSet;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;

public class AssignHomeworkActivity extends BaseActivityWithLoadingState {
    private static final String TAG                    = AssignHomeworkActivity.class.getSimpleName();
    private static final int    SELECTOR_BTN_TEXT_SIZE = 15;
    private static final int    SELECTOR_TEXT_SIZE     = 15;
    public static final  String DATE_FORMAT            = "yyyy.MM.dd HH:mm";

    @Bind(R.id.assign_lv_class)
    ListView mLvClass;

    @Bind(R.id.assign_tv_assigntime)
    TextView mTvAssignTime;

    @Bind(R.id.assign_tv_deadline)
    TextView mTvDeadline;

    @Bind(R.id.assign_tv_answer_publish)
    TextView mTvAnswerPublishTime;

    @Bind(R.id.assign_tv_title)
    TextView mTvHomeworkTitle;

    @Bind(R.id.loading_layout)
    FrameLayout mLoadingLayout;

    @Bind(R.id.loading)
    ImageView mLoadingImg;

    @Bind(R.id.tv_setting)
    TextView mTvComfirm;

    @Bind(R.id.view_divide_line)
    View mDivideLine;

    private AnimationDrawable    mLoadingAnimation;
    private CustomProgressDialog mProgressDialog;

    private String             mHomeworkTitle;
    private String             mCurrentTime;
    private String             mAssignTime;
    private String             mDeadline;
    private MyHashSet<Integer> mTextbookIdSet;
    private MyHashSet<Integer> mChapterIdSet;
    private MyHashSet<Integer> mSectionIdSet;
    private MyHashSet<Integer> mDetailIdSet;
    private MyHashSet<String>  mQuestionIdSet;
    private int                mHomeworkId;
    private int mSelectPosition = -1;
    private int mPublishType    = Constants.PUBLISH_TYPE_AFTER_ASSIGN;//本期默认，不可修改
    private SimpleDateFormat                         mDateFormat;
    private ClassListAdapter                         mClassListAdapter;
    private ArrayList<HomeworkClassEntity.ClassData> mClassList;

    private ClassListSubscriber      mClassListSubscriber;
    private Subscription             mClassListSubscription;
    private AssignHomeworkSubscriber mAssignHomeworkSubscriber;
    private Subscription             mAssignHomeworkSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_homework);
        ButterKnife.bind(this);
        RxBus.getInstance().register(this);
        initView();
        initData();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_homework_assign));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mTitleBar.setSettingButtonText(getString(R.string.btn_confirm_assign));
        mTvComfirm.setEnabled(false);
        initClassListView();
        //加载动画
        mLoadingImg.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mLoadingImg.getDrawable();
        mProgressDialog = CustomProgressDialog.createDialog(this);
    }

    private void initTime(long currentTime) {
        mDateFormat = new SimpleDateFormat(DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        Date assigntime = calendar.getTime();
        mCurrentTime = mDateFormat.format(assigntime);
        calendar.add(Calendar.DATE, 1);
        Date deadline = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");
        mDeadline = df.format(deadline) + " 23:59";
        mAssignTime = mCurrentTime;
        mTvAssignTime.setText(mCurrentTime);
        mTvDeadline.setText(mDeadline);
    }

    private void initData() {
        mHomeworkTitle = getIntent().getStringExtra(Constants.HOMEWORK_TITLE);
        mHomeworkId = getIntent().getIntExtra(Constants.HOMEWORK_ID, -1);
        LogHelper.d(TAG, "homeworkTitle: " + mHomeworkTitle);
        mTvHomeworkTitle.setText(mHomeworkTitle);
        mTextbookIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.TEXTBOOK_ID_SET);
        mChapterIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.CHAPTER_ID_SET);
        mSectionIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.SECTION_ID_SET);
        mDetailIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.DETAIL_ID_SET);
        try {
            if (Reservoir.contains(Constants.QUESTION_ID_SET)) {
                mQuestionIdSet = Reservoir.get(Constants.QUESTION_ID_SET, new TypeToken<MyHashSet<String>>() {
                }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogHelper.d(TAG, "textbookIds: " + mTextbookIdSet.toString());
        LogHelper.d(TAG, "chapterIds: " + mChapterIdSet.toString());
        LogHelper.d(TAG, "sectionIds: " + mSectionIdSet.toString());
        LogHelper.d(TAG, "detailIds: " + mDetailIdSet.toString());
        LogHelper.d(TAG, "questionIds: " + mQuestionIdSet.toString());
        getClassList();
    }

    @Override
    protected void onErrorLayoutClick() {
        super.onErrorLayoutClick();
        mErrorLayout.setVisibility(View.GONE);
        getClassList();
    }

    private void getClassList() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            final String token =
                    "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            mClassListSubscriber = new ClassListSubscriber(this);
            mClassListSubscription = HttpManager.getHomeworkClassList(token, mHomeworkId).subscribe(mClassListSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
        }
    }

    private class ClassListSubscriber extends LoadingSubscriber<HomeworkClassEntity> {

        public ClassListSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, true);
        }

        @Override
        protected void showLoading() {
            mLoadingLayout.setVisibility(View.VISIBLE);
            mLoadingAnimation.start();
        }

        @Override
        protected void dismissLoading() {
            mLoadingLayout.setVisibility(View.INVISIBLE);
            mLoadingAnimation.stop();
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ApiException.TOKEN_INVALID:
                    showTokenInvalidDialog();
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    break;
                default:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
            }
        }

        @Override
        public void onNext(HomeworkClassEntity homeworkClassEntity) {
            super.onNext(homeworkClassEntity);
            long current_time = homeworkClassEntity.getCurrent_time();
            LogHelper.d(TAG, "current_time: " + current_time);
            initTime(current_time * 1000);//current_time单位是秒,要转换为毫秒
            ArrayList<HomeworkClassEntity.ClassData> classList = homeworkClassEntity.getClassList();
            LogHelper.d(TAG, "classList: " + classList);
            if (classList != null && classList.size() > 0) {
                mClassList.clear();
                mClassList.addAll(classList);
                mClassListAdapter.notifyDataSetChanged();
                //scollrview嵌套listview
                View adapterView = mLvClass.getAdapter().getView(0, null, mLvClass);
                adapterView.measure(0, 0);
                int height = adapterView.getMeasuredHeight();
                LogHelper.d(TAG, "item Height: " + height);
                int dividerHeight = mLvClass.getDividerHeight();
                LogHelper.d(TAG, "dividerHeight: " + dividerHeight);
                int listViewHeight = (height + dividerHeight) * mClassListAdapter.getCount() - dividerHeight;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listViewHeight);
                layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.margin_left_class_lv);
                layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.margin_left_class_lv);
                mLvClass.setLayoutParams(layoutParams);
                mLvClass.setFocusable(false);
            } else {
                mDivideLine.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
        }
    }

    private void initClassListView() {
        mClassList = new ArrayList<>();
        mClassListAdapter = new ClassListAdapter(this, mClassList);
        mLvClass.setAdapter(mClassListAdapter);
        mLvClass.setOnItemClickListener((parent, view, position, id) -> {
            MobclickAgent.onEvent(this, Constants.SELECT_CLASS_TOUCHED);
            LogHelper.d(TAG, "onItemClick：" + position);
            mTvComfirm.setEnabled(true);
            //获取选中的参数
            mSelectPosition = position;
            mClassListAdapter.notifyDataSetChanged();
        });
    }

    public class ClassListAdapter extends com.oceansky.teacher.adapter.BaseAdapter<HomeworkClassEntity.ClassData> {

        public ClassListAdapter(Context context, List<HomeworkClassEntity.ClassData> mDatas) {
            super(context, mDatas);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_assign_homework_class, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.tv_class);
                viewHolder.select = (RadioButton) convertView.findViewById(R.id.rb_select);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.name.setText(mDatas.get(position).getClassTitle());
            if (mSelectPosition == position) {
                viewHolder.select.setChecked(true);
            } else {
                viewHolder.select.setChecked(false);
            }
            return convertView;
        }

        class ViewHolder {
            TextView    name;
            RadioButton select;
        }
    }

    @OnClick(R.id.assign_rl_homework_title)
    public void modifyHomeworkTile() {
        Intent intent = new Intent(this, ModifyHomeworkTitleActivity.class);
        intent.putExtra(Constants.HOMEWORK_TITLE, mHomeworkTitle);
        intent.putExtra(Constants.HOMEWORK_ID, mHomeworkId);
        startActivity(intent);
    }

    @OnClick(R.id.assign_rl_assigntime)
    public void selectAssignTime() {
        MobclickAgent.onEvent(this, Constants.SELECT_PUBLISH_HOMEWORK_ANSWER_TIME_TOUCHED);
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        DatePickerPopWin pickerPopWin = new DatePickerPopWin.Builder(this,
                (year, month, day, hour, minute, dateDesc) -> {
                    LogHelper.d(TAG, "dateDesc: " + dateDesc);
                    try {
                        Date assignTime = mDateFormat.parse(dateDesc);
                        Date currentTime = mDateFormat.parse(mCurrentTime);
                        if (assignTime.before(currentTime)) {
                            ToastUtil.showToastBottom(this, R.string.toast_assign_homework_assigntime_invalide, Toast.LENGTH_SHORT);
                        } else {
                            mAssignTime = dateDesc;
                            mTvAssignTime.setText(dateDesc);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                })
                .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                .style(DatePickerPopWin.STYLE_DATE_AND_TIME)
                .minYear(currentYear) //min year in loop
                .maxYear(currentYear + 2) // max year in loop
                .dateChose(mAssignTime) // date chose when init popwindow
                .build();
        pickerPopWin.showPopWin(this);
    }

    @OnClick(R.id.assign_rl_deadline)
    public void selectAssignDeadline() {
        MobclickAgent.onEvent(this, Constants.SELECT_DEADLINE_TIME_TOUCHED);
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        DatePickerPopWin pickerPopWin = new DatePickerPopWin.Builder(this,
                (year, month, day, hour, minute, dateDesc) -> {
                    LogHelper.d(TAG, "dateDesc: " + dateDesc);
                    try {
                        Date deadline = mDateFormat.parse(dateDesc);
                        Date assignTime = mDateFormat.parse(mAssignTime);
                        if (deadline.before(assignTime)) {
                            ToastUtil.showToastBottom(this, R.string.toast_assign_homework_deadline_invalide, Toast.LENGTH_SHORT);
                        } else {
                            mDeadline = dateDesc;
                            mTvDeadline.setText(dateDesc);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                })
                .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                .style(DatePickerPopWin.STYLE_DATE_AND_TIME)
                .minYear(currentYear) //min year in loop
                .maxYear(currentYear + 2) // max year in loop
                .dateChose(mDeadline) // date chose when init popwindow
                .build();
        pickerPopWin.showPopWin(this);
    }

    @OnClick(R.id.tv_setting)
    public void confirm() {
        //校验
        try {
            Date assignTime = mDateFormat.parse(mAssignTime);
            Date deadline = mDateFormat.parse(mDeadline);
            if (deadline.before(assignTime)) {
                ToastUtil.showToastBottom(this, R.string.toast_assign_homework_deadline_invalide, Toast.LENGTH_SHORT);
                return;
            }
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String assignTimeStr = df2.format(assignTime);
            String deadlineStr = df2.format(deadline);
            int classId = mClassList.get(mSelectPosition).getClassId();
            assignHomework(mHomeworkId, classId, assignTimeStr, deadlineStr, mPublishType);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void assignHomework(int homeworkId, int courseId, String publishTime, String deadline, int publishType) {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            final String token =
                    "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            mAssignHomeworkSubscriber = new AssignHomeworkSubscriber(this);
            mAssignHomeworkSubscription = HttpManager.modifyHomework(token, mHomeworkId, mTextbookIdSet.toString(),
                    mChapterIdSet.toString(), mSectionIdSet.toString(), mDetailIdSet.toString(), mQuestionIdSet.toString())
                    .flatMap(new Func1<SimpleResponse, Observable<SimpleResponse>>() {
                        @Override
                        public Observable<SimpleResponse> call(SimpleResponse simpleResponse) {
                            if (simpleResponse.getCode() != 200) {
                                throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
                            }
                            return HttpManager.assignHomework(token, homeworkId, courseId, publishTime, deadline, publishType);
                        }
                    }).subscribe(mAssignHomeworkSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
        }
    }

    private class AssignHomeworkSubscriber extends LoadingSubscriber<SimpleResponse> {

        public AssignHomeworkSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            refreshLoadingState(Constants.LOADING_STATE_TIME_OUT, false);
        }

        @Override
        protected void showLoading() {
            mProgressDialog.show();
        }

        @Override
        protected void dismissLoading() {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ApiException.TOKEN_INVALID:
                    showTokenInvalidDialog();
                    break;
                default:
                    ToastUtil.showToastBottom(AssignHomeworkActivity.this, R.string.toast_error_net_breakdown, Toast.LENGTH_SHORT);
            }
        }

        @Override
        public void onNext(SimpleResponse simpleResponse) {
            int code = simpleResponse.getCode();
            if (code != 200) {
                throw (new ApiException(code + ""));
            }
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            RxBus.getInstance().post(new AssignHomeworkEvent());
            RxBus.getInstance().post(new RefreshUnassignedHomeworkListEvent());
            RxBus.getInstance().post(new RefreshAssignedHomeworkListEvent());
            CustomDialog.Builder ibuilder = new CustomDialog.Builder(AssignHomeworkActivity.this);
            ibuilder.setMessage(R.string.dialog_assign_homework);
            ibuilder.setPositiveButton(R.string.btn_know, (dialog, which) -> {
                dialog.dismiss();
                finish();
            });
            CustomDialog customDialog = ibuilder.create();
            customDialog.setCancelable(false);
            customDialog.show();
        }
    }

    @Subscribe
    public void modifyHomeworkTitle(ModifyHomeworkTitleEvent modifyHomeworkTitleEvent) {
        mHomeworkTitle = modifyHomeworkTitleEvent.getHomeworkTitle();
        mTvHomeworkTitle.setText(mHomeworkTitle);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClassListSubscriber != null) {
            mClassListSubscriber.onCancle();
        }
        if (mClassListSubscription != null && !mClassListSubscription.isUnsubscribed()) {
            mClassListSubscription.unsubscribe();
        }
        if (mAssignHomeworkSubscriber != null) {
            mAssignHomeworkSubscriber.onCancle();
        }
        if (mAssignHomeworkSubscription != null && !mAssignHomeworkSubscription.isUnsubscribed()) {
            mAssignHomeworkSubscription.unsubscribe();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(this);
    }
}
