package com.oceansky.calendar.example.manager;

import android.content.Context;
import android.text.TextUtils;

import com.oceansky.calendar.example.constant.Constants;
import com.oceansky.calendar.example.network.response.RegisterEntity;
import com.oceansky.calendar.example.network.response.LoginEntity;
import com.oceansky.calendar.example.utils.LogHelper;
import com.oceansky.calendar.example.utils.SecurePreferences;
import com.oceansky.calendar.example.utils.SharePreferenceUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by tangqifa on 16/3/18.
 */
public class AcountManager {

    private static final String TAG = "AcountManager";

    public static void saveAcountInfo(Context context, RegisterEntity registerEntity) {
        LogHelper.d(TAG, "registerEntity : " + registerEntity);
        int usrID = registerEntity.getUser_id();
        String access_token = registerEntity.getAccess_token();
        String token_type = registerEntity.getToken_type();
        int expires_in = registerEntity.getExpires_in();
        RegisterEntity.Extra extra = registerEntity.getExtra();
        if (extra != null) {
            int teacher_id = extra.getTeacher_id();
            SecurePreferences.getInstance(context, false).put(Constants.KEY_TEACHER_ID, teacher_id + "");
            int teacher_status = extra.getStatus();
            SharePreferenceUtils.setIntPref(context, Constants.TEACHER_STATUS, teacher_status);
            int grade_id = extra.getGrade_id();
            int lesson_id = extra.getLesson_id();
            SharePreferenceUtils.setIntPref(context, Constants.GRADE_ID, grade_id);
            SharePreferenceUtils.setIntPref(context, Constants.LESSON_ID, lesson_id);
        }
        if (!(TextUtils.isEmpty(access_token) && TextUtils.isEmpty(token_type)
                && TextUtils.isEmpty("" + usrID) && TextUtils.isEmpty("" + expires_in))) {
            SecurePreferences.getInstance(context, false).put(Constants.KEY_ACCESS_TOKEN, access_token);
            SecurePreferences.getInstance(context, false).put(Constants.KEY_EXPIRES_IN, expires_in + "");
            SecurePreferences.getInstance(context, false).put(Constants.KEY_TOKEN_TYPE, token_type);
            SecurePreferences.getInstance(context, false).put(Constants.KEY_USER_ID, usrID + "");
            //友盟 账号统计
            MobclickAgent.onProfileSignIn(usrID + "");
        }
        LogHelper.d(TAG, "Register-->" + "KEY_ACCESS_TOKEN: " + SecurePreferences.getInstance(context, false).getString(Constants.KEY_ACCESS_TOKEN)
                + "，KEY_EXPIRES_IN: " + SecurePreferences.getInstance(context, false).getString(Constants.KEY_EXPIRES_IN)
                + "，KEY_TOKEN_TYPE: " + SecurePreferences.getInstance(context, false).getString(Constants.KEY_TOKEN_TYPE)
                + "，KEY_USER_ID: " + SecurePreferences.getInstance(context, false).getString(Constants.KEY_USER_ID)
        );
    }

    public static void saveAcountInfo(Context context, LoginEntity loginEntity) {
        if (loginEntity == null) {
            return;
        }
        int usrID = loginEntity.getUser_id();
        String access_token = loginEntity.getAccess_token();
        String token_type = loginEntity.getToken_type();
        int expires_in = loginEntity.getExpires_in();
        LoginEntity.Extra extra = loginEntity.getExtra();
        if (extra != null) {
            int teacher_id = extra.getTeacher_id();
            SecurePreferences.getInstance(context, false).put(Constants.KEY_TEACHER_ID, teacher_id + "");
            int teacher_status = extra.getStatus();
            SharePreferenceUtils.setIntPref(context, Constants.TEACHER_STATUS, teacher_status);
            int grade_id = extra.getGrade_id();
            int lesson_id = extra.getLesson_id();
            SharePreferenceUtils.setIntPref(context, Constants.GRADE_ID, grade_id);
            SharePreferenceUtils.setIntPref(context, Constants.LESSON_ID, lesson_id);
        }
        LogHelper.d(TAG, "usrID: " + usrID + ",access_token: " + access_token);
        LogHelper.d(TAG, "token_type: " + token_type + ",expires_in: " + expires_in);
        if (!(TextUtils.isEmpty(access_token) && TextUtils.isEmpty(token_type)
                && TextUtils.isEmpty("" + usrID) && TextUtils.isEmpty("" + expires_in))) {
            SecurePreferences.getInstance(context, true).put(Constants.KEY_ACCESS_TOKEN, access_token);
            SecurePreferences.getInstance(context, true).put(Constants.KEY_EXPIRES_IN, expires_in + "");
            SecurePreferences.getInstance(context, true).put(Constants.KEY_TOKEN_TYPE, token_type);
            SecurePreferences.getInstance(context, true).put(Constants.KEY_USER_ID, usrID + "");
            //友盟 账号统计
            MobclickAgent.onProfileSignIn(usrID + "");
        }
        LogHelper.d(TAG, "Login-->" + "KEY_ACCESS_TOKEN: " + SecurePreferences.getInstance(context, false).getString(Constants.KEY_ACCESS_TOKEN)
                + "，KEY_EXPIRES_IN: " + SecurePreferences.getInstance(context, false).getString(Constants.KEY_EXPIRES_IN)
                + "，KEY_TOKEN_TYPE: " + SecurePreferences.getInstance(context, false).getString(Constants.KEY_TOKEN_TYPE)
                + "，KEY_USER_ID: " + SecurePreferences.getInstance(context, false).getString(Constants.KEY_USER_ID)
        );
    }
}
