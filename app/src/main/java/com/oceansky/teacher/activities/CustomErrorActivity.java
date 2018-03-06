package com.oceansky.teacher.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.utils.FileHelper;
import com.oceansky.teacher.utils.LogHelper;
import com.oceansky.teacher.utils.StorageUtils;
import com.oceansky.teacher.utils.ToastUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;

public class CustomErrorActivity extends Activity {
    private static final String TAG = "MyCrashHandler";

    private static final String CRASH_LOG_FILENAME = "crash.trace";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToastUtil.showToastBottom(this, "发生错误,应用关闭", Toast.LENGTH_SHORT);
        String errorStr = CustomActivityOnCrash.getAllErrorDetailsFromIntent(this, getIntent());
        dumpCrashToSD(errorStr);
        finish();
    }

    private void dumpCrashToSD(String error) {
        if (!StorageUtils.externalStorageAvailable()) {
            return; // no SD card available
        }

        File crashLog = FileHelper.getSafeExternalFile(Constants.EXTERNAL_PATH_LOGS,
                CRASH_LOG_FILENAME);
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(crashLog)));
            pw.println();
            pw.write(error);
            pw.close();
        } catch (Exception e) {
            LogHelper.w(TAG, "cannot dump crash logs: " + e);
        }
    }

}
