package com.oceansky.calendar.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.oceansky.calendar.example.R;

import java.util.ArrayList;

/**
 * User: 王旭国
 * Date: 16/6/15 16:43
 * Email:wangxuguo@jhyx.com.cn
 */
public class PickWeekAdapter extends BaseAdapter{
    private final Context context;
    private final ArrayList<String> list;

    public PickWeekAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){
            convertView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_pop_list_weeks,null);
            viewHolder = new ViewHolder();
            viewHolder.tv = (TextView) convertView.findViewById(R.id.tv);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                viewHolder.tv.setTextColor(context.getResources().getColorStateList(R.color.listpop_week_textcolor_selector,null));
//            }else{
//                viewHolder.tv.setTextColor(context.getResources().getColorStateList(R.color.listpop_week_textcolor_selector));
//            }
//            viewHolder.tv.setBackgroundResource(R.drawable.listpop_week_bg_selector);
//            viewHolder.tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            viewHolder.tv.setText((String)getItem(position));
//            viewHolder.tv.setGravity(Gravity.CENTER);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.tv .setText((String)getItem(position));
        }
        return convertView;
    }
    public static class ViewHolder{
        TextView tv;
    }
}
