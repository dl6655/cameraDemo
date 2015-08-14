package com.example.cameraDemo.camera;

import android.app.Application;

public class YmApp extends Application {
    private static YmApp ymApp;
    @Override
    public void onCreate() {
        super.onCreate();
        ymApp = this;
    }
    public static YmApp getInstance() {
        return ymApp;
    }
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_MODERATE) {
        }
    }
}
