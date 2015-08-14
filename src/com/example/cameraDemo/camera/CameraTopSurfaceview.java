package com.example.cameraDemo.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraTopSurfaceview extends SurfaceView implements SurfaceHolder.Callback{

	private SurfaceHolder sh;
	private int width;
	private int height;
	public CameraTopSurfaceview(Context context,AttributeSet attrs) {
		super(context,attrs);
		sh = this.getHolder();
		sh.addCallback(this);
		sh.setFormat(PixelFormat.TRANSPARENT);
		this.setZOrderOnTop(true);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

}
