package com.oceansky.teacher.customviews.adapter;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.oceansky.teacher.utils.LogHelper;

/**
 * User: 王旭国
 * Date: 16/6/29 09:55
 * Email:wangxuguo@jhyx.com.cn
 */
public class InfiniteCoursePageAdapter extends PagerAdapter {
    private static final String TAG = InfiniteCoursePageAdapter.class.getSimpleName();
    private PagerAdapter adapter;
    Fragment currentFragment;
    public InfiniteCoursePageAdapter(PagerAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getCount() {
        // warning: scrolling to very high values (1,000,000+) results in
        // strange drawing behaviour
        return Integer.MAX_VALUE;
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        currentFragment = (Fragment) object;
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * @return the {@link #getCount()} result of the wrapped adapter
     */
    public int getRealCount() {
        return adapter.getCount();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int virtualPosition = position % getRealCount();
        // only expose virtual position to the inner adapter
        LogHelper.d(TAG,"instantiateItem: position: "+position+" virtualPosition: "+ virtualPosition);
        return adapter.instantiateItem(container, virtualPosition);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        int virtualPosition = (position) % getRealCount();
        // only expose virtual position to the inner adapter
        LogHelper.d(TAG,"destroyItem: position: "+position+" virtualPosition: "+ virtualPosition);
        adapter.destroyItem(container, virtualPosition, object);
    }

    /*
     * Delegate rest of methods directly to the inner adapter.
     */

    @Override
    public void finishUpdate(ViewGroup container) {
        adapter.finishUpdate(container);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return adapter.isViewFromObject(view, object);
    }

    @Override
    public void restoreState(Parcelable bundle, ClassLoader classLoader) {
        adapter.restoreState(bundle, classLoader);
    }

    @Override
    public Parcelable saveState() {
        return adapter.saveState();
    }

    @Override
    public void startUpdate(ViewGroup container) {
        adapter.startUpdate(container);
    }
}
