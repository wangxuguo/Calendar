package com.oceansky.teacher.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.network.response.OrdersEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * User: dengfa
 * Date: 16/6/2016
 * Tel:  18500234565
 */
public class OrdersAdapter extends BaseAdapter<OrdersEntity.OrdersData> {

    public static final String STRING_EMPTY_DEFAULT = "未获取";
    private final Context mContext;

    public OrdersAdapter(Context context, List<OrdersEntity.OrdersData> dataList) {
        super(context, dataList);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OrdersEntity.OrdersData ordersData = mDatas.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_orders, null);
            holder.ordersTvSchool = (TextView) convertView.findViewById(R.id.orders_tv_school);
            holder.ordersTvState = (TextView) convertView.findViewById(R.id.orders_tv_state);
            holder.ordersTvClass = (TextView) convertView.findViewById(R.id.orders_tv_class);
            holder.ordersTvDate = (TextView) convertView.findViewById(R.id.orders_tv_date);
            holder.ordersTvPrice = (TextView) convertView.findViewById(R.id.orders_tv_price);
            holder.ordersTvPeriod = (TextView) convertView.findViewById(R.id.orders_tv_period);
            holder.ordersTvPerformance = (TextView) convertView.findViewById(R.id.orders_tv_performance);
            holder.ordersTvNumber = (TextView) convertView.findViewById(R.id.orders_tv_number);
            holder.ordersTvTotal = (TextView) convertView.findViewById(R.id.orders_tv_total);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String school_name = ordersData.getSchool_name();
        school_name = TextUtils.isEmpty(school_name) ? STRING_EMPTY_DEFAULT : school_name;
        holder.ordersTvSchool.setText(school_name);
        String status_description = ordersData.getStatus_description();
        status_description = TextUtils.isEmpty(status_description) ? STRING_EMPTY_DEFAULT : status_description;
        int textColor = R.color.text_course_state_other;
        int bg = R.drawable.bg_course_state_other;
        if (TextUtils.equals("进行中", status_description)) {
            textColor = R.color.text_course_state_inclass;
            bg = R.drawable.bg_course_state_inclass;
        } else if (TextUtils.equals("已成功", status_description)) {
            textColor = R.color.text_course_state_finish;
            bg = R.drawable.bg_course_state_finish;
        } else if (TextUtils.equals("已关闭", status_description)) {
            textColor = R.color.text_order_state_close;
            bg = R.drawable.bg_order_state_close;
        }
        holder.ordersTvState.setText(status_description);
        holder.ordersTvState.setTextColor(mContext.getResources().getColor(textColor));
        holder.ordersTvState.setBackgroundDrawable(mContext.getResources().getDrawable(bg));
        String plan_name = ordersData.getPlan_name();
        plan_name = TextUtils.isEmpty(plan_name) ? STRING_EMPTY_DEFAULT : plan_name;
        holder.ordersTvClass.setText(plan_name);
        String start_date = ordersData.getStart_date();
        String end_date = ordersData.getEnd_date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy.MM.dd");
            if (TextUtils.isEmpty(start_date)) {
                start_date = STRING_EMPTY_DEFAULT;
            } else {
                Date start = dateFormat.parse(start_date);
                start_date = dateFormat2.format(start);
            }
            if (TextUtils.isEmpty(end_date)) {
                end_date = STRING_EMPTY_DEFAULT;
            } else {
                Date end = dateFormat.parse(end_date);
                end_date = dateFormat2.format(end);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.ordersTvDate.setText(start_date + " - " + end_date);
        holder.ordersTvPrice.setText(ordersData.getUnit_price() + "元/课时");
        holder.ordersTvPeriod.setText(ordersData.getTotal_class_hours() + "课时");
        holder.ordersTvPerformance.setText("绩效: " + ordersData.getPrice_performance() + "元");
        String order_code = ordersData.getOrder_code();
        order_code = TextUtils.isEmpty(order_code) ? STRING_EMPTY_DEFAULT : order_code;
        holder.ordersTvNumber.setText("订单编号: " + order_code);
        String total_price = ordersData.getTotal_price();
        total_price = TextUtils.isEmpty(total_price) ? STRING_EMPTY_DEFAULT : total_price + "元";
        holder.ordersTvTotal.setText("总计: " + total_price);
        return convertView;
    }

    public class ViewHolder {
        TextView ordersTvSchool;
        TextView ordersTvState;
        TextView ordersTvClass;
        TextView ordersTvDate;
        TextView ordersTvPrice;
        TextView ordersTvPeriod;
        TextView ordersTvPerformance;
        TextView ordersTvNumber;
        TextView ordersTvTotal;
    }
}
