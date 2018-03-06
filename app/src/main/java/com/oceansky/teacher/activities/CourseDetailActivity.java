package com.oceansky.teacher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.constant.FeatureConfig;
import com.oceansky.teacher.fragments.CaldroidCustomFragment;
import com.oceansky.teacher.utils.LogHelper;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;


public class CourseDetailActivity extends BaseWebView {

    private final static String TAG = CourseDetailActivity.class.getSimpleName();

    private Date             mCurrentDate;
    private String           mTimeStr;
    private CaldroidFragment mDialogCaldroidFragment;
    private CaldroidListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        initView();
    }

    private void initView() {
        mWebView.addJavascriptInterface(new JsInteration(), "controller");
        mDialogCaldroidFragment = new CaldroidCustomFragment();
        // Setup listener
        listener = new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                mDialogCaldroidFragment.clearSelectedDates();
                mDialogCaldroidFragment.setSelectedDate(date);
                mDialogCaldroidFragment.refreshView();
                LogHelper.d(TAG, "date: " + date);
                mCurrentDate = date;
            }
        };
        mDialogCaldroidFragment.setCaldroidListener(listener);
    }

    private void setCustomResourceForDates() {
        if (TextUtils.isEmpty(mTimeStr))
            return;
        Calendar cal = Calendar.getInstance();
        Date todayDate = cal.getTime();
        LogHelper.d(TAG, "todayDate: " + todayDate);
        //设置今日
        LogHelper.d(TAG, "mCurrentDate: " + mCurrentDate);
        if (mCurrentDate != null) {
            todayDate = mCurrentDate;
        }
        mDialogCaldroidFragment.setSelectedDate(todayDate);
        try {
            JSONObject jsonObject = new JSONObject(mTimeStr);
            JSONArray namesArray = jsonObject.names();
            LogHelper.d(TAG, "namesArray: " + namesArray);
            int lenth = namesArray.length();
            for (int i = 0; i < lenth; i++) {
                String monthStr = namesArray.getString(i);
                LogHelper.d(TAG, "monthStr: " + monthStr);
                JSONArray dayArray = jsonObject.getJSONArray(monthStr);
                int size = dayArray.length();
                for (int k = 0; k < size; k++) {
                    int day = dayArray.getInt(k);
                    int year = Integer.valueOf(monthStr.substring(0, 4));
                    int months = Integer.valueOf(monthStr.substring(4));
                    LogHelper.d(TAG, "year:" + year + ",months: " + months + ",day: " + day);

                    cal = Calendar.getInstance();
                    cal.set(year, months - 1, day);
                    LogHelper.d(TAG, "cal:" + cal.getTime());
                    mDialogCaldroidFragment.setTextUndelineForDate(1, cal.getTime());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class JsInteration {
        @JavascriptInterface
        public void show(final String timeInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogHelper.d(TAG, "timeInfo: " + timeInfo);
                    mTimeStr = timeInfo;
                    //fixed bug ARD-186 start
                    mCurrentDate = null;
                    mDialogCaldroidFragment = new CaldroidCustomFragment();
                    mDialogCaldroidFragment.setCaldroidListener(listener);
                    //fixed bug ARD-186 end
                    setCustomResourceForDates();
                    mDialogCaldroidFragment.show(getSupportFragmentManager(), "dialog");
                }
            });
        }

        @JavascriptInterface
        public void redirect(final String teacherId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent it = new Intent(CourseDetailActivity.this, TeacherDetailActivity.class);
                    it.putExtra(Constants.WEBVIEW_URL, Constants.TEACHER_URL + "/" + teacherId);
                    it.putExtra(Constants.WEBVIEW_TITLE, getString(R.string.title_teacher_detail));
                    startActivity(it);
                }
            });
        }

        @JavascriptInterface
        public void redirectOutline(final String url) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogHelper.d(TAG, "url: " + url);
                    Intent it = new Intent(CourseDetailActivity.this, CourseDetailActivity.class);
                    it.putExtra(Constants.WEBVIEW_URL, FeatureConfig.BASE_URL + "/" + url);
                    it.putExtra(Constants.WEBVIEW_TITLE, mTitle);
                    startActivity(it);
                }
            });
        }
    }
}
