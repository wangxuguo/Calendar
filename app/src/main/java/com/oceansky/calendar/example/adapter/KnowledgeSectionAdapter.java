package com.oceansky.calendar.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oceansky.calendar.example.R;
import com.oceansky.calendar.example.network.response.KnowledgePointEntity;

import java.util.List;

/**
 * User: dengfa
 * Date: 29/8/2016
 * Tel:  18500234565
 */
public class KnowledgeSectionAdapter extends BaseAdapter<KnowledgePointEntity.SectionDetail> {

    private final Context mContext;

    public KnowledgeSectionAdapter(Context context, List<KnowledgePointEntity.SectionDetail> dataList) {
        super(context, dataList);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        KnowledgePointEntity.SectionDetail data = mDatas.get(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_knowledge_point, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.tv_knowledge_point);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(data.getName());
        return convertView;
    }

    public class ViewHolder {
        public TextView title;
    }
}
