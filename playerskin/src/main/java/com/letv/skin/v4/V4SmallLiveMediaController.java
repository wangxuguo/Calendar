package com.letv.skin.v4;

import com.letv.skin.base.BasePlayBtn;
import com.letv.skin.controller.BaseMediaController;

import android.content.Context;
import android.util.AttributeSet;

public class V4SmallLiveMediaController extends BaseMediaController {

    public V4SmallLiveMediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public V4SmallLiveMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public V4SmallLiveMediaController(Context context) {
        super(context);
    }

    @Override
    protected void onSetLayoutId() {
        layoutId = "letv_skin_v4_controller_live_layout";
        childId.add("vnew_play_btn");
        childId.add("vnew_chg_btn");
        childId.add("vnew_seekbar");
    }

    @Override
    protected void onInitView() {

    }

    @Override
    protected void initPlayer() {
        BasePlayBtn playBtn = (BasePlayBtn) childViews.get(0);
        playBtn.setPlayBtnType(BasePlayBtn.play_btn_type_vod);// 设置按钮模式
    }

}
