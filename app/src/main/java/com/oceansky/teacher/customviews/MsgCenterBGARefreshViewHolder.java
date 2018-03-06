package com.oceansky.teacher.customviews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.AnimRes;
import android.support.annotation.AnyRes;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.oceansky.teacher.R;

import cn.bingoogolapple.refreshlayout.BGAMeiTuanRefreshView;
import cn.bingoogolapple.refreshlayout.BGARefreshViewHolder;

/**
 * User: 王旭国
 * Date: 16/8/23 17:46
 * Email:wangxuguo@jhyx.com.cn
 */
public class MsgCenterBGARefreshViewHolder extends BGARefreshViewHolder {
    private BGAMeiTuanRefreshView mMeiTuanRefreshView;
    private int mPullDownImageResId = -1;
    private int mChangeToReleaseRefreshAnimResId = -1;
    private int mRefreshingAnimResId = -1;
    private int mLoadMoreBackgroundColorRes = -1;
    private int mLoadMoreBackgroundDrawableRes = -1;

    /**
     * @param context
     * @param isLoadingMoreEnabled 上拉加载更多是否可用
     */
    public MsgCenterBGARefreshViewHolder(Context context, boolean isLoadingMoreEnabled) {
        super(context, isLoadingMoreEnabled);
    }

    /**
     * 设置下拉过程中的图片资源
     *
     * @param resId
     */
    public void setPullDownImageResource(@DrawableRes int resId) {
        mPullDownImageResId = resId;
    }

    /**
     * 设置进入释放刷新状态时的动画资源
     *
     * @param resId
     */
    public void setChangeToReleaseRefreshAnimResId(@AnimRes int resId) {
        mChangeToReleaseRefreshAnimResId = resId;
    }

    /**
     * 设置正在刷新时的动画资源
     *
     * @param resId
     */
    public void setRefreshingAnimResId(@AnyRes int resId) {
        mRefreshingAnimResId = resId;
    }

    @Override
    public View getRefreshHeaderView() {
        if (mRefreshHeaderView == null) {
            mRefreshHeaderView = View.inflate(mContext, cn.bingoogolapple.refreshlayout.R.layout.view_refresh_header_meituan, null);
            mRefreshHeaderView.setBackgroundColor(Color.TRANSPARENT);
            if (mRefreshViewBackgroundColorRes != -1) {
                mRefreshHeaderView.setBackgroundResource(mRefreshViewBackgroundColorRes);
            }
            if (mRefreshViewBackgroundDrawableRes != -1) {
                mRefreshHeaderView.setBackgroundResource(mRefreshViewBackgroundDrawableRes);
            }

            mMeiTuanRefreshView = (BGAMeiTuanRefreshView) mRefreshHeaderView.findViewById(cn.bingoogolapple.refreshlayout.R.id.meiTuanView);
            if (mPullDownImageResId != -1) {
                mMeiTuanRefreshView.setPullDownImageResource(mPullDownImageResId);
            } else {
                throw new RuntimeException("请调用" + MsgCenterBGARefreshViewHolder.class.getSimpleName() + "的setPullDownImageResource方法设置下拉过程中的图片资源");
            }
            if (mChangeToReleaseRefreshAnimResId != -1) {
                mMeiTuanRefreshView.setChangeToReleaseRefreshAnimResId(mChangeToReleaseRefreshAnimResId);
            } else {
                throw new RuntimeException("请调用" + MsgCenterBGARefreshViewHolder.class.getSimpleName() + "的setChangeToReleaseRefreshAnimResId方法设置进入释放刷新状态时的动画资源");
            }
            if (mRefreshingAnimResId != -1) {
                mMeiTuanRefreshView.setRefreshingAnimResId(mRefreshingAnimResId);
            } else {
                throw new RuntimeException("请调用" + MsgCenterBGARefreshViewHolder.class.getSimpleName() + "的setRefreshingAnimResId方法设置正在刷新时的动画资源");
            }
        }
        return mRefreshHeaderView;
    }

    @Override
    public void handleScale(float scale, int moveYDistance) {
        if (scale <= 1.0f) {
            //mMeiTuanRefreshView.handleScale(scale);
        }
    }
    public View getLoadMoreFooterView() {
        if (mLoadMoreBackgroundColorRes == -1) {
            mLoadMoreBackgroundColorRes = R.color.activity_bg_gray;
        }
        if (mLoadMoreBackgroundDrawableRes == -1) {
//            mLoadMoreBackgroundDrawableRes = R.drawable.loading_more;
        }
        if (mLoadMoreFooterView == null) {
            mLoadMoreFooterView = View.inflate(mContext, cn.bingoogolapple.refreshlayout.R.layout.view_normal_refresh_footer, null);
            mLoadMoreFooterView.setBackgroundColor(Color.TRANSPARENT);
            if (mLoadMoreBackgroundColorRes != -1) {
                mLoadMoreFooterView.setBackgroundResource(mLoadMoreBackgroundColorRes);
            }
            if (mLoadMoreBackgroundDrawableRes != -1) {
                mLoadMoreFooterView.setBackgroundResource(mLoadMoreBackgroundDrawableRes);
            }
            mFooterStatusTv = (TextView) mLoadMoreFooterView.findViewById(cn.bingoogolapple.refreshlayout.R.id.tv_normal_refresh_footer_status);
            mFooterChrysanthemumIv = (ImageView) mLoadMoreFooterView.findViewById(cn.bingoogolapple.refreshlayout.R.id.iv_normal_refresh_footer_chrysanthemum);
            mFooterChrysanthemumAd = (AnimationDrawable) mFooterChrysanthemumIv.getDrawable();
            mFooterStatusTv.setText(mLodingMoreText);
        }
        return mLoadMoreFooterView;
    }
    @Override
    public void changeToIdle() {
        mMeiTuanRefreshView.changeToIdle();
    }

    @Override
    public void changeToPullDown() {
        mMeiTuanRefreshView.changeToPullDown();
    }

    @Override
    public void changeToReleaseRefresh() {
        mMeiTuanRefreshView.changeToReleaseRefresh();
    }

    @Override
    public void changeToRefreshing() {
        mMeiTuanRefreshView.changeToRefreshing();
    }

    @Override
    public void onEndRefreshing() {
        mMeiTuanRefreshView.onEndRefreshing();
    }

}
