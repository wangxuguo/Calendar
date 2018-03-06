package com.oceansky.teacher.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.entity.CourseBeanForAdapter;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by dengfa on 12/6/2016.
 */
public class CourseAdapter extends BaseAdapter<CourseBeanForAdapter> {
    private final static String TAG = CourseAdapter.class.getSimpleName();

    private static final String STRING_EMPTY_DEFAULT = "未获取";
    private final Context mContext;

    public CourseAdapter(Context context, List<CourseBeanForAdapter> dataList) {
        super(context, dataList);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CourseBeanForAdapter data = mDatas.get(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_courses, null);
            holder.title = (TextView) convertView.findViewById(R.id.course_tv_title);
            holder.state = (TextView) convertView.findViewById(R.id.course_tv_state);
            holder.date = (TextView) convertView.findViewById(R.id.course_tv_date);
            holder.time = (TextView) convertView.findViewById(R.id.course_tv_time);
            holder.grade = (TextView) convertView.findViewById(R.id.course_tv_grade);
            holder.school = (TextView) convertView.findViewById(R.id.course_tv_school);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String title = data.getTitle();
        title = TextUtils.isEmpty(title) ? STRING_EMPTY_DEFAULT : title;
        holder.title.setText(title);
        int status = data.getStatus();
        int color;
        int bg;
        String status_des = data.getStatus_des();
        String text = TextUtils.isEmpty(status_des) ? STRING_EMPTY_DEFAULT : status_des;
        switch (status) {
            case Constants.COURSE_STATE_END:
                color = R.color.text_course_state_finish;
                bg = R.drawable.bg_course_state_finish;
                break;
            case Constants.COURSE_STATE_WAIT:
                color = R.color.text_course_state_future;
                bg = R.drawable.bg_course_state_future;
                break;
            case Constants.COURSE_STATE_ING:
                color = R.color.text_course_state_inclass;
                bg = R.drawable.bg_course_state_inclass;
                break;
            default:
                color = R.color.text_course_state_other;
                bg = R.drawable.bg_course_state_other;
        }
        holder.state.setText(text);
        holder.state.setBackgroundDrawable(mContext.getResources().getDrawable(bg));
        holder.state.setTextColor(mContext.getResources().getColor(color));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        long start_date = data.getStart_date();
        String startDate = start_date < 0 ? STRING_EMPTY_DEFAULT : simpleDateFormat.format(start_date * 1000);
        long end_date = data.getEnd_date();
        String endDate = end_date < 0 ? STRING_EMPTY_DEFAULT : simpleDateFormat.format(end_date * 1000);
        holder.date.setText(String.format("%s - %s", startDate, endDate));
        String start_time = data.getStart_time();
        String end_time = data.getEnd_time();
        start_time = TextUtils.isEmpty(start_time) ? STRING_EMPTY_DEFAULT : start_time;
        end_time = TextUtils.isEmpty(end_time) ? STRING_EMPTY_DEFAULT : end_time;
        holder.time.setText(String.format("%s - %s", start_time, end_time));
        String grade_name = data.getGrade_name();
        grade_name = TextUtils.isEmpty(grade_name) ? STRING_EMPTY_DEFAULT : grade_name;
        holder.grade.setText(grade_name);
        String class_room = data.getClass_room();
        class_room = TextUtils.isEmpty(class_room) ? STRING_EMPTY_DEFAULT : class_room;
        holder.school.setText(class_room);
        return convertView;
    }

    public class ViewHolder {
        public TextView title;
        public TextView state;
        public TextView date;
        public TextView time;
        public TextView grade;
        public TextView school;

    }
}
