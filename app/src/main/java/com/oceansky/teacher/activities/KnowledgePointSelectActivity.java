package com.oceansky.teacher.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andexert.expandablelayout.library.ExpandableLayoutItem;
import com.andexert.expandablelayout.library.ExpandableLayoutListView;
import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.oceansky.teacher.R;
import com.oceansky.teacher.adapter.BaseAdapter;
import com.oceansky.teacher.adapter.KnowledgeSectionAdapter;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.customviews.CustomDialog;
import com.oceansky.teacher.customviews.CustomProgressDialog;
import com.oceansky.teacher.customviews.pickerview.popwindow.SinglePickerPopWin;
import com.oceansky.teacher.event.AssignHomeworkEvent;
import com.oceansky.teacher.event.ChangeknowledgePointEvent;
import com.oceansky.teacher.event.ModifyHomeworkEvent;
import com.oceansky.teacher.event.RefreshUnassignedHomeworkListEvent;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.network.http.ApiException;
import com.oceansky.teacher.network.http.HttpManager;
import com.oceansky.teacher.network.response.BaseDataEntity;
import com.oceansky.teacher.network.response.KnowledgePointEntity;
import com.oceansky.teacher.network.response.SimpleResponse;
import com.oceansky.teacher.network.subscribers.LoadingSubscriber;
import com.oceansky.teacher.network.transformer.DefaultSchedulerTransformer;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.MyHashSet;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static com.anupcowkur.reservoir.Reservoir.get;

public class KnowledgePointSelectActivity extends BaseActivityWithLoadingState {

    private static final String TAG                    = KnowledgePointSelectActivity.class.getSimpleName();
    private static final int    SELECTOR_BTN_TEXT_SIZE = 15;
    private static final int    SELECTOR_TEXT_SIZE     = 15;

    @Bind(R.id.listview)
    ExpandableLayoutListView mExpandableLayoutListView;

    @Bind(R.id.loading_layout)
    FrameLayout mLoadingLayout;

    @Bind(R.id.loading)
    ImageView mLoadingImg;

    @Bind(R.id.hw_tv_textbook)
    TextView mTvTextbook;

    @Bind(R.id.hw_tv_chapter)
    TextView mTvChapter;

    @Bind(R.id.hw_rl_chapter)
    RelativeLayout mRlChapter;

    private CustomProgressDialog mProgressDialog;
    private AnimationDrawable    mLoadingAnimation;

    private int    mTextBookPickerPosition;
    private int    mChapterPickerPosition;
    private int    mHomeworkId;
    private int    mLessonId;
    private int    mGradeId;
    private int    mChapterId;
    private String mHomeworkTitle;
    private int      mTextBookId   = 1;//默认为人教版
    private String[] mChapterArray = {};

    private ArrayList<KnowledgePointEntity.ChapterData> mKnowledgeTree = new ArrayList<>();
    private ArrayList<KnowledgePointEntity.SectionData> mKnowledgeSections;
    private ChapterListAdapter                          mChapterListAdapter;
    private ArrayList<String>                           mTextBookNameList;
    private ArrayList<Integer>                          mTextBookIdList;
    private MyHashSet<String>                           mQuestionIdSet;
    private MyHashSet<Integer>                          mTextbookIdSet;
    private MyHashSet<Integer>                          mChapterIdSet;
    private MyHashSet<Integer>                          mSectionIdSet;
    private MyHashSet<Integer>                          mDetailIdSet;
    private LinkedHashMap<String, String>               mHtmlMap;

    private boolean[] mExpandStates;//保存列表Item的展开状态
    private boolean   mIsLoadingBaseDatasFail;
    private boolean   mIsChangeKnowledgePoint;
    private boolean   mIsAddhomework;

    private KnowledgeTreeSubscriber  mKnowledgeTreeSubscriber;
    private Subscription             mKnowledgeTreeSubscription;
    private BaseDataSubscriber       mBaseDataSubscriber;
    private Subscription             mBaseDataSubscription;
    private ModifyHomeworkSubscriber mModifyHomeworkSubscriber;
    private Subscription             mModifyHomeworkSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_knowlege_point_select);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        RxBus.getInstance().register(this);
        initView();
        initBaseData();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_knowledge_point_select));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mKnowledgeSections = new ArrayList<>();
        mChapterListAdapter = new ChapterListAdapter(this, mKnowledgeSections);
        mExpandableLayoutListView.setAdapter(mChapterListAdapter);
        //加载动画
        mLoadingImg.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mLoadingImg.getDrawable();
        mProgressDialog = CustomProgressDialog.createDialog(this);
    }

    private void initData() {
        try {
            if (Reservoir.contains(Constants.BASE_DATA_TEXTBOOK_ID)) {
                mTextBookNameList = Reservoir.get(Constants.BASE_DATA_TEXTBOOK_NAME, new TypeToken<ArrayList<String>>() {
                }.getType());
                mTextBookIdList = Reservoir.get(Constants.BASE_DATA_TEXTBOOK_ID, new TypeToken<ArrayList<Integer>>() {
                }.getType());
                LogHelper.d(TAG, "TextBookIdList: " + mTextBookIdList);
                LogHelper.d(TAG, "TextBookNameList: " + mTextBookNameList);
                if (mTextBookIdList != null && mTextBookIdList.size() > 0
                        && mTextBookNameList != null && mTextBookNameList.size() > 0) {
                    mTextBookPickerPosition = mTextBookIdList.indexOf(mTextBookId);
                    if (mTextBookPickerPosition != -1) {
                        mTvTextbook.setText(mTextBookNameList.get(mTextBookPickerPosition));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mIsChangeKnowledgePoint = getIntent().getBooleanExtra(Constants.IS_CHANGE_KNOWLEDGE_POINT, false);
        mIsAddhomework = getIntent().getBooleanExtra(Constants.IS_ADD_HOMEWORK, false);
        LogHelper.d(TAG, "IsChangeKnowledgePoint: " + mIsChangeKnowledgePoint);
        LogHelper.d(TAG, "IsAddhomework: " + mIsAddhomework);

        //这4个数据是跳转到此页面必传的
        mLessonId = getIntent().getIntExtra(Constants.LESSON_ID, -1);
        mGradeId = getIntent().getIntExtra(Constants.GRADE_ID, -1);
        mHomeworkId = getIntent().getIntExtra(Constants.HOMEWORK_ID, -1);
        mHomeworkTitle = getIntent().getStringExtra(Constants.HOMEWORK_TITLE);
        LogHelper.d(TAG, "LessonId: " + mLessonId);
        LogHelper.d(TAG, "GradeId: " + mGradeId);
        LogHelper.d(TAG, "HomeworkId: " + mHomeworkId);
        LogHelper.d(TAG, "HomeworkTitle: " + mHomeworkTitle);

        if (mIsAddhomework) {
            try {
                if (Reservoir.contains(Constants.HOMEWORK_HTML_MAP)) {
                    mHtmlMap = Reservoir.get(Constants.HOMEWORK_HTML_MAP, new TypeToken<LinkedHashMap<String, String>>() {
                    }.getType());
                }
                if (Reservoir.contains(Constants.QUESTION_ID_SET)) {
                    mQuestionIdSet = Reservoir.get(Constants.QUESTION_ID_SET, new TypeToken<MyHashSet<String>>() {
                    }.getType());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mTextbookIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.TEXTBOOK_ID_SET);
            mChapterIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.CHAPTER_ID_SET);
            mSectionIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.SECTION_ID_SET);
            mDetailIdSet = (MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.DETAIL_ID_SET);
            LogHelper.d(TAG, "HtmlMap: " + mHtmlMap.toString());
            LogHelper.d(TAG, "TextbookIdSet: " + mTextbookIdSet.toString());
            LogHelper.d(TAG, "ChapterIdSet: " + mChapterIdSet.toString());
            LogHelper.d(TAG, "SectionIdSet: " + mSectionIdSet.toString());
            LogHelper.d(TAG, "DetailIdSet: " + mDetailIdSet.toString());
            LogHelper.d(TAG, "QusetionIdSet: " + mQuestionIdSet.toString());
        }
        if (mIsAddhomework || mIsChangeKnowledgePoint) {
            mTitleBar.setBackButton(R.mipmap.btn_close, this);
        }
        getKnowledgeTree(mGradeId, mLessonId, mTextBookId);
    }

    private void initBaseData() {
        try {
            if (Reservoir.contains(Constants.BASE_DATA_TEXTBOOK_NAME)
                    && Reservoir.contains(Constants.BASE_DATA_TEXTBOOK_ID)) {
                initData();//如果缺少基础数据的话,先获取基础数据
            } else {
                LogHelper.d(TAG, "No BaseData Cache");
                getBaseData();
            }
        } catch (Exception e) {
            LogHelper.d(TAG, "No BaseData Cache");
            getBaseData();
            e.printStackTrace();
        }
    }

    private void getBaseData() {
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

    private void saveHomeworkHttp() {
        MobclickAgent.onEvent(this, Constants.PUBLISH_HOMEWORK_TOUCHED);
        if (NetworkUtils.isNetworkAvaialble(this)) {
            try {
                if (Reservoir.contains(Constants.HOMEWORK_CACHE_TEXTBOOK_ID_SET + mHomeworkId)) {
                    mTextbookIdSet = get(Constants.HOMEWORK_CACHE_TEXTBOOK_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
                    }.getType());
                    mDetailIdSet = Reservoir.get(Constants.HOMEWORK_CACHE_DETAIL_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
                    }.getType());
                    mChapterIdSet = Reservoir.get(Constants.HOMEWORK_CACHE_CHAPTER_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
                    }.getType());
                    mSectionIdSet = Reservoir.get(Constants.HOMEWORK_CACHE_SECTION_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
                    }.getType());
                    mQuestionIdSet = Reservoir.get(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId, new TypeToken<MyHashSet<String>>() {
                    }.getType());
                    LogHelper.d(TAG, "TextbookIdSet: " + mTextbookIdSet.toString());
                    LogHelper.d(TAG, "ChapterIdSet: " + mChapterIdSet.toString());
                    LogHelper.d(TAG, "SectionIdSet: " + mSectionIdSet.toString());
                    LogHelper.d(TAG, "DetailIdSet: " + mDetailIdSet.toString());
                    LogHelper.d(TAG, "QusetionIdSet: " + mQuestionIdSet.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final String token =
                    "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            mModifyHomeworkSubscriber = new ModifyHomeworkSubscriber(this);
            mModifyHomeworkSubscription = HttpManager.modifyHomework(token, mHomeworkId, mTextbookIdSet.toString(),
                    mChapterIdSet.toString(), mSectionIdSet.toString(), mDetailIdSet.toString(), mQuestionIdSet.toString())
                    .subscribe(mModifyHomeworkSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, false);
        }
    }

    private class ModifyHomeworkSubscriber extends LoadingSubscriber<SimpleResponse> {

        public ModifyHomeworkSubscriber(Context context) {
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
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, false);
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
            ToastUtil.showToastBottom(mContext, getString(R.string.toast_save_success), Toast.LENGTH_SHORT);
            clearHomeworkLocalCache();
            RxBus.getInstance().post(new RefreshUnassignedHomeworkListEvent());
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
            mIsLoadingBaseDatasFail = true;
        }

        @Override
        protected void handleError(Throwable e) {
            mIsLoadingBaseDatasFail = true;
            switch (e.getMessage()) {
                case ApiException.TOKEN_INVALID:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
                    showTokenInvalidDialog();
                    break;
                default:
                    refreshLoadingState(Constants.LOADING_STATE_FAIL, true);
            }
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            initData();
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

    @Override
    protected void onErrorLayoutClick() {
        mErrorLayout.setVisibility(View.GONE);
        if (mIsLoadingBaseDatasFail) {
            initBaseData();
        } else {
            getKnowledgeTree(mGradeId, mLessonId, mTextBookId);
        }
    }

    private void getKnowledgeTree(int gradeId, int lessonId, int textBookId) {
        if (NetworkUtils.isNetworkAvaialble(this)) {
            final String token =
                    "Bearer " + SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN);
            mKnowledgeTreeSubscriber = new KnowledgeTreeSubscriber(this);
            mKnowledgeTreeSubscription = HttpManager.getknowledgeTree(token, gradeId, lessonId, textBookId)
                    .doOnNext(this::cacheKnowledgeTree)
                    .compose(new DefaultSchedulerTransformer<>())
                    .subscribe(mKnowledgeTreeSubscriber);
        } else {
            refreshLoadingState(Constants.LOADING_STATE_NO_NET, true);
        }
    }

    private void cacheKnowledgeTree(KnowledgePointEntity knowledgePointEntity) {
        mKnowledgeTree.clear();
        mKnowledgeTree.addAll(knowledgePointEntity.getKnowledge_chapter());
        LogHelper.d(TAG, "mKnowledgeTree :" + mKnowledgeTree);
        mChapterArray = new String[mKnowledgeTree.size()];
        for (int i = 0; i < mKnowledgeTree.size(); i++) {
            mChapterArray[i] = mKnowledgeTree.get(i).getName();
        }
    }


    private class KnowledgeTreeSubscriber extends LoadingSubscriber<KnowledgePointEntity> {

        public KnowledgeTreeSubscriber(Context context) {
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
        public void onNext(KnowledgePointEntity knowledgePointEntity) {
            super.onNext(knowledgePointEntity);
            LogHelper.d(TAG, "knowledgePointEntity: " + knowledgePointEntity);
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            refreshTreeView();
        }
    }

    private void refreshTreeView() {
        if (mKnowledgeTree.size() > 0) {
            //数据加载完后默认显示第一个章节数据
            mTvChapter.setText(mChapterArray[0]);
            mChapterId = mKnowledgeTree.get(0).getId();
            ArrayList<KnowledgePointEntity.SectionData> knowledge_section = mKnowledgeTree.get(0).getKnowledge_section();
            if (knowledge_section != null) {
                mExpandStates = new boolean[knowledge_section.size()];
                mKnowledgeSections.clear();
                mKnowledgeSections.addAll(knowledge_section);
                mChapterListAdapter.notifyDataSetChanged();
            }
            mRlChapter.setVisibility(View.VISIBLE);
            mExpandableLayoutListView.setVisibility(View.VISIBLE);
        } else {
            //暂无知识树的时候不显示
            mTvChapter.setText("");
            mExpandableLayoutListView.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.hw_rl_textbook)
    public void selectTextbook() {
        MobclickAgent.onEvent(this, Constants.SELECT_TEXTBOOK_TOUCHED);
        if (mTextBookNameList == null) {
            ToastUtil.showToastBottom(this, R.string.toast_textbook_get_failed, Toast.LENGTH_SHORT);
            return;
        }
        if (mTextBookNameList.size() > 0) {
            SinglePickerPopWin pickerPopWin = new SinglePickerPopWin.Builder(this,
                    mTextBookNameList.toArray(new String[mTextBookNameList.size()]), (position, selected) -> {
                mTvTextbook.setText(selected);
                mTextBookPickerPosition = position;
                mTextBookId = mTextBookIdList.get(position);
                getKnowledgeTree(mGradeId, mLessonId, mTextBookId);
            })
                    .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                    .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                    .selected(mTextBookPickerPosition)
                    .build();
            pickerPopWin.showPopWin(this);
        } else {
            ToastUtil.showToastBottom(this, R.string.toast_textbook_empty, Toast.LENGTH_SHORT);
        }
    }

    @OnClick(R.id.hw_rl_chapter)
    public void selectChapter() {
        if (mChapterArray.length > 0) {
            MobclickAgent.onEvent(this, Constants.SELECT_CHAPTER_TOUCHED);
            SinglePickerPopWin pickerPopWin = new SinglePickerPopWin.Builder(this,
                    mChapterArray, (position, selected) -> {
                mTvChapter.setText(selected);
                mChapterPickerPosition = position;
                mChapterId = mKnowledgeTree.get(position).getId();
                mProgressDialog.show();
                Observable.timer(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            ArrayList<KnowledgePointEntity.SectionData> knowledge_section = mKnowledgeTree.get(position).getKnowledge_section();
                            LogHelper.d(TAG, "knowledge_section: " + knowledge_section);
                            if (knowledge_section != null) {
                                mExpandStates = new boolean[knowledge_section.size()];
                                mKnowledgeSections.clear();
                                mKnowledgeSections.addAll(knowledge_section);
                                mChapterListAdapter.notifyDataSetChanged();
                            }
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                        });
            })
                    .btnTextSize(SELECTOR_BTN_TEXT_SIZE) // button text size
                    .viewTextSize(SELECTOR_TEXT_SIZE) // pick view text size
                    .selected(mChapterPickerPosition)
                    .build();
            pickerPopWin.showPopWin(this);
        } else {
            CustomDialog.Builder ibuilder = new CustomDialog.Builder(this);
            ibuilder.setMessage(getString(R.string.dialog_msg_nochapter));
            ibuilder.setPositiveButton(R.string.btn_know, (dialog, which) -> {
                dialog.dismiss();
            });
            CustomDialog customDialog = ibuilder.create();
            customDialog.setCancelable(false);
            customDialog.show();
        }
    }

    public class ChapterListAdapter extends BaseAdapter<KnowledgePointEntity.SectionData> {

        public ChapterListAdapter(Context context, List<KnowledgePointEntity.SectionData> dataList) {
            super(context, dataList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            KnowledgePointEntity.SectionData sectionData = mDatas.get(position);
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.view_expandable_row, null);
                ExpandableLayoutItem expandableLayoutItem = (ExpandableLayoutItem) convertView.findViewById(R.id.row);
                FrameLayout contentLayout = expandableLayoutItem.getContentLayout();
                holder.listview = (ListView) contentLayout.findViewById(R.id.listview_sec);
                holder.title = (TextView) expandableLayoutItem.getHeaderLayout().findViewById(R.id.tv_chapter_title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.title.setText(sectionData.getName());
            ArrayList<KnowledgePointEntity.SectionDetail> sectionDetails = sectionData.getKnowledge_detail();
            if (sectionDetails != null) {
                int sectionId = sectionData.getId();
                KnowledgeSectionAdapter knowledgeSectionAdapter = new KnowledgeSectionAdapter(mContext, sectionDetails);
                holder.listview.setAdapter(knowledgeSectionAdapter);
                View adapterView = holder.listview.getAdapter().getView(0, null, holder.listview);
                adapterView.measure(0, 0);
                int height = adapterView.getMeasuredHeight();
                int dividerHeight = holder.listview.getDividerHeight();
                //-dividerHeight:隐藏最后一项的divider
                int listViewHeight = (height + dividerHeight) * knowledgeSectionAdapter.getCount() - dividerHeight;
                RelativeLayout.LayoutParams layoutParams =
                        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listViewHeight);
                layoutParams.leftMargin = (int) mContext.getResources().getDimension(R.dimen.width_knowlege_point_list_icon);
                holder.listview.setLayoutParams(layoutParams);
                holder.listview.setOnItemClickListener((parent1, view1, position1, id1) -> {
                    LogHelper.d(TAG, "OnItemClick: " + position + position1);
                    KnowledgePointEntity.SectionDetail sectionDetail = sectionDetails.get(position1);
                    int sectionDetailId = sectionDetail.getId();
                    String sectionDetailName = sectionDetail.getName();
                    if (mIsChangeKnowledgePoint) {
                        RxBus.getInstance().post(new ChangeknowledgePointEvent(mTextBookId, mChapterId,
                                sectionId, sectionDetailId, sectionDetailName));
                        finishActivity();
                    } else {
                        Intent intent = new Intent(mContext, HomeworkSelectActivity.class);
                        intent.putExtra(Constants.LESSON_ID, mLessonId);
                        intent.putExtra(Constants.TEXTBOOK_ID, mTextBookId);
                        intent.putExtra(Constants.CHAPTER_ID, mChapterId);
                        intent.putExtra(Constants.GRADE_ID, mGradeId);
                        intent.putExtra(Constants.SECTION_ID, sectionId);
                        intent.putExtra(Constants.KNOWLEDGE_DETAIL_ID, sectionDetailId);
                        intent.putExtra(Constants.KNOWLEDGE_POINT, sectionDetailName);
                        intent.putExtra(Constants.HOMEWORK_ID, mHomeworkId);
                        intent.putExtra(Constants.HOMEWORK_TITLE, mHomeworkTitle);
                        if (mIsAddhomework) {
                            intent.putExtra(Constants.IS_ADD_HOMEWORK, true);
                            try {
                                Reservoir.put(Constants.HOMEWORK_HTML_MAP, mHtmlMap);
                                Reservoir.put(Constants.QUESTION_ID_SET, mQuestionIdSet);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            intent.putExtra(Constants.TEXTBOOK_ID_SET, mTextbookIdSet);
                            intent.putExtra(Constants.CHAPTER_ID_SET, mChapterIdSet);
                            intent.putExtra(Constants.SECTION_ID_SET, mSectionIdSet);
                            intent.putExtra(Constants.DETAIL_ID_SET, mDetailIdSet);
                        }
                        mContext.startActivity(intent);
                        if (mIsAddhomework) {
                            finishActivity();
                        }
                    }
                });
            }
            ExpandableLayoutItem expandableLayoutItem = (ExpandableLayoutItem) convertView.findViewById(R.id.row);
            expandableLayoutItem.setOnExpandListener(isExpanded -> {
                LogHelper.d(TAG, "OnExpand: " + "position-" + position + " isExpand-" + isExpanded);
                mExpandStates[position] = isExpanded;
            });
            if (mExpandStates[position]) {
                expandableLayoutItem.showNow();
            } else {
                expandableLayoutItem.hideNow();
            }
            return convertView;
        }

        public class ViewHolder {
            public TextView title;
            public ListView listview;
        }
    }

    private void finishActivity() {
        if (mIsChangeKnowledgePoint || mIsAddhomework) {
            finish();
            overridePendingTransition(R.anim.pop_win_content_fade_in, R.anim.slide_bottom_out);
        } else {
            //首次选择知识点页面,back时根据有没有缓存弹出提示框提示保存
            try {
                if (Reservoir.contains(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId)) {
                    showDialog();
                } else {
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }
    }

    private void showDialog() {
        CustomDialog.Builder ibuilder = new CustomDialog.Builder(this);
        ibuilder.setMessage(R.string.dialog_unsave_homework_save);
        ibuilder.setPositiveButton(R.string.save, (dialog, which) -> {
            dialog.dismiss();
            saveHomeworkHttp();
        });
        ibuilder.setNegativeButton(R.string.unsave, (dialog, which) -> {
            dialog.dismiss();
            clearHomeworkLocalCache();
            finish();
        });
        ibuilder.create().show();
    }

    /**
     * 清除本地修改的缓存
     */
    private void clearHomeworkLocalCache() {
        try {
            if (Reservoir.contains(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId)) {
                Reservoir.delete(Constants.HOMEWORK_CACHE_TEXTBOOK_ID_SET + mHomeworkId);
                Reservoir.delete(Constants.HOMEWORK_CACHE_CHAPTER_ID_SET + mHomeworkId);
                Reservoir.delete(Constants.HOMEWORK_CACHE_SECTION_ID_SET + mHomeworkId);
                Reservoir.delete(Constants.HOMEWORK_CACHE_DETAIL_ID_SET + mHomeworkId);
                Reservoir.delete(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId);
                Reservoir.delete(Constants.HOMEWORK_CACHE_HTML_MAP + mHomeworkId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackStack() {
        finishActivity();
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    @Subscribe
    public void savaHomework(ModifyHomeworkEvent modifyHomeworkEvent) {
        finish();
    }

    @Subscribe
    public void assignHomework(AssignHomeworkEvent assignHomeworkEvent) {
        finish();
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
        if (mKnowledgeTreeSubscriber != null) {
            mKnowledgeTreeSubscriber.onCancle();
        }
        if (mKnowledgeTreeSubscription != null && !mKnowledgeTreeSubscription.isUnsubscribed()) {
            mKnowledgeTreeSubscription.unsubscribe();
        }
        if (mModifyHomeworkSubscriber != null) {
            mModifyHomeworkSubscriber.onCancle();
        }
        if (mModifyHomeworkSubscription != null && !mModifyHomeworkSubscription.isUnsubscribed()) {
            mModifyHomeworkSubscription.unsubscribe();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(this);
    }
}
