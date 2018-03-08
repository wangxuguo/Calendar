package com.oceansky.calendar.example.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.oceansky.calendar.example.network.response.MessageEntity;
import com.oceansky.calendar.example.utils.StringUtils;
import com.oceansky.calendar.example.R;
import com.oceansky.calendar.example.utils.LogHelper;

import java.util.ArrayList;

/**
 * User: 王旭国
 * Date: 16/8/18 16:43
 * Email:wangxuguo@jhyx.com.cn
 */
public class MessageExpandAdapter extends BaseAdapter<MessageEntity> {
    private static final String MSG_DATETIME_FOMATOR = "MM/dd hh:mm";
    private static final String TAG                  = MessageExpandAdapter.class.getSimpleName();
    private     ListView                                 listView;
    private     Context                                  mContext;
    private     MutilLinesMSGSetReadedListener           mutilLinesMSGSetReadedListener;
    private ExpandableTextView.OnItemClickedListener onItemClicked;
//    private SparseArray<Boolean> sparseArray = new SparseArray<>();  //记录是否展开
    public MessageExpandAdapter(Context mContext, ListView listView, ArrayList mListDatas) {
        super(mContext, mListDatas);
        this.mContext = mContext;
        this.listView = listView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LogHelper.d(TAG, "mDatas.get: position: )" + mDatas.get(position));
        final MessageEntity data = mDatas.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_message_expand, null);
            holder.message_title = (TextView) convertView.findViewById(R.id.message_title);
            holder.message_time = (TextView) convertView.findViewById(R.id.message_time);
            holder.message_content = (ExpandableTextView) convertView.findViewById(R.id.expand_text_view);
            holder.message_haveread_dot = convertView.findViewById(R.id.message_haveread_dot);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (data != null) {
            holder.message_title.setText(data.getTitle());
            LogHelper.d(TAG, "date.timestamp: " + data.getTime());
            String date = new java.text.SimpleDateFormat("MM/dd HH:mm").format(new java.util.Date((long) data.getTime() * 1000));

            holder.message_time.setText(date);
//            holder.message_time.setText(new DateTime(data.getTime()*1000).toString(MSG_DATETIME_FOMATOR));
            holder.message_content.setText(StringUtils.ToDBC(data.getText()));
            holder.message_content.setOnExpandStateChangeListener(new ExpandableTextView.OnExpandStateChangeListener() {
                @Override
                public void onExpandStateChanged(TextView textView, boolean isExpanded) {
                    if (isExpanded&&mutilLinesMSGSetReadedListener!=null) {
                        mutilLinesMSGSetReadedListener.setMSGReaded(data.getId(),position);
                    }
                }
            });
            holder.message_content.setPosition(position);
            holder.message_content.setItemClick(onItemClicked);

            if (data.getIs_read() == 0) {
                holder.message_haveread_dot.setVisibility(View.VISIBLE);
            } else {
                holder.message_haveread_dot.setVisibility(View.GONE);
            }

        }
//        holder.title.setText(data.getTitle());
        return convertView;
    }



    public class ViewHolder {
        public TextView           message_title;
        public TextView           message_time;
        public ExpandableTextView message_content;
        public View               message_haveread_dot;
    }




    public void setMutilLinesMSGSetReadedListener(MutilLinesMSGSetReadedListener mutilLinesMSGSetReadedListener) {
        this.mutilLinesMSGSetReadedListener = mutilLinesMSGSetReadedListener;
    }

    public interface MutilLinesMSGSetReadedListener {
         void setMSGReaded(int itemid,int postion);
    }

    public void setOnItemClicked(ExpandableTextView.OnItemClickedListener onItemClicked) {
        this.onItemClicked = onItemClicked;
    }
}
