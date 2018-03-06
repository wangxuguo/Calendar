package com.oceansky.teacher.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.network.response.HomeworkListEntity;

import java.util.List;

/**
 * User: dengfa
 * Date: 2016/8/9
 * Tel:  18500234565
 */
public class HomeworkAdapter extends BaseAdapter<HomeworkListEntity.HomeworkData> {

    private static final String STRING_EMPTY_DEFAULT = "未获取";
    private final Context mContext;

    public HomeworkAdapter(Context context, List<HomeworkListEntity.HomeworkData> dataList) {
        super(context, dataList);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HomeworkListEntity.HomeworkData data = mDatas.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_homework_lv, null);
            holder.title = (TextView) convertView.findViewById(R.id.homework_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String title = data.getTitle();
        title = TextUtils.isEmpty(title) ? STRING_EMPTY_DEFAULT : title;
        holder.title.setText(title);
        return convertView;
    }

    public class ViewHolder {
        public TextView title;
    }
}
