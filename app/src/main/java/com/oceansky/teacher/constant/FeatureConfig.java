package com.oceansky.teacher.constant;

import com.oceansky.teacher.BuildConfig;

public class FeatureConfig {

    // Global control on debug log
    public static final boolean DEBUG_LOG = BuildConfig.LOG_DEBUG;

    public static final String API_HOST_NAME = BuildConfig.BASE_API;
    public static final String BASE_URL      = BuildConfig.BASE_URL;
    public static final String BUCKET        = BuildConfig.BUCKET;
}
