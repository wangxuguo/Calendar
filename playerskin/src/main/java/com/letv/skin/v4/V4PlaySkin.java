package com.letv.skin.v4;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.letv.controller.interfacev1.IPanoVideoChangeMode;
import com.letv.controller.interfacev1.ISplayerController;
import com.letv.skin.BaseSkin;
import com.letv.skin.BaseView;
import com.letv.skin.controller.BaseMediaController;
import com.letv.skin.interfacev1.IActionCallback;
import com.letv.skin.loading.BaseLoadingView;
import com.letv.skin.loading.DefaultLoadingView;
import com.letv.skin.loading.MaterialLoadingView;
import com.letv.skin.popupwindow.BackLivePopWindow;
import com.letv.skin.utils.GestureControl;
import com.letv.skin.utils.ScreenUtils;
import com.letv.skin.utils.UIPlayContext;

import java.util.Observable;
import java.util.Observer;

public class V4PlaySkin extends BaseSkin {
    public static final String SKIN_BUILD_TYPE = "skinBuildType";
    public static final int SKIN_TYPE_NO = -1;
    public static final int SKIN_TYPE_A = 1;
    public static final int SKIN_TYPE_B = 2;
    public static final int SKIN_TYPE_C = 3;
    private static final String TAG = "V4PlaySkin";
    private int defaultWidth = 4;
    private int defaultHeight = 3;
    private int portraitHeight = 0;
    private int portraitWidth = 0;
    private int landscapeHeight = 0;
    private int landscapeWidth = 0;

    private OnClickListener downloadClickListener;
    private IPanoVideoChangeMode panoVideoChangeModeCallback;
    private IActionCallback mIActionCallback;

    public V4PlaySkin(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public V4PlaySkin(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public V4PlaySkin(Context context) {
        super(context);
    }

    @Override
    protected void onBuild(UIPlayContext uiContext) {
        // initLayoutParams();
    }

	public IActionCallback getIActionCallback() {
		return mIActionCallback;
	}
	public void setIActionCallback(IActionCallback mIActionCallback) {
		this.mIActionCallback = mIActionCallback;
	}

    /**
     * 初始化默认（4：3）播放器比例
     */
    private void initLayoutParams() {
        changeLayoutParams(defaultWidth, defaultHeight);
    }

    /**
     * 根据播放视频的宽和高设置画面布局的宽和高
     * 
     * @param width
     * @param height
     */
    public void changeLayoutParams(int width, int height) {
        int screenWidth = ScreenUtils.getWight(context);
        int screenHeight = ScreenUtils.getHeight(context);
        if (screenHeight > screenWidth) {// 初始横屏状态
            screenWidth = screenHeight;
            screenHeight = ScreenUtils.getWight(context);
        }
        // 记录竖屏宽高
        portraitWidth = screenHeight;
        portraitHeight = screenHeight * height / width;
        // 记录横屏宽高
        landscapeWidth = screenWidth;
        landscapeHeight = screenHeight;
    }

    private void changeLayoutParams() {
        if (uiPlayContext.getScreenResolution(context) == ISplayerController.SCREEN_ORIENTATION_LANDSCAPE) {
            this.getLayoutParams().width = landscapeWidth;
            this.getLayoutParams().height = landscapeHeight;
        } else {
            this.getLayoutParams().width = portraitWidth;
            this.getLayoutParams().height = portraitHeight;
        }
        requestLayout();
    }

    public void setOnDownloadClickListener(OnClickListener l) {
        this.downloadClickListener = l;
    }

    @Override
    protected void attachComponentView() {
        changeLayoutParams();
        uiPlayContext.setLockFlag(false);
        switch (uiPlayContext.getSkinBuildType()) {
        case SKIN_TYPE_A:
            if (uiPlayContext.getScreenResolution(context) == ISplayerController.SCREEN_ORIENTATION_LANDSCAPE) {
                // largeController = new V3VodLargeMediaController(context);

                final V4LargeMediaController largeController = new V4LargeMediaController(context);
                largeController.registerPanoVideoChange(panoVideoChangeModeCallback);
                attachBottomView(largeController);
                attachGestureController(this, false);

                if (downloadClickListener != null && uiPlayContext.isDownloadable()) {
                    BaseView rightMediaView = new V4DownloadBtn(context);
                    ((V4DownloadBtn) rightMediaView).setOnDownloadClickListener(downloadClickListener);
                    attachRightMediaView(rightMediaView);
                }

                V4TopTitleView titleView = new V4TopTitleView(context);
                // titleView = new TopTitleMultLiveView(context);
                attachTopFloatMediaView(titleView);
                mGestureControl.getObserver().addObserver(new Observer() {
        			
        			@Override
        			public void update(Observable observable, Object data) {
        				Bundle bundle = (Bundle) data;
        				int state = bundle.getInt("state");
        				switch (state) {
        				case GestureControl.GESTURE_CONTROL_DOWN:
        					break;
        				case GestureControl.GESTURE_CONTROL_SEEK:
        					if (largeController != null && largeController.getSeekbar() != null) {
        						largeController.getSeekbar().startTrackingTouch();
        						largeController.getSeekbar().setProgress(bundle.getInt(GestureControl.GESTURE_CONTROL_SEEK_GAP));
        						if (mGestureControl.mSeekToPopWindow != null) {
        							mGestureControl.mSeekToPopWindow.setProgress(largeController.getSeekbar().getPlayerProgress(),largeController.getSeekbar().getPlayerDuration());
								}
							}
        					break;
        				case GestureControl.GESTURE_CONTROL_UP:
        					if (largeController != null && largeController.getSeekbar() != null) {
        						largeController.getSeekbar().stopTrackingTouch();
							}
        					break;
        				default:
        					break;
        				}
        			}
        		});
                
            } else {
                // smallController = new V3VodSmallMediaController(context);
                BaseMediaController smallController = new V4SmallMediaController(context);
                attachBottomView(smallController);
                attachGestureController(this, true);
                mGestureControl.setSeekable(true);
            }
            
            break;
        case SKIN_TYPE_B:
            if (uiPlayContext.getScreenResolution(context) == ISplayerController.SCREEN_ORIENTATION_LANDSCAPE) {
                // largeController = new V3LiveLargeMediaController(context);
                V4LargeLiveMediaController largeController = new V4LargeLiveMediaController(context);
                largeController.registerPanoVideoChange(panoVideoChangeModeCallback);
                attachBottomView(largeController);
                attachGestureController(this, false);

                V4TopTitleView titleView = new V4TopTitleView(context);
                attachTopFloatMediaView(titleView);

            } else {
                // BaseMediaController smallController = new
                // V3LiveSmallMediaController(context);
                BaseMediaController smallController = new V4SmallLiveMediaController(context);
                attachBottomView(smallController);
                attachGestureController(this, true);
                mGestureControl.setSeekable(false);
            }
            break;
        case SKIN_TYPE_C:
        	BackLivePopWindow.getInstance(context).dismiss();
            if (uiPlayContext.getScreenResolution(context) == ISplayerController.SCREEN_ORIENTATION_LANDSCAPE) {
                final V4LargeLiveMediaController largeController = new V4LargeLiveMediaController(context);
                attachBottomView(largeController);
                attachGestureController(this, false);
                V4MultLiveRightView v3MultLiveLargeController = new V4MultLiveRightView(context);
//                v3MultLiveLargeController.setMark(true);
                v3MultLiveLargeController.setIActionCallback(mIActionCallback);

                // v3MultLiveLargeController.set
                attachRightMediaView(v3MultLiveLargeController);
                v3MultLiveLargeController.setActionInfoDone();

                V4TopTitleMultLiveView titleView = new V4TopTitleMultLiveView(context);
                attachTopFloatMediaView(titleView);
                mGestureControl.setSeekable(true);
                mGestureControl.getObserver().addObserver(new Observer() {
        			
        			@Override
        			public void update(Observable observable, Object data) {
        				Bundle bundle = (Bundle) data;
        				int state = bundle.getInt("state");
        				switch (state) {
        				case GestureControl.GESTURE_CONTROL_SEEK:
        					if (largeController != null && largeController.getSeekbar() != null) {
        						if (largeController.getSeekbar().getVisibility() != View.VISIBLE) {
        							mGestureControl.setSeekable(false);
        							if (mGestureControl.mSeekToPopWindow != null) {
        								mGestureControl.mSeekToPopWindow.dismiss();
									}
									return;
								}
        						mGestureControl.setSeekable(true);
        						largeController.getSeekbar().startTrackingTouch();
        						largeController.getSeekbar().setProgress(bundle.getInt(GestureControl.GESTURE_CONTROL_SEEK_GAP));
        						if (mGestureControl.mSeekToPopWindow != null) {
        							mGestureControl.mSeekToPopWindow.setProgress(largeController.getSeekbar().getSeekToTime());
								}
							}
        					break;
        				case GestureControl.GESTURE_CONTROL_UP:
        					if (largeController != null && largeController.getSeekbar() != null) {
        						largeController.getSeekbar().stopTrackingTouch();
							}
        					break;
        				default:
        					break;
        				}
        			}
        		});

            } else {
                BaseMediaController smallController = new V4SmallLiveMediaController(context);
                attachBottomView(smallController);
                attachGestureController(this, true);
                mGestureControl.setSeekable(false);
            }

            break;

        default:
            break;
        }
    }

	@Override
    protected void attachCommonView() {

        /**
         * 提示view
         */
        V4NoticeView noticeView = new V4NoticeView(context);
        noticeView.setMark(true);
        attachCenterView(noticeView);
        noticeView.setVisibility(View.GONE);

        /**
         * loading
         */
        BaseLoadingView loadingView = new MaterialLoadingView(context);
        loadingView.setMark(true);
        attachCenterView(loadingView);

        /**
         * 水印已经添加到内部，不需要在皮肤层添加
         */
//        if (uiPlayContext.getSkinBuildType() == SKIN_TYPE_C) {
//            V4WaterMarkView v3WaterMarkView = new V4WaterMarkView(context);
//            v3WaterMarkView.setMark(true);
//            attachCenterView(v3WaterMarkView);
//        }
        /**
         * 广告倒计时
         */
//        NormalTimerView timerView = new NormalTimerView(context);
//        timerView.setMark(true);
//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.ALIGN_PARENT_BOTTOM);
//        attachAnyPositionView(timerView, lp);


    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	if (uiPlayContext.getSkinBuildType() == SKIN_TYPE_C) {
    		BackLivePopWindow.getInstance(context).dismiss();
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    public void registerPanoVideoChange(IPanoVideoChangeMode panoVideoChangeModeCallback) {
        this.panoVideoChangeModeCallback = panoVideoChangeModeCallback;
    }

}
