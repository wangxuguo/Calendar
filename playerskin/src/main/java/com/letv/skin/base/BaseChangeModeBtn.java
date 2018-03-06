package com.letv.skin.base;

import com.lecloud.leutils.ReUtils;
import com.letv.controller.interfacev1.IPanoVideoChangeMode;
import com.letv.skin.BaseView;
import com.letv.skin.utils.UIPlayContext;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * 切换全景使用方式 touch 或者陀螺仪
 * 
 * @author heyuekuai
 */
public abstract class BaseChangeModeBtn extends BaseView implements OnClickListener {
    protected ImageView imageBtn;
    protected int mode;

    protected IPanoVideoChangeMode changeModelListener;

    public BaseChangeModeBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BaseChangeModeBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseChangeModeBtn(Context context) {
        super(context);
    }

    @Override
    protected void initView(Context context) {
        this.context = context;
        setVisibility(View.GONE);
        imageBtn = (ImageView) LayoutInflater.from(context).inflate(ReUtils.getLayoutId(context, getLayout()), null);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        this.addView(imageBtn, params);
        reset();
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mode == UIPlayContext.MODE_TOUCH) {
            mode = UIPlayContext.MODE_MOVE;
        } else {
            mode = UIPlayContext.MODE_TOUCH;
        }
        reset();
        if (changeModelListener != null) {
            changeModelListener.switchPanoVideoMode(mode);
        }
    }

    public void registerPanoVideoChange(IPanoVideoChangeMode changeModelListener) {
        this.changeModelListener = changeModelListener;
        if (changeModelListener != null) {
            setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void reset() {
        String btnResId = mode == UIPlayContext.MODE_TOUCH ? getTouchStyle() : getMoveStyle();
        imageBtn.setImageResource(ReUtils.getDrawableId(context, btnResId));
    }

    protected abstract String getMoveStyle();

    protected abstract String getTouchStyle();

    protected abstract String getLayout();

    @Override
    protected void initPlayer() {

    }

}
