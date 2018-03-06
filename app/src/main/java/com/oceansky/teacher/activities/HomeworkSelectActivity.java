package com.oceansky.teacher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.anupcowkur.reservoir.Reservoir;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.event.AssignHomeworkEvent;
import com.oceansky.teacher.event.ChangeHomeworkTypeEvent;
import com.oceansky.teacher.event.ChangeSetEvent;
import com.oceansky.teacher.event.ChangeknowledgePointEvent;
import com.oceansky.teacher.event.DeleteHomeworkEvent;
import com.oceansky.teacher.event.ModifyHomeworkEvent;
import com.oceansky.teacher.event.RxBus;
import com.oceansky.teacher.event.SelectHomeworkEvent;
import com.oceansky.teacher.fragments.HomeworkSelectWebviewFragment;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.MyHashSet;
import com.oceansky.teacher.utils.SharePreferenceUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.anupcowkur.reservoir.Reservoir.get;

public class HomeworkSelectActivity extends BaseActivityWithLoadingState implements OnTabSelectListener, ViewPager.OnPageChangeListener {

    private static final String TAG = HomeworkSelectActivity.class.getSimpleName();

    @Bind(R.id.homework_rg_type)
    RadioGroup mRgHomeworkType;

    @Bind(R.id.select_btn1)
    Button mBtnPreview;

    @Bind(R.id.select_btn2)
    Button mBtnSave;

    @Bind(R.id.select_tv_selected)
    TextView mTvSelected;

    @Bind(R.id.tv_change)
    TextView mTvChangeSet;

    private SlidingTabLayout mTabLayout;
    private int              mLessonId;
    private String           mKnowledgeDetail;
    private int              mCurrentDifficulty;
    private int              mTextbookId;
    private int              mChapterId;
    private int              mSectionID;
    private int              mKnowledgeDetailId;
    private int              mGradeId;
    private int              mHomeworkId;
    private String           mHomeworkTitle;
    private boolean          mIsAddHomework;
    private ViewPager        mViewPager;
    private Adapter          mAdapter;
    private int              mHomeworkType;

    private MyHashSet<String>             mQuestionIdSet = new MyHashSet<>();
    private MyHashSet<Integer>            mTextbookIdSet = new MyHashSet<>();
    private MyHashSet<Integer>            mChapterIdSet  = new MyHashSet<>();
    private MyHashSet<Integer>            mSectionIdSet  = new MyHashSet<>();
    private MyHashSet<Integer>            mDetailIdSet   = new MyHashSet<>();
    private LinkedHashMap<String, String> mHtmlMap       = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_select);
        ButterKnife.bind(this);
        RxBus.getInstance().register(this);
        initView();
        initData();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogHelper.d(TAG, "onResume");
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
                mHtmlMap = Reservoir.get(Constants.HOMEWORK_CACHE_HTML_MAP + mHomeworkId, new TypeToken<LinkedHashMap<String, String>>() {
                }.getType());
                LogHelper.d(TAG, "HtmlMap: " + mHtmlMap.toString());
                LogHelper.d(TAG, "TextbookIdSet: " + mTextbookIdSet.toString());
                LogHelper.d(TAG, "ChapterIdSet: " + mChapterIdSet.toString());
                LogHelper.d(TAG, "SectionIdSet: " + mSectionIdSet.toString());
                LogHelper.d(TAG, "DetailIdSet: " + mDetailIdSet.toString());
                LogHelper.d(TAG, "QusetionIdSet: " + mQuestionIdSet.toString());
                refreshBottomTabview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_homework_select));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);
        mTitleBar.setSettingButtonText(getString(R.string.homework_change_knowlege_point));
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(3);
        mAdapter = new Adapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);
        mTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        mRgHomeworkType.check(R.id.homework_rb_choice);//默认选择题
        mBtnPreview.setText(R.string.homework_btn_preview);
    }

    private void initData() {
        mTextbookId = getIntent().getIntExtra(Constants.TEXTBOOK_ID, -1);
        mChapterId = getIntent().getIntExtra(Constants.CHAPTER_ID, -1);
        mSectionID = getIntent().getIntExtra(Constants.SECTION_ID, -1);
        mKnowledgeDetailId = getIntent().getIntExtra(Constants.KNOWLEDGE_DETAIL_ID, -1);
        mKnowledgeDetail = getIntent().getStringExtra(Constants.KNOWLEDGE_POINT);
        SharePreferenceUtils.setStringPref(this, Constants.KNOWLEDGE_DETAIL_ID, mKnowledgeDetailId + "");
        SharePreferenceUtils.setStringPref(this, Constants.KNOWLEDGE_POINT, mKnowledgeDetail);
        mGradeId = getIntent().getIntExtra(Constants.GRADE_ID, -1);
        mHomeworkTitle = getIntent().getStringExtra(Constants.HOMEWORK_TITLE);
        mHomeworkId = getIntent().getIntExtra(Constants.HOMEWORK_ID, -1);
        mLessonId = getIntent().getIntExtra(Constants.LESSON_ID, -1);
        mHomeworkType = Constants.HOMEWORK_TYPE_CHOICE;//default
        SharePreferenceUtils.setIntPref(this, Constants.HOMEWORK_TYPE, Constants.HOMEWORK_TYPE_CHOICE);
        LogHelper.d(TAG, "lessonId: " + mLessonId);
        LogHelper.d(TAG, "knowledgePoint: " + mKnowledgeDetail);
        LogHelper.d(TAG, "TextbookId: " + mTextbookId);
        LogHelper.d(TAG, "ChapterId: " + mChapterId);
        LogHelper.d(TAG, "SectionID: " + mSectionID);
        LogHelper.d(TAG, "KnowledgeDetailId: " + mKnowledgeDetailId);
        LogHelper.d(TAG, "GradeId: " + mGradeId);
        mIsAddHomework = getIntent().getBooleanExtra(Constants.IS_ADD_HOMEWORK, false);
        LogHelper.d(TAG, "IsAddHomework: " + mIsAddHomework);
        if (mIsAddHomework) {
            try {
                LinkedHashMap htmlMap = Reservoir.get(Constants.HOMEWORK_HTML_MAP, new TypeToken<LinkedHashMap<String, String>>() {
                }.getType());
                mHtmlMap.putAll(htmlMap);
                MyHashSet<String> questionIdSet = Reservoir.get(Constants.QUESTION_ID_SET, new TypeToken<MyHashSet<String>>() {
                }.getType());
                if (questionIdSet != null) {
                    mQuestionIdSet.addAll(questionIdSet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mTextbookIdSet.addAll((MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.TEXTBOOK_ID_SET));
            mChapterIdSet.addAll((MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.CHAPTER_ID_SET));
            mSectionIdSet.addAll((MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.SECTION_ID_SET));
            mDetailIdSet.addAll((MyHashSet<Integer>) getIntent().getSerializableExtra(Constants.DETAIL_ID_SET));
        } else {
            try {
                if (Reservoir.contains(Constants.HOMEWORK_CACHE_TEXTBOOK_ID_SET + mHomeworkId)) {
                    mTextbookIdSet.addAll(get(Constants.HOMEWORK_CACHE_TEXTBOOK_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
                    }.getType()));
                    mDetailIdSet.addAll(Reservoir.get(Constants.HOMEWORK_CACHE_DETAIL_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
                    }.getType()));
                    mChapterIdSet.addAll(Reservoir.get(Constants.HOMEWORK_CACHE_CHAPTER_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
                    }.getType()));
                    mSectionIdSet.addAll(Reservoir.get(Constants.HOMEWORK_CACHE_SECTION_ID_SET + mHomeworkId, new TypeToken<MyHashSet<Integer>>() {
                    }.getType()));
                    mQuestionIdSet.addAll(Reservoir.get(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId, new TypeToken<MyHashSet<String>>() {
                    }.getType()));
                    mHtmlMap.putAll(Reservoir.get(Constants.HOMEWORK_CACHE_HTML_MAP + mHomeworkId, new TypeToken<LinkedHashMap<String, String>>() {
                    }.getType()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LogHelper.d(TAG, "HtmlMap: " + mHtmlMap.toString());
        LogHelper.d(TAG, "TextbookIdSet: " + mTextbookIdSet.toString());
        LogHelper.d(TAG, "ChapterIdSet: " + mChapterIdSet.toString());
        LogHelper.d(TAG, "SectionIdSet: " + mSectionIdSet.toString());
        LogHelper.d(TAG, "DetailIdSet: " + mDetailIdSet.toString());
        LogHelper.d(TAG, "QusetionIdSet: " + mQuestionIdSet.toString());
        mCurrentDifficulty = Constants.HOMEWORK_DIFFICULTY_EASY;//默认
        refreshBottomTabview();
        setupViewPager();
        mTabLayout.setViewPager(mViewPager);
    }

    private void initListener() {
        mTabLayout.setOnTabSelectListener(this);
        mRgHomeworkType.setOnCheckedChangeListener((group, checkedId) -> {
            MobclickAgent.onEvent(HomeworkSelectActivity.this, Constants.SELECT_QUESTION_TYPE_TOUCHED);
            switch (checkedId) {
                case R.id.homework_rb_choice:
                    mHomeworkType = Constants.HOMEWORK_TYPE_CHOICE;
                    SharePreferenceUtils.setIntPref(this, Constants.HOMEWORK_TYPE, Constants.HOMEWORK_TYPE_CHOICE);
                    LogHelper.d(TAG, "onCheckedChanged: HOMEWORK_TYPE_CHOICE");
                    mTvChangeSet.setEnabled(true);
                    break;
                case R.id.homework_rb_completion:
                    mHomeworkType = Constants.HOMEWORK_TYPE_COMPLETION;
                    SharePreferenceUtils.setIntPref(this, Constants.HOMEWORK_TYPE, Constants.HOMEWORK_TYPE_COMPLETION);
                    LogHelper.d(TAG, "onCheckedChanged: HOMEWORK_TYPE_COMPLETION");
                    mTvChangeSet.setEnabled(false);
                    break;
                case R.id.homework_rb_checking:
                    mHomeworkType = Constants.HOMEWORK_TYPE_CHECKING;
                    SharePreferenceUtils.setIntPref(this, Constants.HOMEWORK_TYPE, Constants.HOMEWORK_TYPE_CHECKING);
                    LogHelper.d(TAG, "onCheckedChanged: HOMEWORK_TYPE_CHECKING");
                    mTvChangeSet.setEnabled(false);
                    break;
            }
            RxBus.getInstance().post(new ChangeHomeworkTypeEvent(mHomeworkType));
        });
    }

    private void refreshBottomTabview() {
        int size = mQuestionIdSet.size();
        mTvSelected.setText("已选择" + size + "道题");
        if (size > 0) {
            mBtnPreview.setEnabled(true);
        } else {
            mBtnPreview.setEnabled(false);
        }
    }

    @OnClick(R.id.select_btn1)
    public void homeworkPreview() {
        Intent intent = new Intent(this, PreviewLocalDataActivity.class);
        intent.putExtra(Constants.HOMEWORK_ID, mHomeworkId);
        intent.putExtra(Constants.HOMEWORK_TITLE, mHomeworkTitle);
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
        startActivity(intent);
    }

    @OnClick(R.id.tv_setting)
    public void changeKnowledgePoint() {
        // 更换知识点
        Intent intent = new Intent(this, KnowledgePointSelectActivity.class);
        intent.putExtra(Constants.IS_CHANGE_KNOWLEDGE_POINT, true);
        intent.putExtra(Constants.GRADE_ID, mGradeId);
        intent.putExtra(Constants.LESSON_ID, mLessonId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_bottom_in, R.anim.pop_win_content_fade_out);
    }

    private void setupViewPager() {
        mAdapter.clearFragment();
        HomeworkSelectWebviewFragment easyFragment = new HomeworkSelectWebviewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.HOMEWORK_DIFFICULTY, Constants.HOMEWORK_DIFFICULTY_EASY);
        bundle.putInt(Constants.LESSON_ID, mLessonId);
        bundle.putInt(Constants.HOMEWORK_ID, mHomeworkId);
        bundle.putSerializable(Constants.QUESTION_ID_SET, mQuestionIdSet);
        easyFragment.setArguments(bundle);
        mAdapter.addFragment(easyFragment, getString(R.string.homework_difficulty_easy));
        HomeworkSelectWebviewFragment mediumFragment = new HomeworkSelectWebviewFragment();
        Bundle bundle2 = new Bundle();
        bundle2.putInt(Constants.HOMEWORK_DIFFICULTY, Constants.HOMEWORK_DIFFICULTY_MEDIUM);
        bundle2.putInt(Constants.LESSON_ID, mLessonId);
        bundle2.putInt(Constants.HOMEWORK_ID, mHomeworkId);
        bundle2.putSerializable(Constants.QUESTION_ID_SET, mQuestionIdSet);
        mediumFragment.setArguments(bundle2);
        mAdapter.addFragment(mediumFragment, getString(R.string.homework_difficulty_medium));
        HomeworkSelectWebviewFragment hardFragment = new HomeworkSelectWebviewFragment();
        Bundle bundle3 = new Bundle();
        bundle3.putInt(Constants.HOMEWORK_DIFFICULTY, Constants.HOMEWORK_DIFFICULTY_HARD);
        bundle3.putInt(Constants.LESSON_ID, mLessonId);
        bundle3.putInt(Constants.HOMEWORK_ID, mHomeworkId);
        bundle3.putSerializable(Constants.QUESTION_ID_SET, mQuestionIdSet);
        hardFragment.setArguments(bundle3);
        mAdapter.addFragment(hardFragment, getString(R.string.homework_difficulty_hard));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTabSelect(int position) {
        LogHelper.d(TAG, "onTabSelect: " + position);
        MobclickAgent.onEvent(HomeworkSelectActivity.this, Constants.SELECT_QUESTION_LEVEL_TOUCHED);
        switch (position) {
            case 0:
                mCurrentDifficulty = Constants.HOMEWORK_DIFFICULTY_EASY;
                break;
            case 1:
                mCurrentDifficulty = Constants.HOMEWORK_DIFFICULTY_MEDIUM;
                break;
            case 2:
                mCurrentDifficulty = Constants.HOMEWORK_DIFFICULTY_HARD;
                break;
        }
    }

    @Override
    public void onPageSelected(int position) {
        LogHelper.d(TAG, "onPageSelected: " + position);
        switch (position) {
            case 0:
                mCurrentDifficulty = Constants.HOMEWORK_DIFFICULTY_EASY;
                break;
            case 1:
                mCurrentDifficulty = Constants.HOMEWORK_DIFFICULTY_MEDIUM;
                break;
            case 2:
                mCurrentDifficulty = Constants.HOMEWORK_DIFFICULTY_HARD;
                break;
        }
    }

    @Override
    public void onTabReselect(int position) {
        LogHelper.d(TAG, "onTabReselect: " + position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments      = new ArrayList<>();
        private final List<String>   mFragmentTitles = new ArrayList<>();
        private final FragmentManager mFm;

        public Adapter(FragmentManager fm) {
            super(fm);
            mFm = fm;
        }

        public void clearFragment() {
            if (mFragments.size() > 0) {
                for (int i = 0; i < mFragments.size(); i++) {
                    mFm.beginTransaction().remove(mFragments.get(i)).commit();
                }
                mFragments.clear();
                mFragmentTitles.clear();
            }
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @OnClick(R.id.tv_change)
    public void changeSet() {
        RxBus.getInstance().post(new ChangeSetEvent(mCurrentDifficulty));
    }

    /**
     * 对作业的修改进行缓存
     */
    private void cacheOperator() {
        try {
            Reservoir.put(Constants.HOMEWORK_CACHE_QUESTION_ID_SET + mHomeworkId, mQuestionIdSet);
            Reservoir.put(Constants.HOMEWORK_CACHE_HTML_MAP + mHomeworkId, mHtmlMap);
            Reservoir.put(Constants.HOMEWORK_CACHE_TEXTBOOK_ID_SET + mHomeworkId, mTextbookIdSet);
            Reservoir.put(Constants.HOMEWORK_CACHE_CHAPTER_ID_SET + mHomeworkId, mChapterIdSet);
            Reservoir.put(Constants.HOMEWORK_CACHE_SECTION_ID_SET + mHomeworkId, mSectionIdSet);
            Reservoir.put(Constants.HOMEWORK_CACHE_DETAIL_ID_SET + mHomeworkId, mDetailIdSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void selectHomework(SelectHomeworkEvent selectHomeworkEvent) {
        LogHelper.d(TAG, "Subscribe selectHomework");
        String question_id = selectHomeworkEvent.getQuestion_id();
        String question_html = selectHomeworkEvent.getQuestion_html();
        mHtmlMap.put(question_id, question_html);
        mQuestionIdSet.add(question_id);
        mTextbookIdSet.add(mTextbookId);
        mChapterIdSet.add(mChapterId);
        mSectionIdSet.add(mSectionID);
        mDetailIdSet.add(mKnowledgeDetailId);
        refreshBottomTabview();
        cacheOperator();
    }

    @Subscribe
    public void deleteHomework(DeleteHomeworkEvent deleteHomeworkEvent) {
        LogHelper.d(TAG, "Subscribe deleteHomework");
        String question_id = deleteHomeworkEvent.getQuestion_ids();
        mHtmlMap.remove(question_id);
        mQuestionIdSet.remove(question_id);
        refreshBottomTabview();
        cacheOperator();
    }

    @Subscribe
    public void changeKnowledgePoint(ChangeknowledgePointEvent changeknowledgePointEvent) {
        LogHelper.d(TAG, "Subscribe changeKnowledgePoint");
        mTextbookId = changeknowledgePointEvent.getTextbook_ids();
        mChapterId = changeknowledgePointEvent.getKnowledge_chapter_ids();
        mSectionID = changeknowledgePointEvent.getKnowledge_section_ids();
        mKnowledgeDetailId = changeknowledgePointEvent.getKnowledge_detail_ids();
        mKnowledgeDetail = changeknowledgePointEvent.getKnowledge_detail();
        SharePreferenceUtils.setStringPref(this, Constants.KNOWLEDGE_DETAIL_ID, mKnowledgeDetailId + "");
        SharePreferenceUtils.setStringPref(this, Constants.KNOWLEDGE_POINT, mKnowledgeDetail);
    }

    @Subscribe
    public void modifyHomeworkSuccess(ModifyHomeworkEvent modifyHomeworkEvent) {
        finish();
    }

    @Subscribe
    public void assignHomework(AssignHomeworkEvent assignHomeworkEvent) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(this);
    }
}

