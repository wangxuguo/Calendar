package com.oceansky.calendar.example.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.oceansky.calendar.example.customviews.adapter.CaldroidGridAdapter;
import com.oceansky.calendar.example.R;

/**
 * Created by administrator on 16/6/2.
 */
public class DateGridFragment extends Fragment {

    private GridView gridView;
    private int gridViewRes;
    private CaldroidGridAdapter gridAdapter;
    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemLongClickListener onItemLongClickListener;
    public void setGridViewRes(int gridViewRes) {
        this.gridViewRes = gridViewRes;
    }

    public void setGridAdapter(CaldroidGridAdapter gridAdapter) {
        this.gridAdapter = gridAdapter;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // If gridViewRes is not valid, use default fragment layout
        if (gridViewRes == 0) {
            gridViewRes = R.layout.date_grid_fragment;
        }


        if (gridView == null) {
            LayoutInflater localInflater = LayoutInflater.from(getActivity());
            gridView = (GridView) localInflater.inflate(gridViewRes, container, false);
            setupGridView();
        } else {
            ViewGroup parent = (ViewGroup) gridView.getParent();
            if (parent != null) {
                parent.removeView(gridView);
            }
        }

        return gridView;
    }
    private void setupGridView() {


        // Client normally needs to provide the adapter and onItemClickListener
        // before the fragment is attached to avoid complex crash due to
        // fragment life cycles
        if (gridAdapter != null) {
            gridView.setAdapter(gridAdapter);
        }
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        if (onItemClickListener != null) {
            gridView.setOnItemClickListener(onItemClickListener);
        }
        if (onItemLongClickListener != null) {
            gridView.setOnItemLongClickListener(onItemLongClickListener);
        }
    }
}
