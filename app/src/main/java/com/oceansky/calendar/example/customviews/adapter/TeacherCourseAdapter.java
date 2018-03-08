package com.oceansky.calendar.example.customviews.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.oceansky.calendar.example.entity.TearcherCourseListItemBean;
import com.oceansky.calendar.example.R;

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * User: 王旭国
 * Date: 16/6/16 11:22
 * Email:wangxuguo@jhyx.com.cn
 */
public class TeacherCourseAdapter extends BaseAdapter {

    private Context context;
    private DateTime dateTime;
    private ArrayList<TearcherCourseListItemBean> list;


    public TeacherCourseAdapter(Context context, DateTime dateTime) {
    }

    public TeacherCourseAdapter(Context context, DateTime dateTime, ArrayList<TearcherCourseListItemBean> list) {
        this.context = context;
        this.dateTime = dateTime;
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
        final TearcherCourseListItemBean course = list.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.item_teachercourse, null);
            holder.teachercourse_tv_timeinfo = (TextView) convertView.findViewById(R.id.teachercourse_tv_timeinfo);
            holder.teachercourse_tv_title = (TextView) convertView.findViewById(R.id.teachercourse_tv_title);
            holder.teachercourse_tv_loc = (TextView) convertView.findViewById(R.id.teachercourse_tv_loc);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(course!=null){
            holder.teachercourse_tv_timeinfo.setText(course.getStarttime()+"-"+course.getEndtime());
//            holder.teachercourse_tv_timeinfo.setText(course.getClass_times().get(0).toString()+course.getClass_times().get(1).toString());
            holder.teachercourse_tv_title.setText(course.getTitle());
            holder.teachercourse_tv_loc.setText(course.getClass_room());
        }
        return convertView;
    }
    public class ViewHolder {
        public TextView teachercourse_tv_timeinfo;
        public TextView teachercourse_tv_title;
        public TextView teachercourse_tv_loc;
    }
}
