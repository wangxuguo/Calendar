package com.oceansky.calendar.example.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import java.util.List;

public class SystemUtils {
    private static final String TAG = "SystemUtils";

    /**
     * 判断应用是否已经启动
     *
     * @param context     一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processInfos.size(); i++) {
            if (processInfos.get(i).processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isRunningBack(Context context) {
        String packageName = context.getPackageName();
        String topActivityName = getTopActivityName(context);
        if (packageName != null && topActivityName != null && topActivityName.startsWith(packageName)) {
            LogHelper.d(TAG, "topActivityName: " + topActivityName);
            return false;
        }
        return true;
    }


    public static String getTopActivityName(Context context) {
        String topActivityName = null;
        ActivityManager manager = (ActivityManager) (context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            ComponentName topActivity = runningTaskInfos.get(0).topActivity;
            topActivityName = topActivity.getClassName();
        }
        return topActivityName;
    }
}
