package com.oceansky.calendar.example.customviews;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.ValueAnimator;
import com.oceansky.calendar.example.R;

public class RefreshListView extends ListView implements AbsListView.OnScrollListener {
    private static final int DONE               = 0;      //刷新完毕状态
    private static final int PULL_TO_REFRESH    = 1;   //下拉刷新状态
    private static final int RELEASE_TO_REFRESH = 2;    //释放状态
    private static final int REFRESHING         = 3;    //正在刷新状态
    private static final int RATIO              = 2;
    private RelativeLayout    mHeadView;    //下拉刷新头
    private int               headViewHeight; //头高度
    private float             startY;   //开始Y坐标
    private float             offsetY;  //Y轴偏移量
    private OnRefreshListener mOnRefreshListener;  //刷新接口
    private int               state;  //状态值
    private int               mFirstVisibleItem;  //第一项可见item索引
    private boolean           isRecord;   //是否记录
    private boolean           isEnd;  //是否结束
    private boolean           isRefreable;    //是否刷新

    private ImageView         mIvLoading;  //骑手图片组件
    private AnimationDrawable mLoadingAnimation;

    public RefreshListView(Context context) {
        super(context);
        init(context);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    /**
     * 回调接口，想实现下拉刷新的listview实现此接口
     *
     * @param onRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
        isRefreable = true;
    }

    /**
     * 刷新完毕，从主线程发送过来，并且改变headerView的状态和文字动画信息
     */
    public void setOnRefreshComplete() {
        //一定要将isEnd设置为true，以便于下次的下拉刷新
        // mHeadView.setPadding(0, -headViewHeight, 0, 0);
        changePaddingTopAnimation(mHeadView.getPaddingTop(), -headViewHeight);
        //        int paddingTop = mHeadView.getPaddingTop();
        isEnd = true;
        state = DONE;
        changeHeaderByState(state);
    }

    public void setOnRefreshBegin() {
        changePaddingTopAnimation(-headViewHeight, 0);
        isEnd = false;
        state = REFRESHING;
        changeHeaderByState(state);
    }

    private void init(Context context) {
        //关闭view的OverScroll
        setOverScrollMode(OVER_SCROLL_NEVER);
        setOnScrollListener(this);
        //加载头布局
        mHeadView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.refresh_header_view, this, false);

        //获取头布局图片组件
        mIvLoading = (ImageView) mHeadView.findViewById(R.id.headview);

        //获取动画
        mIvLoading.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) mIvLoading.getDrawable();

        addHeaderView(mHeadView);

        //测量头布局
        measureView(mHeadView);
        //给ListView添加头布局
        //设置头文件隐藏在ListView的第一项
        headViewHeight = mHeadView.getMeasuredHeight();
        mHeadView.setPadding(0, -headViewHeight, 0, 0);

        mLoadingAnimation.stop();
        state = DONE;
        isEnd = true;
        isRefreable = false;


    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isEnd) {//如果现在时结束的状态，即刷新完毕了，可以再次刷新了，在onRefreshComplete中设置
            if (isRefreable) {//如果现在是可刷新状态   在setOnMeiTuanListener中设置为true
                switch (ev.getAction()) {
                    //用户按下
                    case MotionEvent.ACTION_DOWN:
                        //如果当前是在listview顶部并且没有记录y坐标
                        if (mFirstVisibleItem == 0 && !isRecord) {
                            //将isRecord置为true，说明现在已记录y坐标
                            isRecord = true;
                            //将当前y坐标赋值给startY起始y坐标
                            startY = ev.getY();
                        }
                        break;
                    //用户滑动
                    case MotionEvent.ACTION_MOVE:
                        //再次得到y坐标，用来和startY相减来计算offsetY位移值
                        float tempY = ev.getY();
                        //再起判断一下是否为listview顶部并且没有记录y坐标
                        if (mFirstVisibleItem == 0 && !isRecord) {
                            isRecord = true;
                            startY = tempY;
                        }
                        //如果当前状态不是正在刷新的状态，并且已经记录了y坐标
                        if (state != REFRESHING && isRecord) {
                            //计算y的偏移量
                            offsetY = tempY - startY;

                            //如果当前的状态是放开刷新，并且已经记录y坐标
                            if (state == RELEASE_TO_REFRESH && isRecord) {

                                setSelection(0);
                                //如果当前滑动的距离小于headerView的总高度
                                if (-headViewHeight + offsetY / RATIO < 0) {
                                    //将状态置为下拉刷新状态
                                    state = PULL_TO_REFRESH;
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                    //如果当前y的位移值小于0，即为headerView隐藏了
                                } else if (offsetY <= 0) {
                                    //将状态变为done
                                    state = DONE;
                                    stopAnim();
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                }
                            }
                            //如果当前状态为下拉刷新并且已经记录y坐标
                            if (state == PULL_TO_REFRESH && isRecord) {
                                //setSelection(0);
                                //如果下拉距离大于等于headerView的总高度
                                if (-headViewHeight + offsetY / RATIO >= 0) {
                                    //将状态变为放开刷新
                                    state = RELEASE_TO_REFRESH;
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                    //如果当前y的位移值小于0，即为headerView隐藏了
                                } else if (offsetY <= 0) {
                                    //将状态变为done
                                    state = DONE;
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                }
                            }
                            //如果当前状态为done并且已经记录y坐标
                            if (state == DONE && isRecord) {
                                //如果位移值大于0
                                if (offsetY >= 0) {
                                    //将状态改为下拉刷新状态
                                    state = PULL_TO_REFRESH;
                                    changeHeaderByState(state);
                                }
                            }
                            //如果为下拉刷新状态
                            if (state == PULL_TO_REFRESH) {
                                //则改变headerView的padding来实现下拉的效果
                                mHeadView.setPadding(0, (int) (-headViewHeight + offsetY / RATIO), 0, 0);
                            }
                            //如果为放开刷新状态
                            if (state == RELEASE_TO_REFRESH) {
                                //改变headerView的padding值
                                mHeadView.setPadding(0, (int) (-headViewHeight + offsetY / RATIO), 0, 0);
                            }
                        }
                        break;
                    //当用户手指抬起时
                    case MotionEvent.ACTION_UP:
                        //如果当前状态为下拉刷新状态
                        if (state == PULL_TO_REFRESH) {
                            //平滑的隐藏headerView
                            changePaddingTopAnimation(mHeadView.getPaddingTop(), -headViewHeight);
                            //state = DONE;
                            //this.smoothScrollBy((int) (-headViewHeight + offsetY) + headViewHeight, 500);
                            //根据状态改变headerView
                            changeHeaderByState(state);
                        }
                        //如果当前状态为放开刷新
                        if (state == RELEASE_TO_REFRESH) {
                            //平滑的滑到正好显示headerView
                            changePaddingTopAnimation(mHeadView.getPaddingTop(), 0);
                            // this.smoothScrollBy((int) (-headViewHeight + offsetY), 500);
                            //将当前状态设置为正在刷新
                            state = REFRESHING;
                            changeHeaderByState(state);
                        }
                        //这一套手势执行完，一定别忘了将记录y坐标的isRecord改为false，以便于下一次手势的执行
                        isRecord = false;
                        break;
                }

            }
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 根据状态改变headerView的动画和文字显示
     *
     * @param state
     */
    private void changeHeaderByState(int state) {
        switch (state) {
            case DONE://如果的隐藏的状态
                stopAnim();
                //mHeadView.setPadding(0, -headViewHeight, 0, 0);
                break;
            case RELEASE_TO_REFRESH://当前状态为放开刷新
                startAnim();
                break;
            case PULL_TO_REFRESH://当前状态为下拉刷新
                break;
            case REFRESHING://当前状态为正在刷新
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onRefresh();
                }
                break;
            default:
                break;
        }
    }


    public void changePaddingTopAnimation(int start, int end) {
        // 通过ValueAnimator生成渐变值
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(400);
        animator.start();
        // 通过监听,拿到渐变值
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 拿到动画执行过程中的渐变值
                int paddintTop = (Integer) valueAnimator.getAnimatedValue();
                mHeadView.setPadding(0, paddintTop, 0, 0);
            }
        });
    }

    /**
     * 测量View
     *
     * @param child
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * 开启动画
     */
    public void startAnim() {
        mLoadingAnimation.start();
    }

    /**
     * 关闭动画
     */
    public void stopAnim() {
        mLoadingAnimation.stop();
    }
}
