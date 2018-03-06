package com.oceansky.teacher.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oceansky.teacher.R;
import com.oceansky.teacher.network.response.ClassEntity;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * User: dengfa
 * Date: 12/6/2016
 * Tel:  18500234565
 */
public class ClassStudentAdapter extends BaseAdapter<ClassEntity.Kid> {

    private final Context mContext;
    private final int     mItemWidth;

    public ClassStudentAdapter(Context context, List<ClassEntity.Kid> dataList, int itemWidth) {
        super(context, dataList);
        mContext = context;
        mItemWidth = itemWidth;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ClassEntity.Kid student = mDatas.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_class_student, null);
            holder.name = (TextView) convertView.findViewById(R.id.class_student_tv_name);
            holder.tvPhoto = (TextView) convertView.findViewById(R.id.class_studen_tv_photo);
            holder.ivPhoto = (CircleImageView) convertView.findViewById(R.id.class_student_iv_photo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (student != null) {
            String name = student.getName().trim();
            holder.name.setText(name);
            String avatar = student.getAvatar();
            if (TextUtils.isEmpty(avatar)) {
                if (name.length() > 0) {
                    holder.tvPhoto.setText(name.substring(name.length() - 1));
                    holder.tvPhoto.setVisibility(View.VISIBLE);
                    holder.ivPhoto.setVisibility(View.INVISIBLE);
                } else {
                    holder.ivPhoto.setImageResource(R.mipmap.student_photo_default);
                    holder.tvPhoto.setVisibility(View.INVISIBLE);
                    holder.ivPhoto.setVisibility(View.VISIBLE);
                }
            } else {
                holder.tvPhoto.setVisibility(View.INVISIBLE);
                holder.ivPhoto.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(avatar, holder.ivPhoto);
            }
        }
        AbsListView.LayoutParams param = new AbsListView.LayoutParams(mItemWidth, mItemWidth);
        convertView.setLayoutParams(param);
        return convertView;
    }

    public class ViewHolder {
        public TextView        name;
        public TextView        tvPhoto;
        public CircleImageView ivPhoto;
    }
}
