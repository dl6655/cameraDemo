package com.example.cameraDemo.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Created by Zhp on 2014/7/9.
 */
public class BaseStorageUtils {
    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String TAG = "StorageUtils";

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> if card is mounted and app has appropriate permission. Else -
     * Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context) {
        return getCacheDirectory(context, true);
    }

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> (if card is mounted and app has appropriate permission) or
     * on device's file system depending incoming parameters.
     *
     * @param context        Application context
     * @param preferExternal Whether prefer external location for cache
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) { // (sh)it happens (Issue #660)
            externalStorageState = "";
        }
        if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    /**
     * Returns specified application cache directory. Cache directory will be created on SD card by defined path if card
     * is mounted and app has appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context  Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "Android/data/com.yunmall.ym/cache/cachedir")
     * @return Cache {@link File directory}
     */
    public static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(getCacheDirectory(context), cacheDir);
        }
        if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    public static boolean isExternalStorageWritable(Context context) {
        if (hasExternalStoragePermission(context)) {
            String externalStorageState;
            try {
                externalStorageState = Environment.getExternalStorageState();
            } catch (NullPointerException e) { // (sh)it happens (Issue #660)
                externalStorageState = "";
            }
            return MEDIA_MOUNTED.equals(externalStorageState);
        } else {
            return false;
        }
    }

    protected static File getExternalCacheDir(Context context) {
        File appCacheDir = context.getExternalCacheDir();
        if (appCacheDir == null) {
            File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
            File cacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
            if (makeNoMedia(cacheDir)) return null;
            return cacheDir;
        } else {
            if (makeNoMedia(appCacheDir)) return null;
            return appCacheDir;
        }
    }

    protected static boolean makeNoMedia(File cacheDir) {
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                return true;
            }
            try {
                new File(cacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
            }
        }
        return false;
    }

    protected static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }
}
