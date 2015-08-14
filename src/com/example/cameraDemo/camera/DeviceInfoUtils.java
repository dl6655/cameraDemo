package com.example.cameraDemo.camera;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

/**
 * 设备属性工具类
 * Created by Zhp on 2014/5/20.
 */
public class DeviceInfoUtils {

    public static String getClientId(Context context) {
        return getDeviceId(context) + "_" + System.currentTimeMillis();
    }

    /**
     * imei + "_" + macAddress + "_" + PackageName
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        String imei = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        if (imei == null) {
            imei = "";
        }
        String macAddress = getLocalMacAddress(context);
        if (macAddress == null) {
            macAddress = "";
        }

        return imei + "_" + macAddress + "_" + getPackageName(context);
    }

    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    public static String getDeviceName() {
        return Build.DEVICE;
    }

    public static String getOsName() {
        return Build.MODEL + "," + Build.VERSION.SDK_INT + "," + Build.VERSION.RELEASE;
    }

    public static String getVersionName(Context context) {
        PackageInfo packInfo = getPackageInfo(context);

        String name = "";
        if (packInfo != null) {
            name = packInfo.versionName;
        }
        return name;
    }

    public static int getVersionCode(Context context) {
        PackageInfo packInfo = getPackageInfo(context);

        int code = 0;
        if (packInfo != null) {
            code = packInfo.versionCode;
        }
        return code;
    }

    public static String getPackageName(Context context) {
        PackageInfo packInfo = getPackageInfo(context);

        String packageName = "";
        if (packInfo != null) {
            packageName = packInfo.packageName;
        }
        return packageName;
    }

    public static PackageInfo getPackageInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packInfo;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static float getScreenDensity(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.density;
    }

    public static float getScreenDensityDpi(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.densityDpi;
    }

    public static float getScreenScaledDensity(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.scaledDensity;
    }

    public static String getMetaData(Context context, String key) {
        Bundle metaData = null;
        String value = null;
        if (context == null || key == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                value = metaData.getString(key);
            }
        } catch (Exception e) {

        }
        return value;
    }
    
    /** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     * @param context
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
