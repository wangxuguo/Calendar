package com.oceansky.calendar.example.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.oceansky.calendar.example.network.response.MessageEntity;
import com.oceansky.calendar.example.utils.DisplayUtils;
import com.oceansky.calendar.example.R;
import com.oceansky.calendar.example.utils.LogHelper;

import java.util.ArrayList;

/**
 * User: 王旭国
 * Date: 16/6/16 21:19
 * Email:wangxuguo@jhyx.com.cn
 */
public class MessageAdapter extends BaseAdapter<MessageEntity> {
    private static final String MSG_DATETIME_FOMATOR = "MM/dd hh:mm";
    private static final String TAG                  = MessageAdapter.class.getSimpleName();
    private ListView listView;

    public MessageAdapter(Context mContext, ListView listView, ArrayList mListDatas) {
        super(mContext, mListDatas);
        this.mContext = mContext;
        this.listView = listView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LogHelper.d(TAG, "mDatas.get: position: )" + mDatas.get(position));
        MessageEntity data = mDatas.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_message, null);
            holder.message_title = (TextView) convertView.findViewById(R.id.message_title);
            holder.message_time = (TextView) convertView.findViewById(R.id.message_time);
            holder.message_content = (TextView) convertView.findViewById(R.id.message_content);
            holder.message_haveread_dot = convertView.findViewById(R.id.message_haveread_dot);
            holder.message_content_arrow = (ImageView) convertView.findViewById(R.id.message_content_arrow);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (data != null) {
            holder.message_title.setText(data.getTitle());
            LogHelper.d(TAG, "date.timestamp: " + data.getTime());
            String date = new java.text.SimpleDateFormat("MM/dd HH:mm").format(new java.util.Date((long) data.getTime() * 1000));

            holder.message_time.setText(date);
//            holder.message_time.setText(new DateTime(data.getTime()*1000).toString(MSG_DATETIME_FOMATOR));
            holder.message_content.setText(data.getText());
/*******处理标点换行问题******************************************/
            holder.message_content.getViewTreeObserver().addOnPreDrawListener(new OnTVPreDrawListener(holder.message_content));
            holder.message_content.getViewTreeObserver().addOnGlobalLayoutListener(new OnTvGlobalLayoutListener(holder.message_content));
/***************************************************************/
            final TextView tv = holder.message_content;
            final ImageView iv = holder.message_content_arrow;
            Paint paint = new Paint();
            paint.setTextSize(holder.message_content.getTextSize());
            int testlenght = (int) paint.measureText(data.getText());
            int textwidth = getTextWidth(paint, data.getText());

            Resources resources = mContext.getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            int width3 = dm.widthPixels;
            float measureCount = testlenght / (width3 - DisplayUtils.dip2px(mContext, 52));
            int linecount = tv.getLineCount();
            TextView textView = new TextView(mContext);
            textView.setText(data.getText());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.message_content.getTextSize());
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width3 - DisplayUtils.dip2px(mContext, 32), View.MeasureSpec.AT_MOST);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            textView.measure(widthMeasureSpec, heightMeasureSpec);
            LogHelper.d(TAG, "text.getMeasureHeight: " + textView.getMeasuredHeight() + " text.getlinecount:  " + textView.getLineCount() + " lineHeight:" + textView.getLineHeight());
            final int lineCount = textView.getLineCount();
            LogHelper.d(TAG, "linecount: " + linecount + " measureCount: " + measureCount + " testlenght: " + testlenght + " textwidth: " + textwidth);
            if (lineCount > 2) {
                holder.message_content_arrow.setVisibility(View.VISIBLE);
//                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.message_content.getLayoutParams();
//                layoutParams.rightMargin = DisplayUtils.dip2px(mContext,20);
//                holder.message_content.setLayoutParams(layoutParams);
                holder.message_content.setMaxLines(2);
//                holder.message_content.invalidate();
//                linecount = tv.getLineCount();

            } else {
                holder.message_content_arrow.setVisibility(View.GONE);
//                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.message_content.getLayoutParams();
//                layoutParams.rightMargin = DisplayUtils.dip2px(mContext,0);
//                holder.message_content.setLayoutParams(layoutParams);
//                holder.message_content.setMaxLines(2);
//                holder.message_content.invalidate();
            }
            holder.message_content_arrow.setOnClickListener(new View.OnClickListener() {
                boolean flag = true;

                @Override
                public void onClick(View v) {
                    if (flag) {
                        flag = false;
                        tv.setMaxLines(Integer.MAX_VALUE);
//                        tv.setEllipsize(null); // 展开
                        iv.setImageResource(R.mipmap.icon_message_content_arrow_up);
                        if (position == getCount() - 1) {  //最后一个优化，点击之后要显示全部的内容
                            if (listView != null) {
                                listView.setSelection(position);
                            }
                        }
                    } else {
                        flag = true;
                        tv.setMaxLines(2);
//                        tv.setEllipsize(TextUtils.TruncateAt.END); // 收缩
                        iv.setImageResource(R.mipmap.icon_message_content_arrow_down);
                    }

                }
            });
            if (data.getIs_read() == 0) {
                holder.message_haveread_dot.setVisibility(View.VISIBLE);
            } else {
                holder.message_haveread_dot.setVisibility(View.GONE);
            }

        }
//        holder.title.setText(data.getTitle());
        return convertView;
    }

    public static int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    private int measureTextViewHeight(Context context, String text, int textSize, int deviceWidth) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }


    public class ViewHolder {
        public TextView  message_title;
        public TextView  message_time;
        public TextView  message_content;
        public ImageView message_content_arrow;
        public View      message_haveread_dot;
    }

    private class OnTVPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        private final TextView mText;
        public OnTVPreDrawListener(TextView textview) {
            this.mText = textview;
        }

        @Override
        public boolean onPreDraw() {
            final String newText = autoSplitText(mText);
            if (!TextUtils.isEmpty(newText)) {
                mText.setText(newText);
            }
            return true;
        }
        private String autoSplitText(final TextView tv) {
            final String rawText = tv.getText().toString(); //原始文本
            final Paint tvPaint = tv.getPaint(); //paint，包含字体等信息
            final float tvWidth = tv.getWidth() - tv.getPaddingLeft() - tv.getPaddingRight(); //控件可用宽度
            //将原始文本按行拆分
            String[] rawTextLines = rawText.replaceAll("\r", "").split("\n");
            StringBuilder sbNewText = new StringBuilder();
            for (String rawTextLine : rawTextLines) {
                if (tvPaint.measureText(rawTextLine) <= tvWidth) {
                    //如果整行宽度在控件可用宽度之内，就不处理了
                    sbNewText.append(rawTextLine);
                } else {
                    //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                    float lineWidth = 0;
                    for (int cnt = 0; cnt != rawTextLine.length(); ++cnt) {
                        char ch = rawTextLine.charAt(cnt);
                        lineWidth += tvPaint.measureText(String.valueOf(ch));
                        if (lineWidth <= tvWidth) {
                            sbNewText.append(ch);
                        } else {
                            sbNewText.append("\n");
                            lineWidth = 0;
                            --cnt;
                        }
                    }
                }
                sbNewText.append("\n");
            }
            //把结尾多余的\n去掉
            if (!rawText.endsWith("\n")) {
                sbNewText.deleteCharAt(sbNewText.length() - 1);
            }
            return sbNewText.toString();
        }
    }

    private class OnTvGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        private final TextView mText;

        public OnTvGlobalLayoutListener(TextView textView) {
            this.mText = textView;
        }

        @Override
        public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } else {
            }
            final String newText = autoSplitText(mText);
            if (!TextUtils.isEmpty(newText)) {
                mText.setText(newText);
            }
        }

        private String autoSplitText(final TextView tv) {
            final String rawText = tv.getText().toString(); //原始文本
            final Paint tvPaint = tv.getPaint(); //paint，包含字体等信息
            final float tvWidth = tv.getWidth() - tv.getPaddingLeft() - tv.getPaddingRight(); //控件可用宽度
            //将原始文本按行拆分
            String[] rawTextLines = rawText.replaceAll("\r", "").split("\n");
            StringBuilder sbNewText = new StringBuilder();
            for (String rawTextLine : rawTextLines) {
                if (tvPaint.measureText(rawTextLine) <= tvWidth) {
                    //如果整行宽度在控件可用宽度之内，就不处理了
                    sbNewText.append(rawTextLine);
                } else {
                    //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                    float lineWidth = 0;
                    for (int cnt = 0; cnt != rawTextLine.length(); ++cnt) {
                        char ch = rawTextLine.charAt(cnt);
                        lineWidth += tvPaint.measureText(String.valueOf(ch));
                        if (lineWidth <= tvWidth) {
                            sbNewText.append(ch);
                        } else {
                            sbNewText.append("\n");
                            lineWidth = 0;
                            --cnt;
                        }
                    }
                }
                sbNewText.append("\n");
            }
            //把结尾多余的\n去掉
            if (!rawText.endsWith("\n")) {
                sbNewText.deleteCharAt(sbNewText.length() - 1);
            }
            return sbNewText.toString();
        }

        ///  实现悬挂缩进
        private String autoSplitText(final TextView tv, final String indent) {
            final String rawText = tv.getText().toString(); //原始文本
            final Paint tvPaint = tv.getPaint(); //paint，包含字体等信息
            final float tvWidth = tv.getWidth() - tv.getPaddingLeft() - tv.getPaddingRight(); //控件可用宽度

            //将缩进处理成空格
            String indentSpace = "";
            float indentWidth = 0;
            if (!TextUtils.isEmpty(indent)) {
                float rawIndentWidth = tvPaint.measureText(indent);
                if (rawIndentWidth < tvWidth) {
                    while ((indentWidth = tvPaint.measureText(indentSpace)) < rawIndentWidth) {
                        indentSpace += " ";
                    }
                }
            }

            //将原始文本按行拆分
            String[] rawTextLines = rawText.replaceAll("\r", "").split("\n");
            StringBuilder sbNewText = new StringBuilder();
            for (String rawTextLine : rawTextLines) {
                if (tvPaint.measureText(rawTextLine) <= tvWidth) {
                    //如果整行宽度在控件可用宽度之内，就不处理了
                    sbNewText.append(rawTextLine);
                } else {
                    //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                    float lineWidth = 0;
                    for (int cnt = 0; cnt != rawTextLine.length(); ++cnt) {
                        char ch = rawTextLine.charAt(cnt);
                        //从手动换行的第二行开始，加上悬挂缩进
                        if (lineWidth < 0.1f && cnt != 0) {
                            sbNewText.append(indentSpace);
                            lineWidth += indentWidth;
                        }
                        lineWidth += tvPaint.measureText(String.valueOf(ch));
                        if (lineWidth <= tvWidth) {
                            sbNewText.append(ch);
                        } else {
                            sbNewText.append("\n");
                            lineWidth = 0;
                            --cnt;
                        }
                    }
                }
                sbNewText.append("\n");
            }

            //把结尾多余的\n去掉
            if (!rawText.endsWith("\n")) {
                sbNewText.deleteCharAt(sbNewText.length() - 1);
            }

            return sbNewText.toString();
        }
    }
}