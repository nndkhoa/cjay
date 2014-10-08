package com.cloudjay.cjay.activity;

import android.app.Application;
import android.graphics.Bitmap;

import com.cloudjay.cjay.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/**
 * Created by nambv on 08/10/2014.
 */
public class CJayApplication extends Application {

    private static CJayApplication instance;

    public CJayApplication() {
        instance = this;
    }

    public static CJayApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // init image loader default options
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .showImageForEmptyUri(R.drawable.ic_app_360)
                .build();

        // init image loader config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .discCacheSize(500 * 1024 * 1024)
                .memoryCache(new WeakMemoryCache())
                .threadPoolSize(3)
                .threadPriority(Thread.MAX_PRIORITY)
                .build();

        // init image loader with config defined
        ImageLoader.getInstance().init(config);
    }
}
