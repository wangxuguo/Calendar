package com.oceansky.teacher.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.letv.skin.v4.V4PlaySkin;
import com.letv.universal.iplay.ISplayer;
import com.oceansky.teacher.R;
import com.oceansky.teacher.utils.LetvParamsUtils;
import com.oceansky.teacher.utils.LetvSimplePlayBoard;
import com.oceansky.teacher.utils.ToastUtil;


public class PlayActivity extends Activity {
    public final static String DATA = "data";

    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    private V4PlaySkin skin;
    private LetvSimplePlayBoard playBoard;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        loadDataFromIntent();// load data
        skin = (V4PlaySkin) findViewById(R.id.videobody);
        playBoard = new LetvSimplePlayBoard();
        playBoard.init(this, bundle, skin);

        initBtn();
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            bundle = intent.getBundleExtra("data");
            if (bundle == null) {
                ToastUtil.showToastBottom(this, "no data", Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playBoard != null) {
            playBoard.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playBoard != null) {
            playBoard.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playBoard != null) {
            playBoard.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (playBoard != null) {
            playBoard.onConfigurationChanged(newConfig);
        }
    }

    private void initBtn() {
        findViewById(R.id.btn2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playBoard != null && v.isShown()) {
                    ISplayer player = playBoard.getPlayer();
                    if (player != null) {
                        player.stop();
                        player.reset();
                        String uuid = "";
                        String vuid = "";
                        uuid = "2b686d84e3";
                        vuid = "15d2678091";
                        player.setParameter(player.getPlayerId(), LetvParamsUtils.setVodParams(uuid, vuid, "", "151398", ""));
                        player.prepareAsync();
                    }
                }
            }
        });
    }
}
