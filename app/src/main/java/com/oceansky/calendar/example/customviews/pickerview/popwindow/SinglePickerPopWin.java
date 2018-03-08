package com.oceansky.calendar.example.customviews.pickerview.popwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.oceansky.calendar.example.customviews.pickerview.LoopListener;
import com.oceansky.calendar.example.customviews.pickerview.LoopView;
import com.oceansky.calendar.example.R;

import java.util.ArrayList;
import java.util.List;

/**
 * PopWindow for Single Pick
 */
public class SinglePickerPopWin extends PopupWindow implements OnClickListener {

    public TextView cancelBtn;
    public TextView confirmBtn;

    public LoopView selecteLoopView;

    public View pickerContainerV;
    public View contentView;//root view

    private int selectePos = 0;
    private Context mContext;

    private int btnTextsize;//text btnTextsize of cancel and confirm button
    private int viewTextSize;

    List<String> itemList = new ArrayList();// select items

    public String[] mItemArray;

    public static class Builder {

        //Required
        private Context                context;
        private String[]               itemArray;
        private OnSinglePickedListener listener;


        public Builder(Context context, String[] itemArray, OnSinglePickedListener listener) {
            this.context = context;
            this.listener = listener;
            this.itemArray = itemArray;
        }

        //Option
        private int btnTextSize  = 15;//text btnTextsize of cancel and confirm button
        private int viewTextSize = 25;
        private int selectedItem = 0;


        /**
         * set btn text btnTextSize
         *
         * @param textSize dp
         */
        public Builder btnTextSize(int textSize) {
            this.btnTextSize = textSize;
            return this;
        }

        public Builder viewTextSize(int textSize) {
            this.viewTextSize = textSize;
            return this;
        }

        public Builder selected(int position) {
            this.selectedItem = position;
            return this;
        }

        public SinglePickerPopWin build() {

            return new SinglePickerPopWin(this);
        }
    }

    public SinglePickerPopWin(Builder builder) {
        this.mContext = builder.context;
        this.mListener = builder.listener;
        this.btnTextsize = builder.btnTextSize;
        this.viewTextSize = builder.viewTextSize;
        this.mItemArray = builder.itemArray;
        setSelectedDate(builder.selectedItem);
        initView();
    }

    private void setSelectedDate(int selectedItem) {
        selectePos = selectedItem;
    }


    private OnSinglePickedListener mListener;

    private void initView() {

        contentView = LayoutInflater.from(mContext).inflate(
                R.layout.layout_single_picker, null);
        cancelBtn = (TextView) contentView.findViewById(R.id.single_tv_cancle);
        confirmBtn = (TextView) contentView.findViewById(R.id.single_tv_confirm);

        selecteLoopView = (LoopView) contentView.findViewById(R.id.picker_single);

        pickerContainerV = contentView.findViewById(R.id.container_picker);

        cancelBtn.setTextSize(btnTextsize);
        confirmBtn.setTextSize(btnTextsize);

        //do not loop,default can loop
        selecteLoopView.setNotLoop();

        //set loopview text btnTextsize
        selecteLoopView.setTextSize(viewTextSize);

        //set checked listen
        selecteLoopView.setListener(new LoopListener() {
            @Override
            public void onItemSelect(int item) {
                selectePos = item;
            }
        });

        initPickerViews(); // init year and month loop view
        initSinglePickerView();

        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
        contentView.setOnClickListener(this);

        setTouchable(true);
        setFocusable(true);
        // setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setAnimationStyle(R.style.FadeInPopWin);
        setContentView(contentView);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void initSinglePickerView() {
    }

    /**
     * Init year and month loop view,
     * Let the day loop view be handled separately
     */
    private void initPickerViews() {

        for (int i = 0; i < mItemArray.length; i++) {
            itemList.add(mItemArray[i]);
        }
        selecteLoopView.setArrayList((ArrayList) itemList);
        selecteLoopView.setInitPosition(selectePos);
    }


    /**
     * set selected item position value when initView.
     *
     * @param postion
     */
    public void setSelected(int postion) {
        selectePos = postion;
    }

    /**
     * Show date picker popWindow
     *
     * @param activity
     */
    public void showPopWin(Activity activity) {

        if (null != activity) {

            TranslateAnimation trans = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                    0, Animation.RELATIVE_TO_SELF, 1,
                    Animation.RELATIVE_TO_SELF, 0);

            showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM,
                    0, 0);
            trans.setDuration(400);
            trans.setInterpolator(new AccelerateDecelerateInterpolator());

            pickerContainerV.startAnimation(trans);
        }
    }

    /**
     * Dismiss date picker popWindow
     */
    public void dismissPopWin() {

        TranslateAnimation trans = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);

        trans.setDuration(400);
        trans.setInterpolator(new AccelerateInterpolator());
        trans.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                dismiss();
            }
        });

        pickerContainerV.startAnimation(trans);
    }

    @Override
    public void onClick(View v) {

        if (v == contentView || v == cancelBtn) {

            dismissPopWin();
        } else if (v == confirmBtn) {

            if (null != mListener) {
                mListener.onSinglePickCompleted(selectePos, mItemArray[selectePos]);
            }

            dismissPopWin();
        }
    }

    public static int spToPx(Context context, int spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    public interface OnSinglePickedListener {

        /**
         * Listener when date has been checked
         */
        void onSinglePickCompleted(int position, String selected);
    }
}
