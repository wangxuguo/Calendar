package com.oceansky.teacher.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.network.response.ClassListEntity;

import java.util.List;

/**
 * User: dengfa
 * Date: 12/6/2016
 * Tel:  18500234565
 */
public class ClassListAdapter extends BaseAdapter<ClassListEntity> {

    private final Context mContext;

    public ClassListAdapter(Context context, List<ClassListEntity> dataList) {
        super(context, dataList);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ClassListEntity data = mDatas.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_class_lv, null);
            holder.title = (TextView) convertView.findViewById(R.id.class_list_tv_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(data.getTitle());
        return convertView;
    }

    public class ViewHolder {
        public TextView title;
    }
}
