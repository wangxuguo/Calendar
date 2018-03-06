package com.oceansky.teacher.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.customviews.CustomDialog;
import com.oceansky.teacher.customviews.CustomProgressDialog;
import com.oceansky.teacher.customviews.pickerview.popwindow.SinglePickerPopWin;
import com.oceansky.teacher.event.CreatHomeworkEvent;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.model.CreateHomeworkRequest;
import com.oceansky.teacher.network.response.BaseDataEntity;
import com.oceansky.teacher.network.response.CreateHomeworkEntity;
import com.oceansky.teacher.network.subscribers.LoadingSubscriber;
import com.oceansky.teacher.network.transformer.DefaultSchedulerTransformer;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.SharePreferenceUtils;
import com.oceansky.teacher.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

public class CreatHomeworkActivity extends BaseActivityWithLoadingState {
    private static final String TAG                    = CreatHomeworkActivity.class.getSimpleName();
    private static final int    SELECTOR_BTN_TEXT_SIZE = 15;
    private static final int    SELECTOR_TEXT_SIZE     = 15;

    @Bind(R.id.hw_tv_grade)
    TextView     mTvGrade;
    @Bind(R.id.hw_tv_subjects)
    TextView     mTvLesson;
    @Bind(R.id.hw_et_title)
    EditText     mEtTitle;
    @Bind(R.id.loading)
    ImageView    mIvLoading;
    @Bind(R.id.loading_layout)
    FrameLayout  mLoadingLayout;
    @Bind(R.id.layout_content)
    LinearLayout mllContent;
    @Bind(R.id.tv_setting)
    TextView     mTvSave;

    private CustomProgressDialog     mDialog;
    private AnimationDrawable        mLoadingAnimation;
    private ArrayList<String>        mGradeNameList;
    private ArrayList<Integer>       mGradeIdList;
    private ArrayList<String>        mLessonNameList;
    private ArrayList<Integer>       mLessonIdList;
    private boolean                  isGotoSelectHomework;
    private int                      mHomeworkId;
    private int                      mGradeId;
    private int                      mLessonId;
    private String                   mTitle;
    private CreateHomeworkSubscriber mCreateHomeworkSubscriber;
    private Subscription             mCreateHomeworkSubscription;
    private BaseDataSubscriber       mBaseDataSubscriber;
    private Subscription             mBaseDataSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_homework);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_creat_homework));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mTitleBar.setTvSettingVisibility(true);
        mDialog = CustomProgressDialog.createDialog(this);
        //加载动画
        mIvLoading.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mIvLoading.getDrawable();
    }

    private void initData() {
        try {
            if (Reservoir.contains(Constants.BASE_DATA_GRADES_ID)) {
                mGradeNameList = Reservoir.get(Constants.BASE_DATA_GRADES_NAME, new TypeToken<ArrayList<String>>() {
                }.getType());
                mGradeIdList = Reservoir.get(Constants.BASE_DATA_GRADES_ID, new TypeToken<ArrayList<Integer>>() {
                }.getType());
                mLessonNameList = Reservoir.get(Constants.BASE_DATA_LESSONS_NAME, new TypeToken<ArrayList<String>>() {
                }.getType());
                mLessonIdList = Reservoir.get(Constants.BASE_DATA_LESSONS_ID, new TypeToken<ArrayList<Integer>>() {
                }.getType());
                refreshPickerView();
                LogHelper.d(TAG, "GradeIdList: " + mGradeIdList);
                LogHelper.d(TAG, "GradeNameList: " + mGradeNameList);
                LogHelper.d(TAG, "LessonIdList: " + mLessonIdList);
                LogHelper.d(TAG, "LessonNameList: " + mLessonNameList);
            } else {
                initBaseData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            initBaseData();
        }
    }

    private void initBaseData() {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            mBaseDataSubscriber = new BaseDataSubscriber(this);
            mBaseDataSubscription = HttpManager.getBaseData()
                    .doOnNext(this::cacheBaseDatas)
                    .compose(new DefaultSchedulerTransformer<>())
                    .subscribe(mBaseDataSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
            mTvSave.setEnabled(false);
        }
    }

    private void cacheBaseDatas(BaseDataEntity baseDatas) {
        LogHelper.d(TAG, "cacheBaseData");
        if (baseDatas == null) {
            throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
        }

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
                mLessonNameList = new ArrayList<>(lessons.size());
                mLessonIdList = new ArrayList<>(lessons.size());
                for (BaseDataEntity.Data data : lessons) {
                    mLessonIdList.add(data.getId());
                    mLessonNameList.add(data.getName());
                }
                Reservoir.put(Constants.BASE_DATA_LESSONS_NAME, mLessonNameList);
                Reservoir.put(Constants.BASE_DATA_LESSONS_ID, mLessonIdList);
            }

            List<BaseDataEntity.Data> grades = baseDatas.getGrades();
            if (grades != null) {
                mGradeNameList = new ArrayList<>(grades.size());
                mGradeIdList = new ArrayList<>(grades.size());
                for (BaseDataEntity.Data data : grades) {
                    mGradeIdList.add(data.getId());
                    mGradeNameList.add(data.getName());
                }
                Reservoir.put(Constants.BASE_DATA_GRADES_NAME, mGradeNameList);
                Reservoir.put(Constants.BASE_DATA_GRADES_ID, mGradeIdList);
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

    @Override
    protected void onErrorLayoutClick() {
        super.onErrorLayoutClick();
        mErrorLayout.setVisibility(View.GONE);
        initBaseData();
    }

    @OnClick(R.id.hw_rl_grade)
    public void selectGrade() {
        MobclickAgent.onEvent(this, Constants.SELECT_GRADE_TOUCHED);
        if (mGradeNameList == null) {
            ToastUtil.showToastBottom(this, R.string.toast_grade_get_failed, Toast.LENGTH_SHORT);
            return;
        }
        if (mGradeNameList.size() > 0) {
            SinglePickerPopWin pickerPopWin = new SinglePickerPopWin.Builder(this,
                    mGradeNameList.toArray(new String[mGradeNameList.size()]), (position, selected) -> {
                mTvGrade.setText(selected);
                mGradeId = mGradeIdList.get(position);
            })
                    .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                    .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                    .selected(mGradeIdList.indexOf(mGradeId))
                    .build();
            pickerPopWin.showPopWin(this);
        } else {
            ToastUtil.showToastBottom(this, R.string.toast_grade_data_empty, Toast.LENGTH_SHORT);
        }
    }

    @OnClick(R.id.hw_rl_subjects)
    public void selectSubjects() {
        MobclickAgent.onEvent(this, Constants.SELECT_SUBJECT_TOUCHED);
        if (mLessonNameList == null) {
            ToastUtil.showToastBottom(this, R.string.toast_subject_get_failed, Toast.LENGTH_SHORT);
            return;
        }
        if (mLessonNameList.size() > 0) {
            SinglePickerPopWin pickerPopWin = new SinglePickerPopWin.Builder(this,
                    mLessonNameList.toArray(new String[mLessonNameList.size()]), (position, selected) -> {
                mTvLesson.setText(selected);
                mLessonId = mLessonIdList.get(position);
            })
                    .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                    .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                    .selected(mLessonIdList.indexOf(mLessonId))
                    .build();
            pickerPopWin.showPopWin(this);
        } else {
            ToastUtil.showToastBottom(this, R.string.toast_subject_data_empty, Toast.LENGTH_SHORT);
        }
    }

    @OnClick(R.id.tv_setting)
    public void save() {
        LogHelper.d(TAG, "save");
        isGotoSelectHomework = true;
        saveHomework();
    }

    @Override
    public void onBackStack() {
        finishActivity();
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity() {
        MobclickAgent.onEvent(this, Constants.BACK_CREATE_HOMEWORK_TOUCHED);
        mTitle = mEtTitle.getText().toString().trim();
        if (TextUtils.isEmpty(mTitle)) {
            finish();
        } else {
            showDialog();
        }
    }

    private void showDialog() {
        CustomDialog.Builder ibuilder = new CustomDialog.Builder(this);
        ibuilder.setMessage(R.string.dialog_unsave_homework_back);
        ibuilder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            MobclickAgent.onEvent(CreatHomeworkActivity.this, Constants.SELECT_KNOWLEDGE_POINT_SURE_TOUCHED);
            dialog.dismiss();
            finish();
        });
        ibuilder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
            MobclickAgent.onEvent(CreatHomeworkActivity.this, Constants.SELECT_KNOWLEDGE_POINT_CANCEL_TOUCHED);
            dialog.dismiss();
        });
        ibuilder.create().show();
    }

    private void saveHomework() {
        mTitle = mEtTitle.getText().toString().trim();
        if (TextUtils.isEmpty(mTitle)) {
            ToastUtil.showToastBottom(this, R.string.toast_homework_title_empty, Toast.LENGTH_SHORT);
            return;
        } else if (mGradeId < 1) {
            ToastUtil.showToastBottom(this, R.string.toast_homework_grade_empty, Toast.LENGTH_SHORT);
            return;
        } else if (mLessonId < 1) {
            ToastUtil.showToastBottom(this, R.string.toast_homework_lesson_empty, Toast.LENGTH_SHORT);
            return;
        }
        createHomework(mTitle, mGradeId, mLessonId);
    }

    private void createHomework(String title, int grade_id, int lesson_id) {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            final String token =
                    "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            mCreateHomeworkSubscriber = new CreateHomeworkSubscriber(this);
            mCreateHomeworkSubscription = HttpManager.createHomework(token, new CreateHomeworkRequest(title, grade_id, lesson_id))
                    .subscribe(mCreateHomeworkSubscriber);
        } else {
            ToastUtil.showToastBottom(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT);
        }
    }

    private class CreateHomeworkSubscriber extends LoadingSubscriber<CreateHomeworkEntity> {

        public CreateHomeworkSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            ToastUtil.showToastBottom(CreatHomeworkActivity.this, R.string.toast_error_time_out, Toast.LENGTH_SHORT);
        }

        @Override
        protected void showLoading() {
            mDialog.show();
        }

        @Override
        protected void dismissLoading() {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ApiException.TOKEN_INVALID:
                    showTokenInvalidDialog();
                    break;
                default:
                    ToastUtil.showToastBottom(CreatHomeworkActivity.this, R.string.toast_error_net_breakdown, Toast.LENGTH_SHORT);
            }
        }

        @Override
        public void onNext(CreateHomeworkEntity createHomeworkEntity) {
            mHomeworkId = createHomeworkEntity.getId();
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            if (isGotoSelectHomework) {
                Intent intent = new Intent(CreatHomeworkActivity.this, KnowledgePointSelectActivity.class);
                intent.putExtra(Constants.GRADE_ID, mGradeId);
                intent.putExtra(Constants.LESSON_ID, mLessonId);
                intent.putExtra(Constants.HOMEWORK_ID, mHomeworkId);
                intent.putExtra(Constants.HOMEWORK_TITLE, mTitle);
                startActivity(intent);
            }
            RxBus.getInstance().post(new CreatHomeworkEvent(mTitle));
            finish();
        }
    }

    class BaseDataSubscriber extends LoadingSubscriber<BaseDataEntity> {
        public BaseDataSubscriber(Context context) {
            super(context);
        }

        @Override
        public void onStart() {
            super.onStart();
            mTvSave.setEnabled(false);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
            mTvSave.setEnabled(false);
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case ApiException.TOKEN_INVALID:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    showTokenInvalidDialog();
                    break;
                default:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
            }
            mTvSave.setEnabled(false);
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            refreshPickerView();
            mTvSave.setEnabled(true);
        }

        @Override
        public void onNext(BaseDataEntity baseDatas) {

        }

        @Override
        protected void showLoading() {
            mLoadingLayout.setVisibility(View.VISIBLE);
            mLoadingAnimation.start();
        }

        @Override
        protected void dismissLoading() {
            mLoadingLayout.setVisibility(View.GONE);
            mLoadingAnimation.stop();
        }
    }

    private void refreshPickerView() {
        int defGradeId = SharePreferenceUtils.getIntPref(this, Constants.GRADE_ID, -1);
        int defLessonId = SharePreferenceUtils.getIntPref(this, Constants.LESSON_ID, -1);
        if (mGradeIdList != null && mGradeIdList.size() > 0 && mLessonIdList != null && mLessonIdList.size() > 0) {
            mGradeId = defGradeId < 0 ? mGradeIdList.get(0) : defGradeId;
            mLessonId = defLessonId < 0 ? mLessonIdList.get(0) : defLessonId;
            if (mGradeIdList.contains(mGradeId)) {
                mTvGrade.setText(mGradeNameList.get(mGradeIdList.indexOf(mGradeId)));
            }
            if (mLessonIdList.contains(mLessonId)) {
                mTvLesson.setText(mLessonNameList.get(mLessonIdList.indexOf(mLessonId)));
            }
            mllContent.setVisibility(View.VISIBLE);
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
        if (mCreateHomeworkSubscriber != null) {
            mCreateHomeworkSubscriber.onCancle();
        }
        if (mCreateHomeworkSubscription != null && !mCreateHomeworkSubscription.isUnsubscribed()) {
            mCreateHomeworkSubscription.unsubscribe();
        }
    }
}
