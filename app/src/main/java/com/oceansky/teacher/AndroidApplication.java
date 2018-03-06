package com.oceansky.teacher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.anupcowkur.reservoir.Reservoir;
import com.facebook.stetho.Stetho;
import com.igexin.sdk.PushService;
import com.marswin89.marsdaemon.DaemonClient;
import com.marswin89.marsdaemon.DaemonConfigurations;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.oceansky.teacher.constant.Constants;
import com.oceansky.teacher.constant.FeatureConfig;
import com.oceansky.teacher.customviews.photoSelector.listener.UILPauseOnScrollListener;
import com.oceansky.teacher.customviews.photoSelector.loader.UILImageLoader;
import com.oceansky.teacher.utils.LogHelper;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Stack;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;
import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.PauseOnScrollListener;
import cn.finalteam.galleryfinal.ThemeConfig;


public class AndroidApplication extends MultiDexApplication {
    private static final String TAG            = "Application";
    private static final String APPID          = "906dcee6855c9e6cb2cba353504c4226";

    public static  boolean            canUpdate;
    public static  boolean            isLogined;
    private static AndroidApplication mInstance;
    private        Stack<Activity>    mActivityStack;
    private        DaemonClient       mDaemonClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        mActivityStack = new Stack<>();

        CustomActivityOnCrash.install(this);
//        CustomActivityOnCrash.setErrorActivityClass(CustomErrorActivity.class);

        // enable logging
        // enable stetho
        Stetho.initializeWithDefaults(this);

        //获取测试设备信息
        String deviceInfo = getDeviceInfo(this);
        LogHelper.d(TAG, deviceInfo);

        initImageLoader();
        initPhotoSelector();
        initMobAnalytics();
        initBmobUpdate();
        initCacheUtil();
    }

    public static AndroidApplication getInstance() {
        return mInstance;
    }

    public void pushActivity(Activity activity) {
        if (!mActivityStack.contains(activity)) {
            mActivityStack.push(activity);
        }
    }

    public Stack<Activity> getActivityStack() {
        return mActivityStack;
    }

    /**
     * 初始化ImageLoader
     */
    private void initImageLoader() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.profile_photo_default)
                .showImageOnFail(R.mipmap.profile_photo_default)
                .showImageForEmptyUri(R.mipmap.profile_photo_default)
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this)
                .memoryCacheExtraOptions(480, 800)
                // default = device screen dimensions
                .threadPoolSize(3)
                // default
                .threadPriority(Thread.NORM_PRIORITY - 1)
                // default
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024).memoryCacheSizePercentage(13) // default
                .discCacheSize(50 * 1024 * 1024) // 缓冲大小
                .discCacheFileCount(100) // 缓冲文件数目
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .imageDownloader(new BaseImageDownloader(this) {
                    @Override
                    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
                        return super.getStreamFromOtherSource(imageUri, extra);
                    }
                })
                .defaultDisplayImageOptions(options) // default
                .writeDebugLogs().build();

        // 2.单例ImageLoader类的初始化
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    /**
     * 初始化友盟统计
     */
    private void initMobAnalytics() {
        /** 使用集成测试服务 */
        MobclickAgent.setDebugMode(true);
        /** 禁止默认的页面统计方式，这样将不会再自动统计Activity */
        MobclickAgent.openActivityDurationTrack(false);
        if (FeatureConfig.DEBUG_LOG) {
            /** debug时关闭错误统计功能 */
            MobclickAgent.setCatchUncaughtExceptions(false);
        }
    }

    /**
     * 初始化Bmob自动更新
     */
    private void initBmobUpdate() {
        /** Bmob自动更新SDK初始化 */
        //设置BmobConfig
        BmobConfig config = new BmobConfig.Builder()
                //请求超时时间（单位为秒）：默认15s
                .setConnectTimeout(30)
                //文件分片上传时每片的大小（单位字节），默认512*1024
                .setBlockSize(500 * 1024)
                .build();
        Bmob.getInstance().initConfig(config);
        //Bmob初始化
        Bmob.initialize(this, APPID);
    }

    /**
     * 初始化照片选择器
     */
    private void initPhotoSelector() {
        LogHelper.d(TAG, "initPhotoSelector");
        ThemeConfig themeConfig = new ThemeConfig.Builder()
                .setTitleBarBgColor(getResources().getColor(R.color.button_normal_background_color))
                .setCropControlColor(getResources().getColor(R.color.button_normal_background_color))
                .setCheckNornalColor(getResources().getColor(R.color.button_normal_background_color))//选择框未选颜色
                .setCheckSelectedColor(getResources().getColor(R.color.button_focus_background_color))//选择框选中颜色
                .setCropControlColor(getResources().getColor(R.color.button_focus_background_color))//设置裁剪控制点和裁剪框颜色
                .setFabNornalColor(getResources().getColor(R.color.button_normal_background_color))//设置Floating按钮Nornal状态颜色
                .setFabPressedColor(getResources().getColor(R.color.button_focus_background_color))//设置Floating按钮Pressed状态颜色
                .build();

        FunctionConfig.Builder functionConfigBuilder = new FunctionConfig.Builder();
        functionConfigBuilder.setEnableEdit(true)
                .setEnableCrop(true)
                .setCropReplaceSource(false)
                .setForceCrop(true);

        //functionConfigBuilder.setSelected(mPhotoList);//添加过滤集合
        FunctionConfig functionConfig = functionConfigBuilder.build();

        cn.finalteam.galleryfinal.ImageLoader imageLoader = new UILImageLoader();
        PauseOnScrollListener pauseOnScrollListener = new UILPauseOnScrollListener(false, true);
        CoreConfig coreConfig = new CoreConfig.Builder(this, imageLoader, themeConfig)
                .setFunctionConfig(functionConfig)
                .setPauseOnScrollListener(pauseOnScrollListener)
                .setNoAnimcation(true)
                .build();

        GalleryFinal.init(coreConfig);
    }

    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps != null) {
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    return procInfo.processName;
                }
            }
        }
        return null;
    }

    /**
     * 友盟统计 获取测试设备的信息
     */
    @SuppressLint("NewApi")
    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        } else {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        }
        return result;
    }

    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = null;
            if (checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                device_id = tm.getDeviceId();
            }
            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);
            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }
            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
            }
            json.put("device_id", device_id);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化缓存工具
     */
    private void initCacheUtil() {
        try {
            Reservoir.init(this, Constants.CACHE_MAX_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        mDaemonClient = new DaemonClient(createDaemonConfigurations());
//        mDaemonClient.onAttachBaseContext(base);
    }

//    protected DaemonConfigurations createDaemonConfigurations() {
//        DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
//                "com.oceansky.teacher:pushservice",
//                PushService.class.getCanonicalName(),
//                Receiver1.class.getCanonicalName());
//
//        DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
//                "com.oceansky.teacher:process2",
//                Service2.class.getCanonicalName(),
//                Receiver2.class.getCanonicalName());
//
//        DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
//        //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
//        return new DaemonConfigurations(configuration1, configuration2, listener);
//    }


//    class MyDaemonListener implements DaemonConfigurations.DaemonListener {
//        @Override
//        public void onPersistentStart(Context context) {
//        }
//
//        @Override
//        public void onDaemonAssistantStart(Context context) {
//        }
//
//        @Override
//        public void onWatchDaemonDaed() {
//        }
//    }
}
