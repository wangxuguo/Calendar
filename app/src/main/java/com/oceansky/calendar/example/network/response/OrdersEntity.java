package com.oceansky.example.network.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * User: dengfa
 * Date: 16/6/16
 * Tel:  18500234565
 */
public class OrdersEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @SerializedName("list")
    private ArrayList<OrdersData> orderList;

    public ArrayList<OrdersData> getOrderList() {
        return orderList;
    }

    public void setOrderList(ArrayList<OrdersData> orderList) {
        this.orderList = orderList;
    }

    @Override
    public String toString() {
        return "Data{" +
                "orderList=" + orderList +
                '}';
    }

    public static class OrdersData implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("id")
        private int id;

        @SerializedName("plan_name")
        private String plan_name;

        @SerializedName("start_date")
        private String start_date;

        @SerializedName("end_date")
        private String end_date;

        @SerializedName("unit_price")
        private int unit_price;

        @SerializedName("total_class_hours")
        private int total_class_hours;

        @SerializedName("price_performance")
        private int price_performance;

        @SerializedName("performance_description")
        private String performance_description;

        @SerializedName("order_code")
        private String order_code;

        @SerializedName("total_price")
        private String total_price;

        @SerializedName("status_description")
        private String status_description;

        @SerializedName("school_name")
        private String school_name;

        @SerializedName("school_short_name")
        private String school_short_name;

        public String getPlan_name() {
            return plan_name;
        }

        public void setPlan_name(String plan_name) {
            this.plan_name = plan_name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getStart_date() {
            return start_date;
        }

        public void setStart_date(String start_date) {
            this.start_date = start_date;
        }

        public String getEnd_date() {
            return end_date;
        }

        public void setEnd_date(String end_date) {
            this.end_date = end_date;
        }

        public int getUnit_price() {
            return unit_price;
        }

        public void setUnit_price(int unit_price) {
            this.unit_price = unit_price;
        }

        public int getTotal_class_hours() {
            return total_class_hours;
        }

        public void setTotal_class_hours(int total_class_hours) {
            this.total_class_hours = total_class_hours;
        }

        public int getPrice_performance() {
            return price_performance;
        }

        public void setPrice_performance(int price_performance) {
            this.price_performance = price_performance;
        }

        public String getPerformance_description() {
            return performance_description;
        }

        public void setPerformance_description(String performance_description) {
            this.performance_description = performance_description;
        }

        public String getOrder_code() {
            return order_code;
        }

        public void setOrder_code(String order_code) {
            this.order_code = order_code;
        }

        public String getTotal_price() {
            return total_price;
        }

        public void setTotal_price(String total_price) {
            this.total_price = total_price;
        }

        public String getStatus_description() {
            return status_description;
        }

        public void setStatus_description(String status_description) {
            this.status_description = status_description;
        }

        public String getSchool_name() {
            return school_name;
        }

        public void setSchool_name(String school_name) {
            this.school_name = school_name;
        }

        public String getSchool_short_name() {
            return school_short_name;
        }

        public void setSchool_short_name(String school_short_name) {
            this.school_short_name = school_short_name;
        }

        @Override
        public String toString() {
            return "OrdersData{" +
                    "id=" + id +
                    ", plan_name='" + plan_name + '\'' +
                    ", start_date='" + start_date + '\'' +
                    ", end_date='" + end_date + '\'' +
                    ", unit_price=" + unit_price +
                    ", total_class_hours=" + total_class_hours +
                    ", price_performance=" + price_performance +
                    ", performance_description='" + performance_description + '\'' +
                    ", order_code='" + order_code + '\'' +
                    ", total_price='" + total_price + '\'' +
                    ", status_description='" + status_description + '\'' +
                    ", school_name='" + school_name + '\'' +
                    ", school_short_name='" + school_short_name + '\'' +
                    '}';
        }
    }
}