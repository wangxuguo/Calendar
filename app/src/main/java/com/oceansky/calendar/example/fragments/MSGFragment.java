package com.oceansky.example.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.reflect.TypeToken;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.oceansky.example.R;
import com.oceansky.example.adapter.MessageExpandAdapter;
import com.oceansky.example.constant.Constants;
import com.oceansky.example.customviews.MsgCenterBGARefreshViewHolder;
import com.oceansky.example.network.http.ApiException;
import com.oceansky.example.network.http.HttpManager;
import com.oceansky.example.network.response.MessageEntity;
import com.oceansky.example.network.response.SimpleResponse;
import com.oceansky.example.network.subscribers.BaseSubscriber;
import com.oceansky.example.network.subscribers.LoadingSubscriber;
import com.oceansky.example.utils.DisplayUtils;
import com.oceansky.example.utils.LogHelper;
import com.oceansky.example.utils.NetworkUtils;
import com.oceansky.example.utils.SecurePreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import rx.Subscription;

import static android.view.View.inflate;

/**
 * User: 王旭国
 * Date: 16/8/18 15:40
 * Email:wangxuguo@jhyx.com.cn
 */
public class MSGFragment extends BaseLazyFragment implements AdapterView.OnItemClickListener, BGARefreshLayout.BGARefreshLayoutDelegate {
    private static final String TAG = MSGFragment.class.getSimpleName();
    private Activity                 mContext;
    //区分当前是公共消息还是个人消息 0  公共  1  私有   2 者都有
    private String                   msgType;
    private BGARefreshLayout         mRefreshLayout;
    private ListView                 mListView;
    private RelativeLayout           errorLayout;
    private TextView                 descText;
    //    private TextView                 subDescText;
    private Button                   errorBtn;
    private ImageView                mErrorImage;
    private FrameLayout              mLoadingLayout;
    private ImageView                loadingImg;
    private AnimationDrawable        mLoadingAnimation;
    private ArrayList<MessageEntity> msgList;
    //    private MessageAdapter           messageAdapter;
    private MessageExpandAdapter     messageAdapter;
    private View                     mEmptyPage;
    private View                     mFooterView;
    private ArrayList<MessageEntity> mMsgListCache;
    private int                                      size                 = 10;
    private int                                      start_id             = 0;
    private int                                      after_id             = 0;
    private boolean                                  isFirstRefresh       = true;
    private boolean                                  isFirstRedPointState = true;
    private Handler                                  handler              = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private ExpandableTextView.OnItemClickedListener onItemClicked        = new ExpandableTextView.OnItemClickedListener() {
        @Override
        public void onItemClicked(int position) {
            LogHelper.d(TAG, "onItemClicked : " + position);
            onItemClick(null, null, position, 0);
        }
    };
    private boolean               mIsFirstRefresh;
    private boolean               isShowedFooterView;
    private boolean               isRefresh;
    private MessageBeanSubscriber mMessageBeanSubscriber;
    private BeseBeanSubscriber    mBeseBeanSubscriber;
    private Subscription          mMessageSubscription;
    private Subscription          mSetMessageItemReadedSubscription;
    private boolean               misEmpty;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_msg);
        super.onCreateViewLazy(savedInstanceState);
        mContext = getActivity();
        msgType = getArguments().getString(Constants.MSG_TYPE);
        initView();
        initData();
    }

    @Override
    void onErrorLayoutClick() {
    }

    private void initView() {
        mRefreshLayout = (BGARefreshLayout) findViewById(R.id.rl_listview_refresh);
        mListView = (ListView) findViewById(R.id.listview);
        errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        descText = (TextView) findViewById(R.id.error_desc);
        //        subDescText = (TextView) findViewById(R.id.sub_desc);
        mErrorImage = (ImageView) findViewById(R.id.error_img);
        mLoadingLayout = (FrameLayout) findViewById(R.id.loading_layout);
        loadingImg = (ImageView) findViewById(R.id.loading);
        setNormalView();
        //加载动画
        loadingImg.setImageResource(R.drawable.loading_animation);
        mLoadingAnimation = (AnimationDrawable) loadingImg.getDrawable();
        mRefreshLayout.setDelegate(this);
        MsgCenterBGARefreshViewHolder msgRefreshViewHolder = new MsgCenterBGARefreshViewHolder(getActivity(), true);
        msgRefreshViewHolder.setPullDownImageResource(R.drawable.loading_animation);
        msgRefreshViewHolder.setRefreshingAnimResId(R.drawable.loading_animation);
        msgRefreshViewHolder.setChangeToReleaseRefreshAnimResId(R.anim.bga_refresh_mt_refreshing);
        msgRefreshViewHolder.setRefreshingAnimResId(R.anim.bga_refresh_mt_refreshing);
        msgRefreshViewHolder.setLoadingMoreText("正在加载更多的数据..."); //正在加载更多的数据...  加载更多
        msgRefreshViewHolder.setLoadMoreBackgroundDrawableRes(R.drawable.loading_animation);
        mRefreshLayout.setRefreshViewHolder(msgRefreshViewHolder);
        mRefreshLayout.setIsShowLoadingMoreView(true);

        msgList = new ArrayList<>();
        messageAdapter = new MessageExpandAdapter(mContext, mListView, msgList);
        messageAdapter.setOnItemClicked(onItemClicked);
        mListView.setAdapter(messageAdapter);
        messageAdapter.setMutilLinesMSGSetReadedListener(new MessageExpandAdapter.MutilLinesMSGSetReadedListener() {
            @Override
            public void setMSGReaded(int itemid, int position) {
                onItemClick(null, null, position, 0);
            }
        });
        mListView.setHeaderDividersEnabled(false);
        mListView.setFooterDividersEnabled(true);
        mListView.setOnItemClickListener(this);
        initEmptyPage();

        TextView textView = (TextView) mEmptyPage.findViewById(R.id.empty_tv_msg);
        textView.setText("暂无消息");
        textView.setTextSize(13);
        textView.setTextColor(getResources().getColor(R.color.tab_msg_textcolor_unselected));

       /* Display defaultDisplay = mContext.getWindowManager().getDefaultDisplay();
        int screenHeight = defaultDisplay.getHeight();
        //获取状态栏高度
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        int actionBarHeight = (int) getResources().getDimension(R.dimen.action_bar_height);
        int tabHeight = (int) getResources().getDimension(R.dimen.height_order_tab);
        int paddingBottom = (int) getResources().getDimension(R.dimen.padding_bottom_order_listview);
        int paddingTop = (int) getResources().getDimension(R.dimen.padding_top_order_listview);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                screenHeight - actionBarHeight - statusBarHeight - tabHeight - paddingBottom - paddingTop);
        mEmptyPage.setLayoutParams(params);*/
        mFooterView = inflate(mContext, R.layout.layout_list_footer_nomore_data, null);
        AbsListView.LayoutParams footerParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayUtils.dip2px(mContext, 30));
        //        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(defaultDisplay.getWidth(), DisplayUtils.dip2px(mContext, 30));
        LogHelper.d(TAG, "footerParams.height: " + footerParams.height);
        mFooterView.setLayoutParams(footerParams);
    }

    private void initEmptyPage() {
        //添加空页面
        if (msgType.equals(Constants.MSG_COMON)) {
            mEmptyPage = LayoutInflater.from(mContext).inflate(R.layout.layout_no_common_msg, mRefreshLayout, false);
        } else {
            mEmptyPage = LayoutInflater.from(mContext).inflate(R.layout.layout_no_person_msg, mRefreshLayout, false);
        }
        mRefreshLayout.addView(mEmptyPage);
        misEmpty = true;
    }

    private void initData() {
        errorLayout.setVisibility(View.GONE);
        mLoadingLayout.setVisibility(View.VISIBLE);
        mRefreshLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mLoadingAnimation.start();
        // 在这里加载最新数据
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            // 如果网络可用，则加载网络数据
            //            mTime.start();
            mListView.setVisibility(View.GONE);
            LogHelper.d(TAG, "开始数据请求： " + "start_id: " + start_id + ",after_id: " + after_id + ",size: " + size + ",msgType: " + msgType);
            start_id = 0;
            int type = 1;
            if (msgType.equals(Constants.MSG_COMON)) {   //0:仅个人消息，1:仅公共消息，2:个人消息和公共消息
                type = 1;
            } else {
                type = 0;
            }
            final String token =
                    "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
            LogHelper.d(TAG, "getListData: " + token);
            mMessageBeanSubscriber = new MessageBeanSubscriber(mContext, true);
            mMessageSubscription = HttpManager.getMessageList(token, type, size, start_id, after_id).subscribe(mMessageBeanSubscriber);
        } else {
            // 网络不可用，结束刷新
            Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            mLoadingLayout.setVisibility(View.GONE);
            mLoadingAnimation.stop();

            mRefreshLayout.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.VISIBLE);
            try {
                if (Reservoir.contains(getConstant())) {
                    mMsgListCache = Reservoir.get(getConstant(), new TypeToken<ArrayList<MessageEntity>>() {
                    }.getType());
                }
            } catch (Exception e) {
                LogHelper.e(TAG, "initData Exception " + e.toString());
                e.printStackTrace();
            }
            LogHelper.d(TAG, "TListCache " + mMsgListCache);
            refreshListView(true, mMsgListCache);
        }
    }

    /**
     * 刷新数据
     */
    private void refreshData() {
        // 在这里加载最新数据
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            // 如果网络可用，则加载网络数据
            //            mTime.start();
            start_id = 0;
            after_id = 0;
            int type = 1;
            if (msgType.equals(Constants.MSG_COMON)) {   //0:仅个人消息，1:仅公共消息，2:个人消息和公共消息
                type = 1;
            } else {
                type = 0;
            }
            final String token =
                    "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
            LogHelper.d(TAG, "getListData: " + token);
            mMessageBeanSubscriber = new MessageBeanSubscriber(mContext, true);
            mMessageSubscription = HttpManager.getMessageList(token, type, size, start_id, after_id).subscribe(mMessageBeanSubscriber);
        } else {
            // 网络不可用，结束下拉刷新
            Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            mRefreshLayout.endRefreshing();
        }
    }

    /**
     * 加载更多数据
     */
    private void loadmoreData() {
        // 在这里加载最新数据
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            // 如果网络可用，则加载网络数据
            mMessageBeanSubscriber = new MessageBeanSubscriber(mContext, false);
            if (msgList != null && msgList.size() > 0) {
                start_id = msgList.get(msgList.size() - 1).getId();
            } else {
                start_id = 0;
            }
            int type = 1;
            if (msgType.equals(Constants.MSG_COMON)) {   //0:仅个人消息，1:仅公共消息，2:个人消息和公共消息
                type = 1;
            } else {
                type = 0;
            }
            final String token =
                    "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
            LogHelper.d(TAG, "getListData: " + token);
            mMessageSubscription = HttpManager.getMessageList(token, type, size, start_id, after_id).subscribe(mMessageBeanSubscriber);
        } else {
            // 网络不可用，结束
            Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            mRefreshLayout.endLoadingMore();
            handler.postDelayed(mDelayHiddenLoadingMoreViewTask, 50);
            LogHelper.d(TAG, "mRefreshLayout.endLoadingMore");
        }
    }

    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        LogHelper.d(TAG, "onBGARefreshLayoutBeginRefreshing ");
        refreshData();
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        LogHelper.d(TAG, "onBGARefreshLayoutBeginLoadingMore ");
        if (isShowedFooterView || msgList.size() < 1) {
            refreshLayout.endLoadingMore();
            handler.postDelayed(mDelayHiddenLoadingMoreViewTask, 50);
            LogHelper.d(TAG, "mRefreshLayout.endLoadingMore");
            return false;
        }
        loadmoreData();
        return true;
    }

    @Override
    protected void showTokenInvalidDialog() {
//        Intent intent = new Intent(mContext, TokenInvalidDialogActivity.class);
//        startActivity(intent);
    }

    /**
     * 如果当前列表中没有未读消息，则发广播
     */
    private void checkifHaveUnReadMsg() {
        LogHelper.d(TAG, "checkifHaveUnReadMsg");
        for (int i = 0; i < msgList.size(); i++) {
            MessageEntity data = msgList.get(i);
            if (data.getIs_read() == 0) { // 0:未读 | 1:已读
                // 如果有未读的,给MsgCenter发送消息显示有红点
                postHaveUnReadMsg(true);
                return;
            }
        }
        postHaveUnReadMsg(false);
    }

    private void postHaveUnReadMsg(boolean b) {
        if (isFirstRedPointState) {
            isFirstRedPointState = false;
            return;
        }
        if (msgType.equals("msg_common")) {   //0:仅个人消息，1:仅公共消息，2:个人消息和公共消息
            Intent intent = new Intent(Constants.ACTION_PUBMSG_COUNT);
            intent.putExtra(Constants.IS_HAVE_UNREADMSG, b);
            mContext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent(Constants.ACTION_PRIMSG_COUNT);
            intent.putExtra(Constants.IS_HAVE_UNREADMSG, b);
            mContext.sendBroadcast(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (msgList.size() < 1 || msgList.size() == position) {
            return;
        }
        MessageEntity item = msgList.get(position);
        if (item.getIs_read() == 1) {  //已读
            return;
        }
        LogHelper.i(TAG, "onItemClick  position: " + (position) + " item.getId(): " + item.getId());
        if (NetworkUtils.isNetworkAvaialble(mContext)) {
            setMessageItemReaded(item.getId(), position);
        } else {
            Toast.makeText(mContext, R.string.toast_error_no_net, Toast.LENGTH_SHORT).show();
        }
    }

    protected void setMessageItemReaded(int id, int position) {
        final String token =
                "Bearer " + SecurePreferences.getInstance(mContext, false).getString(Constants.KEY_ACCESS_TOKEN);
        LogHelper.d(TAG, "setMessageItemReaded: " + token);
        mBeseBeanSubscriber = new BeseBeanSubscriber(mContext, id, position);
        mSetMessageItemReadedSubscription = HttpManager.setMessageItemReaded(token, id).subscribe(mBeseBeanSubscriber);
    }

    private class MessageBeanSubscriber extends LoadingSubscriber<ArrayList<MessageEntity>> {

        private ArrayList<MessageEntity> mMessageList;
        private boolean                  isRefresh;

        public MessageBeanSubscriber(Context context, boolean isRefresh) {
            super(context);
            this.isRefresh = isRefresh;
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
            mLoadingAnimation.stop();
            mLoadingLayout.setVisibility(View.GONE);
            mRefreshLayout.setVisibility(View.VISIBLE);
            refreshListView(isRefresh, mMessageList);
            mRefreshLayout.endLoadingMore();
            mRefreshLayout.endRefreshing();
            checkifHaveUnReadMsg();
        }

        @Override
        protected void onTimeout() {
            super.onTimeout();
            if (isRefresh) {
                LogHelper.d(TAG, "请求超时");
                if (isFirstRefresh) {
                    mLoadingLayout.setVisibility(View.GONE);
                    mLoadingAnimation.stop();
                    mRefreshLayout.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.VISIBLE);
                    setLoadFailView();
                    errorLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setNormalView();
                            mListView.setVisibility(View.GONE);
                            initData();
                        }
                    });
                    return;
                }
                Toast.makeText(getContext(), "网络出错，请重试", Toast.LENGTH_SHORT).show();
                errorLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refreshData();
                    }
                });
                mRefreshLayout.endRefreshing();
                if (mIsFirstRefresh && mMsgListCache != null) {
                    refreshListView(true, mMsgListCache);
                } else {
                    // TODO: 16/11/22 ??????
                    mListView.removeHeaderView(mEmptyPage);
                    mListView.addHeaderView(mEmptyPage);
                }
            } else {
                mRefreshLayout.endLoadingMore();
                LogHelper.d(TAG, "TimeLoadMoreCount: " + "  onFinish");
                mLoadingLayout.setVisibility(View.GONE);
                mLoadingAnimation.stop();
                mRefreshLayout.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                errorLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setNormalView();
                        mListView.setVisibility(View.GONE);
                        initData();
                    }
                });
                setLoadFailView();
                LogHelper.d(TAG, "mRefreshLayout.endLoadingMore");
                Toast.makeText(getContext(), "网络出错，请重试", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void handleError(Throwable e) {
            switch (e.getMessage()) {
                case "4013":
                    mRefreshLayout.endLoadingMore();
                    mRefreshLayout.endRefreshing();
                    setLoadFailView();
                    getErrorLayout().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initData();
                        }
                    });
                    showTokenInvalidDialog();
                    break;
                case "4003":
                case "5000":
                    setLoadFailView();
                    break;
                case "4004":
                    if (msgList.size() < 1) {
                        try {
                            if (Reservoir.contains(getConstant())) {
                                mMsgListCache = Reservoir.get(getConstant(), new TypeToken<ArrayList<MessageEntity>>() {
                                }.getType());
                            }
                        } catch (Exception ex) {
                            LogHelper.e(TAG, "initData Exception " + ex.toString());
                            ex.printStackTrace();
                        }
                        LogHelper.d(TAG, "TListCache " + mMsgListCache);
                        refreshListView(true, mMsgListCache);
                    }
                    break;
                default:
                    if (mIsFirstRefresh && mMsgListCache != null) {
                        refreshListView(true, mMsgListCache);
                    }
                    mRefreshLayout.endLoadingMore();
                    mRefreshLayout.endRefreshing();
                    setLoadFailView();
                    //点击屏幕重试
                    errorLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setNormalView();
                            initData();
                        }
                    });
            }
        }

        @Override
        public void onNext(ArrayList<MessageEntity> messageList) {
            LogHelper.d(TAG, "MSG messageList: " + messageList);
            if (messageList == null) {
                throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
            }
            mListView.removeHeaderView(mEmptyPage);
            try {
                Reservoir.put(getConstant(), messageList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            /***********测试代码****************/
            //                if (msgList.size() > 6) {
            //                    msgList.get(1).setText("构造函数里提供所需宽度（比如屏幕宽度减去 marginLeft 和 marginRight），文本 (CharSequence, 支持 Span)，对应的 TextPaint, 两个行间距 (multiplier 和 add, 如果为一倍行距就分别为 1.0f 和 0f )，随后即可通过 Layout 提供的 getLineCount() 方法获得行数，getHeight() 获得文本总高度。Layout 这几个类还提供比如某一行的 start, end, top, width, offset 等等\n" +
            //                            "\n" +
            //                            "作者：方自在\n" +
            //                            "链接：https://www.zhihu.com/question/19801466/answer/48050071\n" +
            //                            "来源：知乎\n" +
            //                            "著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。");
            //                    msgList.get(4).setText("构造函数里提供所需宽度（比如屏幕宽度减去 marginLeft 和 marginRi");
            //                    msgList.get(2).setText("Reservoir is down with RxJava! All the async methods have RxJava variants that return observables. These observables are scheduled on a background thread and observed on the main thread by default (you can change this easily by assigning your own schedulers and observers to the returned observable).");
            //                    msgList.get(3).setText("当然，不要忘记重载 getItemPosition() 函数，返回 POSITION_NONE，这个两个类的解决方案都需要的。二者不同之处在于，FragmentStatePagerAdapter 在会在因 POSITION_NONE 触发调用的 destroyItem() 中真正的释放资源，重新建立一个新的 Fragment；而 FragmentPagerAdapter 仅仅会在 destroyItem() 中 detach 这个 Fragment，在 instantiateItem() 时会使用旧的 Fragment，并触发 attach，因此没有释放资源及重建的过程。\n" +
            //                            "\n" +
            //                            "这样，当 notifyDataSetChanged() 被调用后，会最终触发 instantiateItem()，而不管 getItem() 是否被调用，我们都在重载的 instantiateItem() 函数中已经将所需要的数据传递给了相应的 Fragment。在 Fragment 接下来的 onCreateView(), onStart() 以及 onResume() 的事件中，它可以正确的读取新的数据，Fragment 被成功复用了。\n" +
            //                            "\n" +
            //                            "这里需要注意一个问题，在 Fragment 没有被添加到 FragmentManager 之前，我们可以通过 Fragment.setArguments() 来设置参数，并在 Fragment 中，使用 getArguments() 来取得参数。这是常用的参数传递方式。但是这种方式对于我们说的情况不适用。因为这种数据传递方式只可能用一次，在 Fragment 被添加到 FragmentManager 后，一旦被使用，我们再次调用 setArguments() 将会导致 java.lang.IllegalStateException: Fragment already active 异常。因此，我们这里的参数传递方式选择是，在继承的 Fragment 子类中，新增几个 setter，然后通过这些 setter 将数据传递过去。反向也是类似。相关信息可以参考 [5]。哦，这些 setter 中要注意不要操作那些 View，这些 View 只有在 onCreateView() 事件后才可以操作。");
            //                }
            /***********测试代码****************/
            mMessageList = messageList;
        }

        @Override
        protected void showLoading() {
            if (mIsFirstRefresh) {
                mLoadingLayout.setVisibility(View.VISIBLE);
                mLoadingAnimation.start();
            }
        }

        @Override
        protected void dismissLoading() {
            if (mIsFirstRefresh) {
                mLoadingLayout.setVisibility(View.INVISIBLE);
                mLoadingAnimation.stop();
            } else {
                mRefreshLayout.endRefreshing();
            }
        }
    }

    private void refreshListView(boolean isRefresh, ArrayList<MessageEntity> mMessageList) {
        if (isRefresh) {
            mRefreshLayout.endRefreshing();
        } else {
            mRefreshLayout.endLoadingMore();
        }
        if (mMessageList == null) {
            return;
        }
        if (isRefresh) {
            mIsFirstRefresh = false;
            if (mMessageList.size() > 0) {
                removeEmptyPage();
            } else {
                addEmptyPage();
            }
            msgList.clear();
            msgList.addAll(mMessageList);
            Collections.sort(msgList, new Comparator<MessageEntity>() {
                @Override
                public int compare(MessageEntity lhs, MessageEntity rhs) {
                    if (lhs.getId() > rhs.getId()) {
                        return -1;
                    } else if (lhs.getId() < rhs.getId()) {
                        return 1;
                    }
                    return 0;
                }
            });
            mRefreshLayout.setIsShowLoadingMoreView(true);
            mListView.removeFooterView(mFooterView);
            isShowedFooterView = false;
            messageAdapter.notifyDataSetChanged();
            mListView.setVisibility(View.VISIBLE);
        } else {
            if (mMessageList.size() < 1 || mMessageList.size() < size) {
                mListView.removeFooterView(mFooterView);
                isShowedFooterView = true;
                mListView.addFooterView(mFooterView);
                mRefreshLayout.setIsShowLoadingMoreView(false);
                return;
            }
            msgList.addAll(mMessageList);
            Collections.sort(msgList, new Comparator<MessageEntity>() {
                @Override
                public int compare(MessageEntity lhs, MessageEntity rhs) {
                    if (lhs.getId() > rhs.getId()) {
                        return -1;
                    } else if (lhs.getId() < rhs.getId()) {
                        return 1;
                    }
                    return 0;
                }
            });
            messageAdapter.notifyDataSetChanged();
            mListView.setVisibility(View.VISIBLE);
        }
    }

    private void removeEmptyPage() {
        if (misEmpty) {
            mRefreshLayout.removeView(mEmptyPage);
            misEmpty = false;
        }
    }

    private void addEmptyPage() {
        if (!misEmpty) {
            mRefreshLayout.addView(mEmptyPage);
            misEmpty = true;
        }
    }

    private String getConstant() {
        if (msgType.equals(Constants.MSG_COMON)) {   //0:仅个人消息，1:仅公共消息，2:个人消息和公共消息
            return Constants.MSG_TYPE + Constants.MSG_COMON;
        } else {
            return Constants.MSG_TYPE + Constants.MSG_PERSONAL;
        }
    }

    private class BeseBeanSubscriber extends BaseSubscriber<SimpleResponse> {
        private int position;
        private int id;

        public BeseBeanSubscriber(Context context, int id, int position) {
            super(context);
            this.id = id;
            this.position = position;
        }

        @Override
        protected void onTimeout() {

        }

        @Override
        public void onCompleted() {
            LogHelper.d(TAG, "onCompleted");
            checkifHaveUnReadMsg();
        }

        @Override
        public void onNext(SimpleResponse simpleResponse) {
            if (simpleResponse == null) {
                throw (new ApiException(ApiException.ERROR_LOAD_FAIL));
            }
            int code = simpleResponse.getCode();
            if (code != 200) {
                throw (new ApiException(code + ""));
            }
            MessageEntity data2 = msgList.get(position);
            data2.setIs_read(1);
            updateView(position);
            if (position < 5) {
                messageAdapter.notifyDataSetChanged();
            }
            for (int i = 0; i < msgList.size(); i++) {
                MessageEntity data = msgList.get(i);
                if (data.getId() == id) {
                    LogHelper.d(TAG, "data: " + data.toString());
                }
            }
            Intent intent = new Intent(Constants.ACTION_MSG_COUNTDOWN);
            intent.putExtra(Constants.MSG_TYPE, msgType);
            mContext.sendBroadcast(intent);
        }

        @Override
        protected void handleError(Throwable e) {

        }
    }

    /**
     * @param itemIndex
     */
    private void updateView(int itemIndex) {
        LogHelper.d(TAG, "updateView: itemIndex: " + itemIndex);
        int visiblePostion = mListView.getFirstVisiblePosition();
        if (mListView != null) {
            View v = mListView.getChildAt(itemIndex - visiblePostion);
            if (v != null) {
                LogHelper.d(TAG, "updateView: itemIndex:" + itemIndex + "  visiblePostion:" + visiblePostion);
                View dot = v.findViewById(R.id.message_haveread_dot);
                if (dot != null) {
                    dot.setVisibility(View.GONE);
                }
            }
        }
    }


    private Runnable mDelayHiddenLoadingMoreViewTask = new Runnable() {
        @Override
        public void run() {
            mRefreshLayout.endLoadingMore();
        }
    };

    protected void setWithoutContentView() {
        getErrorLayout().setVisibility(View.VISIBLE);
        getErrorDescTv().setVisibility(View.GONE);
    }

    protected void setNetworkErrorView() {
        getErrorLayout().setVisibility(View.VISIBLE);
        getErrorDescTv().setText("呀，没网了~");
        getErrorDescTv().setVisibility(View.VISIBLE);
    }

    protected void setCannotVisitView() {
        getErrorLayout().setVisibility(View.VISIBLE);
        getErrorDescTv().setVisibility(View.GONE);
    }

    protected void setLoadFailView() {
        mLoadingLayout.setVisibility(View.GONE);
        getErrorLayout().setVisibility(View.VISIBLE);
        mErrorImage.setImageResource(R.mipmap.icon_error_load_failure);
        getErrorDescTv().setText(getString(R.string.error_msg_load_failure));
        getErrorDescTv().setVisibility(View.VISIBLE);
    }

    protected void setNormalView() {
        getErrorLayout().setVisibility(View.GONE);
    }

    public RelativeLayout getErrorLayout() {
        return errorLayout;
    }

    public TextView getErrorDescTv() {
        return descText;
    }
}
