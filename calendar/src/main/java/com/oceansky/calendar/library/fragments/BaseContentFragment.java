package com.oceansky.calendar.library.fragments;

import android.support.v4.app.Fragment;
import android.widget.ListView;

import org.joda.time.DateTime;

/**
 * Created by wxg on 2018/3/9.
 */

public class BaseContentFragment extends Fragment {
    private DateTime dateTime;
    private ListView listView;
    public void changeDateTime(DateTime dateTime) {
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public ListView getListView() {
        return listView;
    }
}
