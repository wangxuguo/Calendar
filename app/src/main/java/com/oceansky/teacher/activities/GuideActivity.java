package com.oceansky.teacher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.oceansky.teacher.R;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.utils.SharePreferenceUtils;
import com.oceansky.teacher.utils.StringUtils;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.IndicatorViewPager.IndicatorPagerAdapter;
import com.shizhefei.view.indicator.IndicatorViewPager.IndicatorViewPagerAdapter;

public class GuideActivity extends FragmentActivity {
    private static final int[] GUIDE_IMAGES = {R.drawable.guide_1, R.drawable.guide_2, R.drawable.guide_3};

    private IndicatorViewPager indicatorViewPager;
    private LayoutInflater     inflate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentVersionCode = StringUtils.getVersionCode(this);
        int versionCode = SharePreferenceUtils.getIntPref(this, Constants.KEY_GUID_SHOW, 0);
        if (currentVersionCode > versionCode) {
            setContentView(R.layout.activity_guide);
            ViewPager viewPager = (ViewPager) findViewById(R.id.guide_viewPager);
            Indicator indicator = (Indicator) findViewById(R.id.guide_indicator);
            indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
            inflate = LayoutInflater.from(getApplicationContext());
            indicatorViewPager.setAdapter(adapter);
        } else {
            startActivity(new Intent(this, TabMainActivity.class));
            finish();
        }

    }

    private IndicatorPagerAdapter adapter = new IndicatorViewPagerAdapter() {
        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = inflate.inflate(R.layout.tab_guide, container, false);
            }
            return convertView;
        }

        @Override
        public View getViewForPage(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = inflate.inflate(R.layout.activity_welcome, container, false);
            }
            ImageView img = (ImageView) convertView.findViewById(R.id.img);
            if (position == getCount() - 1) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharePreferenceUtils.setIntPref(GuideActivity.this, Constants.KEY_GUID_SHOW,
                                StringUtils.getVersionCode(GuideActivity.this));
                        startActivity(new Intent(GuideActivity.this, TabMainActivity.class));
                        finish();
                    }
                });
            } else {
                convertView.setOnClickListener(null);
            }
            img.setImageResource(GUIDE_IMAGES[position]);
            return convertView;
        }

        @Override
        public int getCount() {
            return GUIDE_IMAGES.length;
        }
    };
}
