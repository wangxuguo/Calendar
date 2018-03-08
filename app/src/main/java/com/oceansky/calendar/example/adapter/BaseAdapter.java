package com.oceansky.calendar.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;

import java.util.Collections;
import java.util.List;

public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {
    protected LayoutInflater mInflater;
    protected Context mContext;
    protected List<T> mDatas;

    public BaseAdapter(Context mContext, List<T> mDatas) {
        mInflater = LayoutInflater.from(mContext);
        this.mContext = mContext;
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<T> getDatas() {
        return this.mDatas;
    }

    public void addNewDatas(List<T> datas) {
        if (datas != null) {
            this.mDatas.addAll(0, datas);
            this.notifyDataSetChanged();
        }

    }

    public void addMoreDatas(List<T> datas) {
        if (datas != null) {
            this.mDatas.addAll(this.mDatas.size(), datas);
            this.notifyDataSetChanged();
        }

    }

    public void setDatas(List<T> datas) {
        if (datas != null) {
            this.mDatas = datas;
        } else {
            this.mDatas.clear();
        }

        this.notifyDataSetChanged();
    }

    public void clear() {
        this.mDatas.clear();
        this.notifyDataSetChanged();
    }

    public void removeItem(int position) {
        this.mDatas.remove(position);
        this.notifyDataSetChanged();
    }

    public void removeItem(T model) {
        this.mDatas.remove(model);
        this.notifyDataSetChanged();
    }

    public void addItem(int position, T model) {
        this.mDatas.add(position, model);
        this.notifyDataSetChanged();
    }

    public void addFirstItem(T model) {
        this.addItem(0, model);
    }

    public void addLastItem(T model) {
        this.addItem(this.mDatas.size(), model);
    }

    public void setItem(int location, T newModel) {
        this.mDatas.set(location, newModel);
        this.notifyDataSetChanged();
    }

    public void setItem(T oldModel, T newModel) {
        this.setItem(this.mDatas.indexOf(oldModel), newModel);
    }

    public void moveItem(int fromPosition, int toPosition) {
        Collections.swap(this.mDatas, fromPosition, toPosition);
        this.notifyDataSetChanged();
    }
}
