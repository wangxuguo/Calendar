package com.oceansky.teacher.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.TouchDelegate;
import android.view.View;

/**
 * User: dengfa
 * Date: 16/6/13
 * Tel:  18500234565
 */
public class DisplayUtils {
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 扩大点击范围
     * @param delegate
     * @param parent
     * @param sizeInDp
     */
    public static void touchDelegate(final View delegate, final View parent, final int sizeInDp) {
        if (parent != null && delegate != null) {
            if(parent instanceof  View) {
                final boolean post = parent.post(new Runnable() {
                    @Override
                    public void run() {
                        Rect delegateArea = new Rect();
                        delegate.getHitRect(delegateArea);
                        Resources res = delegate.getContext().getResources();
                        int sizeInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDp, res.getDisplayMetrics());
                        delegateArea.inset(-sizeInPixels, -sizeInPixels);
                        TouchDelegate expandedArea = new TouchDelegate(delegateArea, delegate);
                        parent.setTouchDelegate(expandedArea);
                    }
                });
            }
        }
    }

}

