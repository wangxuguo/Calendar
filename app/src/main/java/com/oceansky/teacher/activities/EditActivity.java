package com.oceansky.teacher.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.constant.FeatureConfig;
import com.oceansky.teacher.customviews.CustomProgressDialog;
import com.oceansky.teacher.customviews.PhotoDialog;
import com.oceansky.teacher.customviews.pickerview.popwindow.DatePickerPopWin;
import com.oceansky.teacher.customviews.pickerview.popwindow.SinglePickerPopWin;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.BaseDataEntity;
import com.oceansky.teacher.network.response.ModifyInforEntity;
import com.oceansky.teacher.network.response.UploadTokenEntity;
import com.oceansky.teacher.network.subscribers.BaseSubscriber;
import com.oceansky.teacher.network.subscribers.LoadingSubscriber;
import com.oceansky.teacher.network.transformer.DefaultSchedulerTransformer;
import com.oceansky.teacher.utils.ImageUtils;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.MD5;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.RegexUtil;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.SharePreferenceUtils;
import com.oceansky.teacher.utils.ToastUtil;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscription;

public class EditActivity extends BaseActivityWithLoadingState {

    private static final String TAG                     = EditActivity.class.getSimpleName();
    public static final  int    SELECTOR_TEXT_SIZE      = 15;
    private static final int    SELECTOR_BTN_TEXT_SIZE  = 15;
    public static final  int    MIN_FIRST_TEACH_YEAR    = 1950;
    public static final  int    MIN_BIRTH_YEAR          = 1920;
    public static final  String BIRTHDAY_DEFAULT        = "1980-01-01";
    public static final  int    FIRST_TEACH_DEFAULT     = 2006;
    public static final  int    EDUCATUIN_DEFAULT       = 3;
    public static final  int    SEX_ID_DEFAULT          = 0;
    public static final  int    QUALIFICAION_ID_DEFAULT = 1;
    public static final  int    EXP_DEFAULT             = 1;
    // photo selecte
    private final        int    REQUEST_CODE_CAMERA     = 1000;
    private final        int    REQUEST_CODE_GALLERY    = 1001;

    @Bind(R.id.edit_iv_photo)
    CircleImageView mIvPhoto;

    @Bind(R.id.edit_tv_sex)
    TextView mTvSex;

    @Bind(R.id.edit_tv_birthday)
    TextView mTvBirthday;

    @Bind(R.id.edit_tv_first_time)
    TextView mTvFirstTime;

    @Bind(R.id.edit_tv_qualification)
    TextView mTvQualification;

    @Bind(R.id.edit_tv_experience)
    TextView mTvExperience;

    @Bind(R.id.edit_tv_education)
    TextView mTVEducation;

    @Bind(R.id.edit_et_name)
    EditText mEtName;

    @Bind(R.id.edit_et_school)
    EditText mEtSchool;

    @Bind(R.id.edit_et_wechat)
    EditText mEtWechat;

    @Bind(R.id.edit_et_email)
    EditText mEtEmail;

    @Bind(R.id.edit_layout)
    LinearLayout mEditLayout;

    @Bind(R.id.loading_layout)
    FrameLayout mLoadingLayout;

    @Bind(R.id.loading)
    ImageView mIvLoading;

    private String            mPhotoPath;
    private int               mSexId;
    private String            mBirthday;
    private int               mQualificationId;
    private int               mExperienceId;
    private String            mOriginPhotoPath;
    private String            mTeacherName;
    private String            mTeacherGraduate;
    private int               mFirstTeach;
    private String            mWeChat;
    private String            mEmail;
    private int               mTeacherId;
    private UploadTokenEntity mUploadData;
    private String            mQiNiuKey;
    private String            mQiNiuToken;
    private EditActivity      mContext;
    private AnimationDrawable mLoadingAnimation;
    private int               mLoadState;
    private int               mEducationId;

    private String[]     mSexArray           = new String[]{"女", "男"};
    private List<String> yearList            = new ArrayList();
    private String[]     mQualificationArray = new String[]{"无", "有"};
    private String[]     mExperienceArray    = new String[]{"无", "有"};
    private ModifyInforSubscriber mModifyInforSubscriber;
    private BaseDataSubscriber    mBaseDataSubscriber;
    private Subscription          mBaseDataSubscription;
    private Subscription          mModifyInforSubscription;
    private UploadTokenSubscriber mUploadTokenSubscriber;
    private Subscription          mUploadTokenSubscription;
    private CustomProgressDialog  mDialog;
    private ArrayList<Integer>    mEducationIdList;
    private ArrayList<String>     mEducationNameList;
    private int                   mEducationSelectedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_edit);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mContext = EditActivity.this;
        //加载动画
        mIvLoading.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mIvLoading.getDrawable();
        mTitleBar.setTitle(getString(R.string.title_edit_enable));
        try {
            if (Reservoir.contains(Constants.BASE_DATA_EDUCATIONS_NAME)
                    && Reservoir.contains(Constants.BASE_DATA_EDUCATIONS_ID)) {
                mEducationNameList = Reservoir.get(Constants.BASE_DATA_EDUCATIONS_NAME, new TypeToken<ArrayList<String>>() {
                }.getType());
                mEducationIdList = Reservoir.get(Constants.BASE_DATA_EDUCATIONS_ID, new TypeToken<ArrayList<Integer>>() {
                }.getType());
                LogHelper.d(TAG, "EducationIdList: " + mEducationIdList);
                LogHelper.d(TAG, "EducationNameList: " + mEducationNameList);
                initView();
                mEditLayout.setVisibility(View.VISIBLE);
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
        }
    }

    private void cacheBaseDatas(BaseDataEntity baseDatas) {
        LogHelper.d(TAG, "cacheBaseDatas");
        List<BaseDataEntity.Data> educations = baseDatas.getEducations();
        if (educations != null) {
            mEducationIdList = new ArrayList<>(educations.size());
            mEducationNameList = new ArrayList<>(educations.size());
            for (BaseDataEntity.Data data : educations) {
                mEducationIdList.add(data.getId());
                mEducationNameList.add(data.getName());
            }
            try {
                Reservoir.put(Constants.BASE_DATA_EDUCATIONS_ID, mEducationIdList);
                Reservoir.put(Constants.BASE_DATA_EDUCATIONS_NAME, mEducationNameList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
        }
    }

    @Override
    protected void onErrorLayoutClick() {
        mErrorLayout.setVisibility(View.GONE);
        initBaseData();
    }

    private void initView() {
        mPhotoPath = SharePreferenceUtils.getStringPref(this, Constants.TEACHER_PHOTO, null);
        ImageUtils.loadImage(mPhotoPath, mIvPhoto, R.mipmap.profile_photo_default);
        mOriginPhotoPath = mPhotoPath;
        mTeacherName = SharePreferenceUtils.getStringPref(this, Constants.TEAHER_NAME, null);
        mSexId = SharePreferenceUtils.getIntPref(this, Constants.TEACHER_SEX, SEX_ID_DEFAULT);
        mBirthday = SharePreferenceUtils.getStringPref(this, Constants.TEACHER_BIRTHDAY, BIRTHDAY_DEFAULT);
        mTeacherGraduate = SharePreferenceUtils.getStringPref(this, Constants.TEACHER_GRADUATE, null);
        mFirstTeach = SharePreferenceUtils.getIntPref(this, Constants.TEACHER_FIRST_TEACH, FIRST_TEACH_DEFAULT);
        mEducationId = SharePreferenceUtils.getIntPref(this, Constants.TEACHER_EDUCATION, EDUCATUIN_DEFAULT);
        mWeChat = SharePreferenceUtils.getStringPref(this, Constants.TEACHER_WECHAT, null);
        mEmail = SharePreferenceUtils.getStringPref(this, Constants.TEACHER_EMAIL, null);
        mQualificationId = SharePreferenceUtils.getIntPref(this, Constants.TEACHER_QUALIFICATION, QUALIFICAION_ID_DEFAULT);
        mExperienceId = SharePreferenceUtils.getIntPref(this, Constants.TEACHER_EXPERIENCEN, EXP_DEFAULT);
        mEtName.setText(mTeacherName);
        mTvSex.setText(mSexArray[mSexId]);
        mTvBirthday.setText(mBirthday);
        mEtSchool.setText(mTeacherGraduate);
        mTvFirstTime.setText(mFirstTeach + "年");
        mEducationSelectedPosition = mEducationIdList.indexOf(mEducationId);
        mTVEducation.setText(mEducationNameList.get(mEducationSelectedPosition));
        mEtWechat.setText(mWeChat);
        mEtEmail.setText(mEmail);
        mTvQualification.setText(mQualificationArray[mQualificationId]);
        mTvExperience.setText(mExperienceArray[mExperienceId]);
        mDialog = CustomProgressDialog.createDialog(this);
        mTitleBar.setTvSettingVisibility(true);
    }

    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback
            = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            LogHelper.d(TAG, "reqeustCode:" + reqeustCode);
            if (resultList != null && resultList.size() > 0) {
                mPhotoPath = resultList.get(0).getPhotoPath();
                LogHelper.d(TAG, "PohoPath:" + mPhotoPath);
                ImageLoader.getInstance().displayImage("file://" + mPhotoPath, mIvPhoto);
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
            ToastUtil.showToastBottom(EditActivity.this, errorMsg, Toast.LENGTH_SHORT);
        }
    };

    @OnClick(R.id.edit_rl_photo)
    public void modifyPhoto() {
        new PhotoDialog(this, new PhotoDialog.OnCustomDialogListener() {
            @Override
            public void back(int item) {
                switch (item) {
                    case R.id.dialog_select_photo:
                        GalleryFinal.openGallerySingle(REQUEST_CODE_GALLERY, mOnHanlderResultCallback);
                        break;

                    case R.id.dialog_take_photo:
                        GalleryFinal.openCamera(REQUEST_CODE_CAMERA, mOnHanlderResultCallback);
                        break;
                }
            }
        }).show();
    }

    @OnClick(R.id.edit_rl_name)
    public void modifyName() {
        mEtName.requestFocus();
        mEtName.setSelection(mEtName.getText().length());
        mImm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @OnClick(R.id.edit_rl_school)
    public void modifySchool() {
        mEtSchool.requestFocus();
        mEtSchool.setSelection(mEtSchool.getText().length());
        mImm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @OnClick(R.id.edit_rl_wechat)
    public void modifyWechat() {
        mEtWechat.requestFocus();
        mEtWechat.setSelection(mEtWechat.getText().length());
        mImm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @OnClick(R.id.edit_rl_email)
    public void modifyEmail() {
        mEtEmail.requestFocus();
        mEtEmail.setSelection(mEtEmail.getText().length());
        mImm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @OnClick(R.id.edit_rl_sex)
    public void modifySex() {
        SinglePickerPopWin pickerPopWin = new SinglePickerPopWin.Builder(this,
                mSexArray, new SinglePickerPopWin.OnSinglePickedListener() {
            @Override
            public void onSinglePickCompleted(int position, String selected) {
                mTvSex.setText(selected);
                mSexId = position;
            }
        })
                .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                .selected(mSexId)
                .build();
        pickerPopWin.showPopWin(this);
    }

    @OnClick(R.id.edit_rl_birthday)
    public void modifyBirthday() {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        DatePickerPopWin pickerPopWin = new DatePickerPopWin.Builder(this,
                new DatePickerPopWin.OnDatePickedListener() {
                    @Override
                    public void onDatePickCompleted(int year, int month, int day, int hour, int minute, String dateDesc) {
                        LogHelper.d(TAG, "dateDesc: " + dateDesc);
                        mBirthday = dateDesc;
                        mTvBirthday.setText(dateDesc);
                        //mTvBirthday.setText(year+"年"+month+"月"+day+"日");
                        //mTvDone.setVisibility(View.VISIBLE);
                    }
                })
                .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                .minYear(MIN_BIRTH_YEAR) //min year in loop
                .maxYear(currentYear + 1) // max year in loop
                .style(DatePickerPopWin.STYLE_DATE)
                .dateChose(mBirthday) // date chose when init popwindow
                .build();
        pickerPopWin.showPopWin(this);
    }

    @OnClick(R.id.edit_rl_first_time)
    public void setFirstTime() {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        String[] yearArr = initPickerViews(MIN_FIRST_TEACH_YEAR, currentYear + 1);
        SinglePickerPopWin pickerPopWin = new SinglePickerPopWin.Builder(this,
                yearArr, new SinglePickerPopWin.OnSinglePickedListener() {
            @Override
            public void onSinglePickCompleted(int position, String selected) {
                mTvFirstTime.setText(selected);
                mFirstTeach = position + MIN_FIRST_TEACH_YEAR;
            }
        })
                .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                .selected(mFirstTeach - MIN_FIRST_TEACH_YEAR)
                .build();
        pickerPopWin.showPopWin(this);
    }

    @OnClick(R.id.edit_rl_education)
    public void modifyEducation() {
        if (mEducationNameList == null || mEducationNameList.size() < 1) {
            ToastUtil.showToastBottom(this, R.string.toast_education_get_failed, Toast.LENGTH_SHORT);
            return;
        }
        SinglePickerPopWin pickerPopWin = new SinglePickerPopWin.Builder(this,
                mEducationNameList.toArray(new String[mEducationNameList.size()]), (position, selected) -> {
            mTVEducation.setText(selected);
            mEducationSelectedPosition = position;
            mEducationId = mEducationIdList.get(position);
        })
                .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                .selected(mEducationSelectedPosition)
                .build();
        pickerPopWin.showPopWin(this);
    }

    @OnClick(R.id.edit_rl_qualification)
    public void setQualification() {
        SinglePickerPopWin pickerPopWin = new SinglePickerPopWin.Builder(this,
                mQualificationArray, new SinglePickerPopWin.OnSinglePickedListener() {
            @Override
            public void onSinglePickCompleted(int position, String selected) {
                mTvQualification.setText(selected);
                mQualificationId = position;
            }
        })
                .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                .selected(mQualificationId)
                .build();
        pickerPopWin.showPopWin(this);
    }

    @OnClick(R.id.edit_rl_experience)
    public void setExperience() {
        SinglePickerPopWin pickerPopWin = new SinglePickerPopWin.Builder(this,
                mExperienceArray, new SinglePickerPopWin.OnSinglePickedListener() {
            @Override
            public void onSinglePickCompleted(int position, String selected) {
                mTvExperience.setText(selected);
                mExperienceId = position;
            }
        })
                .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                .selected(mExperienceId)
                .build();
        pickerPopWin.showPopWin(this);
    }

    @OnClick(R.id.tv_setting)
    public void save() {
        //表单校验
        mTeacherName = mEtName.getText().toString().trim();
        mTeacherGraduate = mEtSchool.getText().toString().trim();
        mWeChat = mEtWechat.getText().toString().trim();
        mEmail = mEtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(mTeacherName)) {
            ToastUtil.showToastBottom(this, R.string.toast_check_name_empty, Toast.LENGTH_SHORT);
            return;
        }
        if (!RegexUtil.checkName(mTeacherName)) {
            ToastUtil.showToastBottom(this, R.string.toast_check_name_invalied, Toast.LENGTH_SHORT);
            return;
        }

        if (TextUtils.isEmpty(mTeacherGraduate)) {
            ToastUtil.showToastBottom(this, R.string.toast_check_school_empty, Toast.LENGTH_SHORT);
            return;
        }
        if (!RegexUtil.checkSchoolName(mTeacherGraduate)) {
            ToastUtil.showToastBottom(this, R.string.toast_check_school_invalied, Toast.LENGTH_SHORT);
            return;
        }

        if (!TextUtils.isEmpty(mEmail) && !RegexUtil.checkEmail(mEmail)) {
            ToastUtil.showToastBottom(this, R.string.toast_check_email_invalied, Toast.LENGTH_SHORT);
            return;
        }
        if (NetworkUtils.isNetworkAvaialble(this)) {
            showProgress();
            if (!mOriginPhotoPath.equals(mPhotoPath) && mOriginPhotoPath != null) {
                getUploadToken();
            } else {
                //不上传七牛
                postModifyedData();
            }
        } else {
            ToastUtil.showToastBottom(this, R.string.toast_error_no_net, Toast.LENGTH_SHORT);
        }
    }

    /**
     * 获取七牛的uploadToken
     *
     * @return
     */
    private void getUploadToken() {
        String fileMD5 = MD5.getFileMD5(new File(mPhotoPath));
        LogHelper.d("child", "postModifyedData" + "\n" + "fileMD5: " + fileMD5);
        //发送GET请求，获取七牛的uploadToken
        mUploadTokenSubscriber = new UploadTokenSubscriber(mContext);
        mUploadTokenSubscription = HttpManager.getUploadToken(FeatureConfig.BUCKET, fileMD5)
                .subscribe(mUploadTokenSubscriber);
    }

    /**
     * 上传修改后的资料到服务器
     */
    private void postModifyedData() {
        MobclickAgent.onEvent(this, "mine_child_infosave_touched");
        String avatar = SharePreferenceUtils.getStringPref(this, Constants.QI_NIU_KEY, "");
        if ("".equals(avatar)) {
            String[] strings = mPhotoPath.split("/");
            avatar = strings[strings.length - 1];
        }
        mTeacherId = SharePreferenceUtils.getIntPref(this, Constants.TEACHER_ID, 0);
        //发送POST请求，修改孩子信息
        final String token = "Bearer " + SecurePreferences.getInstance(EditActivity.this, false)
                .getString(Constants.KEY_ACCESS_TOKEN);
        mModifyInforSubscriber = new ModifyInforSubscriber(mContext);
        mModifyInforSubscription = HttpManager.modifyInfor(token, mTeacherName, mSexId, mBirthday,
                mFirstTeach, mTeacherGraduate, mEducationId, mWeChat, mEmail, mQualificationId,
                mExperienceId, avatar).subscribe(mModifyInforSubscriber);
    }

    private String[] initPickerViews(int minYear, int maxYear) {
        int yearCount = maxYear - minYear;
        for (int i = 0; i < yearCount; i++) {
            yearList.add(minYear + i + "年");
        }
        String[] yearArr = (String[]) yearList.toArray(new String[yearList.size()]);
        return yearArr;
    }

    class UploadTokenSubscriber extends BaseSubscriber<UploadTokenEntity> {
        public UploadTokenSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            hideProgress();
            Toast.makeText(EditActivity.this, R.string.toast_error_time_out, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void handleError(Throwable e) {
            hideProgress();
            switch (e.getMessage()) {
                case "4001":
                    showTokenInvalidDialog();
                    break;
                default:
                    Toast.makeText(EditActivity.this, R.string.toast_error_net_breakdown, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onNext(UploadTokenEntity uploadTokenEntity) {
            LogHelper.d(TAG, "uploadTokenEntity" + uploadTokenEntity);
            //获取成功
            mUploadData = uploadTokenEntity;
            //上传图片到七牛
            UploadManager uploadManager = new UploadManager();
            //<指定七牛服务上的文件名，或 null>
            mQiNiuKey = mUploadData.getFile_name();
            //<从服务端SDK获取>
            mQiNiuToken = mUploadData.getUpload_token();
            SharePreferenceUtils.setStringPref(EditActivity.this, Constants.QI_NIU_KEY, mQiNiuKey);
            SharePreferenceUtils.setStringPref(EditActivity.this, Constants.QI_NIU_TOKEN, mQiNiuToken);
            uploadManager.put(mPhotoPath, mQiNiuKey, mQiNiuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            // res 包含hash、key等信息，具体字段取决于上传策略的设置。
                            LogHelper.d("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            postModifyedData();
                        }
                    }, null);
        }
    }

    class ModifyInforSubscriber extends BaseSubscriber<ModifyInforEntity> {
        public ModifyInforSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            hideProgress();
            Toast.makeText(EditActivity.this, R.string.toast_error_time_out, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void handleError(Throwable e) {
            hideProgress();
            switch (e.getMessage()) {
                case "4013":
                    showTokenInvalidDialog();
                    break;
                case "4000":
                    Toast.makeText(mContext, R.string.toast_error_input_error, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(mContext, R.string.toast_error_net_breakdown, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onNext(ModifyInforEntity modifyInforEntity) {
            Toast.makeText(EditActivity.this, R.string.toast_edit_success, Toast.LENGTH_SHORT).show();
            if (modifyInforEntity != null) {
                mPhotoPath = modifyInforEntity.getAvatar();
            }
            SharePreferenceUtils.setStringPref(mContext, Constants.TEAHER_NAME, mTeacherName);
            SharePreferenceUtils.setStringPref(mContext, Constants.TEACHER_PHOTO, mPhotoPath);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_SEX, mSexId);
            SharePreferenceUtils.setStringPref(mContext, Constants.TEACHER_BIRTHDAY, mBirthday);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_FIRST_TEACH, mFirstTeach);
            SharePreferenceUtils.setStringPref(mContext, Constants.TEACHER_GRADUATE, mTeacherGraduate);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_EDUCATION, mEducationId);
            SharePreferenceUtils.setStringPref(mContext, Constants.TEACHER_WECHAT, mWeChat);
            SharePreferenceUtils.setStringPref(mContext, Constants.TEACHER_EMAIL, mEmail);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_QUALIFICATION, mQualificationId);
            SharePreferenceUtils.setIntPref(mContext, Constants.TEACHER_EXPERIENCEN, mExperienceId);
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            hideProgress();
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    class BaseDataSubscriber extends LoadingSubscriber<BaseDataEntity> {
        public BaseDataSubscriber(Context context) {
            super(context);
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case "4013":
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    showTokenInvalidDialog();
                    break;
                default:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
            }
        }

        @Override
        public void onNext(BaseDataEntity baseDatas) {
            if (baseDatas == null) {
                throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
            }
            List<BaseDataEntity.Data> educationList = baseDatas.getEducations();
            LogHelper.d(TAG, "educationList: " + educationList);
            if (educationList != null) {
                mLoadState = Constants.LOADING_STATE_SUCCESS;
                mEducationNameList = new ArrayList<>(educationList.size());
                mEducationIdList = new ArrayList<>(educationList.size());
                for (BaseDataEntity.Data data : educationList) {
                    mEducationIdList.add(data.getId());
                    mEducationNameList.add(data.getName());
                }
                initView();
            } else {
                mLoadState = Constants.LOADING_STATE_FAIL;
            }
            mEditLayout.setVisibility(View.VISIBLE);
            refreshLoadingState(mLoadState, true);
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

    @Override
    protected void onStop() {
        super.onStop();
        if (mBaseDataSubscriber != null) {
            mBaseDataSubscriber.onCancle();
        }
        if (mBaseDataSubscription != null && !mBaseDataSubscription.isUnsubscribed()) {
            mBaseDataSubscription.unsubscribe();
        }
        if (mUploadTokenSubscriber != null) {
            mUploadTokenSubscriber.onCancle();
        }
        if (mUploadTokenSubscription != null && !mUploadTokenSubscription.isUnsubscribed()) {
            mUploadTokenSubscription.unsubscribe();
        }
        if (mModifyInforSubscriber != null) {
            mModifyInforSubscriber.onCancle();
        }
        if (mModifyInforSubscription != null && !mModifyInforSubscription.isUnsubscribed()) {
            mModifyInforSubscription.unsubscribe();
        }
    }

    private void showProgress() {
        if (!mDialog.isShowing())
            mDialog.show();
    }

    private void hideProgress() {
        if (mDialog.isShowing())
            mDialog.dismiss();
    }
}
