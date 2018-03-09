package com.oceansky.calendar.example.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.oceansky.calendar.example.R;
import com.oceansky.calendar.example.constant.CaldroidCustomConstant;
import com.oceansky.calendar.example.customviews.adapter.TeacherCourseAdapter;
import com.oceansky.calendar.example.entity.TearcherCourseListItemBean;
import com.oceansky.calendar.example.manager.TeacherCourseManager;
import com.oceansky.calendar.example.utils.LogHelper;

//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;


import java.util.ArrayList;

/**
 * 时间 课表 每日
 * User: 王旭国ter
 * Date: 16/6/16 10:55
 * Email:wangxuguo@jhyx.com.cn
 */
public class CoursesFragment extends Fragment {
    private static final String TAG = CoursesFragment.class.getSimpleName();
    private DateTime dateTime;
    private ListView listView;
    private TeacherCourseAdapter teacherCourseAdapter;
    private TeacherCourseManager teacherCourseManager;
    private ArrayList<TearcherCourseListItemBean> list;
    private View emptyView;
//    private View                                  nologinView;

    public void changeDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
        LogHelper.d(TAG, this.toString() + "  changeDateTime: " + dateTime);
        if (listView == null) {
            if (listView == null) {
                LogHelper.e(TAG, "listview null");
                return;
            }
        }
        if (teacherCourseManager == null) {
            teacherCourseManager = TeacherCourseManager.getInstance(getActivity());
        }
        LogHelper.d(TAG, "teacherCouserManager: getTeacherCourse " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
        list = teacherCourseManager.getTeacherCourse(dateTime);
        initViews();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogHelper.i(TAG, this.toString() + "   onCreateView    ");
        View view = inflater.inflate(R.layout.fragment_course_item, null);
        listView = (ListView) view.findViewById(R.id.listview_course);
        teacherCourseManager = TeacherCourseManager.getInstance(getActivity());
        emptyView = view.findViewById(R.id.empty_teachercourse);
//        nologinView = view.findViewById(R.id.layout_nologin_page);
        if (dateTime == null) {
            dateTime = DateTime.now();
        }
        LogHelper.d(TAG, "teacherCouserManager: getTeacherCourse " + dateTime.toString(CaldroidCustomConstant.simpleFormator));
        list = teacherCourseManager.getTeacherCourse(dateTime);
        initViews();
        listView.setOnItemClickListener((parent, view1, position, id) -> {
//            TearcherCourseListItemBean item = list.get(position);
//            LogHelper.d(TAG, "TearcherCourseListItemBean: " + item);
//            Intent intent = new Intent(getActivity(), CourseDetailActivity.class);
//            intent.putExtra(Constants.WEBVIEW_URL, item.getDetail_url());
//            intent.putExtra(Constants.WEBVIEW_TITLE, item.getTitle());
//            startActivity(intent);
        });
        return view;
    }

    /**
     *
     */
    public void initViews() {
        if (list == null) {
            emptyView.setVisibility(View.GONE);
//            nologinView.setVisibility(View.VISIBLE);
            LogHelper.d(TAG, "list size is empty ");
//            ImageView iv = (ImageView) nologinView.findViewById(R.id.iv_image);
//            iv.setImageResource(R.mipmap.icon_error_unlogin);
//            TextView tv = (TextView) nologinView.findViewById(R.id.tv_detail);
//            tv.setTextColor(getActivity().getResources().getColor(R.color.text_gray_deep));
//            tv.setText(Html.fromHtml(getActivity().getString(R.string.login_register_class)));
//            tv.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    Intent intent = new Intent(getActivity(), LoginActivity.class);
////                    getActivity().startActivity(intent);
//                }
//            });

            list = new ArrayList<>();
        } else if (list.size() == 0) {
            LogHelper.d(TAG, "list.size: " + list.size());
            emptyView.setVisibility(View.VISIBLE);
//            nologinView.setVisibility(View.GONE);
            listView.setEmptyView(emptyView);
        } else {
            listView.setEmptyView(null);
            emptyView.setVisibility(View.GONE);
//            nologinView.setVisibility(View.GONE);
            LogHelper.d(TAG, "list.size: " + list.size());
            for (int i = 0; i < list.size(); i++) {
                LogHelper.d(TAG, "" + list.get(i).toString());
            }
        }

        teacherCourseAdapter = new TeacherCourseAdapter(getContext(), dateTime, list);
        listView.setAdapter(teacherCourseAdapter);
    }

    public ListView getListView() {
        return listView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogHelper.d(TAG, "onViewCreated: ");
        if (listView == null) {
            LogHelper.e(TAG, "onViewCreated listview NULL");
        }

    }

    @UiThread
//    @Subscribe
    public void onTeacherCourseChanged(TeacherCourseManager.TeacherCourseChanged teacherCourseChanged) {

        if (teacherCourseManager == null) {
            teacherCourseManager = TeacherCourseManager.getInstance(getActivity());
        }
        if (dateTime == null) {
            dateTime = DateTime.now();
        }

        LogHelper.d(TAG, this.toString() + "onTeacherCourseChanged " + "   " + dateTime.withTime(0, 0, 0, 0).toString(CaldroidCustomConstant.simpleFormator));
        list = teacherCourseManager.getTeacherCourse(dateTime.withTime(0, 0, 0, 0));
        //        list = teacherCourseManager.getTeacherCource(dateTime);
        //        if (list == null || list.size() == 0) {
        //            emptyView.setVisibility(View.VISIBLE);
        //            listView.setEmptyView(emptyView);
        //            if (list == null) {
        //                list = new ArrayList<>();
        //            }
        //            LogHelper.d(TAG, "list.size = 0 ");
        //        } else {
        //            listView.setEmptyView(null);
        //            LogHelper.d(TAG, "list.get(0): " + list.get(0).toString());
        //            emptyView.setVisibility(View.GONE);
        //        }
        initViews();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        EventBus.getDefault().unregister(this);
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }
}
