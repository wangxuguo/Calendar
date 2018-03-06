package com.oceansky.teacher.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.jaeger.library.StatusBarUtil;
import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.customviews.CustomDialog;
import com.oceansky.teacher.customviews.TitleBar;
import com.oceansky.teacher.listeners.BackStackListener;
import com.oceansky.teacher.utils.LogHelper;
import com.umeng.analytics.MobclickAgent;

public abstract class BaseActivity extends FragmentActivity implements BackStackListener {

    private final static String TAG = BaseActivity.class.getSimpleName();

    protected TitleBar mTitleBar;
    protected int mAlpha = 0;
    protected InputMethodManager mImm;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBar();
        setTitleBar();
        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.title_bar_bg), mAlpha);
    }

    protected void setTitleBar() {
        mTitleBar = new TitleBar(this, R.id.title_bar);
    }

    @Override
    public void onBackStack() {
        finish();
    }

    /**
     * 点击空白处隐藏软键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点）
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                hideSoftInput(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 多种隐藏软件盘方法的其中一种
     *
     * @param token
     */
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * token失效弹框
     */
    protected void showTokenInvalidDialog() {
        String simpleName = this.getClass().getSimpleName();
        LogHelper.d(TAG, "simpleName: " + simpleName);
        CustomDialog.Builder ibuilder = new CustomDialog.Builder(this);
        ibuilder.setTitle(R.string.prompt);
        ibuilder.setMessage(getString(R.string.btn_login_expiry));
        ibuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                intent.putExtra(Constants.CLASS_NAME, simpleName);
                startActivity(intent);
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
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        String SimpleName = this.getClass().getSimpleName();
        MobclickAgent.onPageStart(SimpleName);
    }

    @Override
    protected void onPause() {
        super.onPause();
        String SimpleName = this.getClass().getSimpleName();
        MobclickAgent.onPageEnd(SimpleName);
        MobclickAgent.onPause(this);
    }
}
