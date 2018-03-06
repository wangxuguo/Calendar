package com.oceansky.teacher.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.oceansky.teacher.AndroidApplication;
import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.customviews.CustomDialog;
import com.oceansky.teacher.customviews.CustomProgressDialog;
import com.oceansky.teacher.utils.DataCleanUtils;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.NetworkUtils;
import com.oceansky.teacher.utils.SecurePreferences;
import com.oceansky.teacher.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import cn.bmob.v3.update.UpdateStatus;

public class SettingActivity extends BaseActivityWithLoadingState {
    private static final String TAG = SettingActivity.class.getSimpleName();

    public static final  int MSG_SUCCESS                 = 1;
    private static final int MSG_ERROR                   = 0;
    private static final int RESET_PASSWORD              = 1001;
    private static final int PERMISSIONS_REQUEST_STORAGE = 1;
    @Bind(R.id.setting_update_has_update)
    TextView       mTvHasUpdate;
    @Bind(R.id.setting_tv_cache)
    TextView       mTvCache;
    @Bind(R.id.setting_modify_pwd)
    RelativeLayout mRlResetPwd;
    @Bind(R.id.setting_login_out)
    Button         mBtnLoginout;
    @Bind(R.id.setting_line)
    View           mLine;
    @Bind(R.id.setting_update)
    RelativeLayout mRlUpdate;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            hideProgress();
            switch (msg.what) {
                case 1:
                    showCacheSize();
                    ToastUtil.showToastBottom(SettingActivity.this, R.string.toast_clean_cache_finished, Toast.LENGTH_SHORT);
                    break;
                case 0:
                    String errorMsg = (String) msg.obj;
                    ToastUtil.showToastBottom(SettingActivity.this, errorMsg, Toast.LENGTH_SHORT);
                    break;
            }
            return false;
        }
    });
    private CustomProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        initView();
        initListener();
    }

    private void initView() {
        mTitleBar.setTitle(getString(R.string.title_setting));
        mTitleBar.setBackButton(R.mipmap.icon_back_white, this);

        if (SecurePreferences.getInstance(this, false).getString(Constants.KEY_ACCESS_TOKEN) == null) {
            mRlResetPwd.setVisibility(View.GONE);
            mBtnLoginout.setVisibility(View.INVISIBLE);
            mLine.setVisibility(View.GONE);
        } else {
            mRlResetPwd.setVisibility(View.VISIBLE);
            mBtnLoginout.setVisibility(View.VISIBLE);
            mLine.setVisibility(View.VISIBLE);
        }

        if (AndroidApplication.canUpdate) {
            mTvHasUpdate.setVisibility(View.VISIBLE);
        } else {
            mTvHasUpdate.setVisibility(View.INVISIBLE);
        }
        showCacheSize();
        mDialog = CustomProgressDialog.createDialog(this);
    }

    private void initListener() {
        BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                if (updateStatus == UpdateStatus.Yes) {
                    //版本有更新
                    AndroidApplication.canUpdate = true;
                    mTvHasUpdate.setVisibility(View.VISIBLE);
                } else if (updateStatus == UpdateStatus.No) {
                    //版本无更新
                    AndroidApplication.canUpdate = false;
                    mTvHasUpdate.setVisibility(View.INVISIBLE);
                    Toast.makeText(SettingActivity.this, R.string.toast_update_already_update, Toast.LENGTH_SHORT).show();
                } else if (updateStatus == UpdateStatus.TimeOut) {
                    //查询出错或查询超时
                    Toast.makeText(SettingActivity.this, R.string.toast_error_time_out, Toast.LENGTH_SHORT).show();
                } else if (updateStatus == UpdateStatus.IGNORED) {
                    //该版本已被忽略更新
                    LogHelper.d(TAG, "onUpdateReturned: 该版本已被忽略更新");
                    AndroidApplication.canUpdate = true;
                    mTvHasUpdate.setVisibility(View.VISIBLE);
                    BmobUpdateAgent.forceUpdate(SettingActivity.this);
                }
            }
        });
        RxView.clicks(mRlUpdate).throttleFirst(1, TimeUnit.SECONDS).subscribe(aVoid -> {
            LogHelper.d(TAG, "RxView.clicks");
            bmobUpdate();
        });
    }

    private void bmobUpdate() {
        if (NetworkUtils.isNetworkAvaialble(SettingActivity.this)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //拥有权限
                if (AndroidApplication.canUpdate) {
                    BmobUpdateAgent.forceUpdate(SettingActivity.this);
                } else {
                    BmobUpdateAgent.update(SettingActivity.this);
                }
            } else {
                //没有权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_STORAGE);
            }
        } else {
            ToastUtil.showToastBottom(this, R.string.toast_error_no_net,Toast.LENGTH_SHORT);
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
                    BmobUpdateAgent.forceUpdate(SettingActivity.this);
                } else {
                    BmobUpdateAgent.update(SettingActivity.this);
                }
            } else {
                showTipDialog();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 获取缓存的大小并显示
     */
    private void showCacheSize() {
        try {
            String cacheSize = DataCleanUtils.getCacheSize(this);
            LogHelper.d(TAG, "CacheSize: " + cacheSize);
            mTvCache.setText(cacheSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.setting_modify_pwd)
    void modifyPassWord() {
        MobclickAgent.onEvent(this, "jhyx_tap_setting_password");
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivityForResult(intent, RESET_PASSWORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESET_PASSWORD && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    @OnClick(R.id.setting_cache)
    void cleanCache() {
        MobclickAgent.onEvent(this, "jhyx_tap_setting_clean");
        showProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataCleanUtils.cleanExternalCache(SettingActivity.this);
                DataCleanUtils.cleanInternalCache(SettingActivity.this);
                Message msg = Message.obtain();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    msg.what = MSG_ERROR;
                    msg.obj = e.getMessage();
                    mHandler.sendMessage(msg);
                }
                msg.what = MSG_SUCCESS;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    @OnClick(R.id.setting_about_us)
    void introduce() {
        MobclickAgent.onEvent(this, "jhyx_tap_setting_about");
        Intent intent = new Intent(this, IntroductionActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.setting_evaluate)
    void gotoRate() {
        MobclickAgent.onEvent(this, "jhyx_tap_setting_evaluate");
        //这里开始执行一个应用市场跳转逻辑，默认this为Context上下文对象
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + getPackageName())); //跳转到应用市场
        //存在手机里没安装应用市场的情况，跳转会包异常，做一个接收判断
        if (intent.resolveActivity(getPackageManager()) != null) { //可以接收
            startActivity(intent);
        } else { //没有应用市场，我们通过浏览器跳转到指定网页
            //TODO
            intent.setData(Uri.parse("http://www.wandoujia.com/search?key=" + getPackageName()));
            //intent.setData(Uri.parse("http://sj.qq.com/myapp/detail.htm?apkName=" + getPackageName()));
            //不确定这个appid是否会改变
            //intent.setData(Uri.parse("http://app.qq.com/#id=detail&appid=1105301283"));
            startActivity(intent);
        }
    }

    @OnClick(R.id.setting_fadeback)
    void fadeback() {
        MobclickAgent.onEvent(this, "jhyx_tap_setting_feedback");
        Intent intent = new Intent(this, SettingFeedbackActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.setting_login_out)
    void loginout() {

        CustomDialog.Builder ibuilder = new CustomDialog.Builder(this);
        ibuilder.setTitle(R.string.prompt);
        ibuilder.setMessage(R.string.login_out);
        ibuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(Activity.RESULT_FIRST_USER);
                finish();
                dialog.dismiss();
            }
        });
        ibuilder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ibuilder.create().show();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showProgress() {
        if (!mDialog.isShowing())
            mDialog.show();
    }

    public void hideProgress() {
        if (mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgress();
    }
}
