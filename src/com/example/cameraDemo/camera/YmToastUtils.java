package com.example.cameraDemo.camera;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by dingli on 2015-8-13.
 */
public class YmToastUtils {
    public static void showToast(Context ctx,String msg){
        Toast.makeText(ctx,msg,Toast.LENGTH_LONG).show();

    }
}
