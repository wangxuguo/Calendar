package com.oceansky.teacher.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by dengfa on 16/5/28.
 */
public class ImageUtils {
    /**
     * 加载图片
     *
     * @param imagePath
     * @param ivPhoto
     * @param defaultImageId 默认图片的resourceId
     */
    public static void  loadImage(String imagePath, ImageView ivPhoto, int defaultImageId) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultImageId)
                .showImageOnFail(defaultImageId)
                .showImageForEmptyUri(defaultImageId)
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoader.getInstance().displayImage(imagePath, ivPhoto, options);
    }
}
