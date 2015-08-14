package com.example.cameraDemo.camera;


import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;

/**
 * 主要用于camer的管理
 * @author cuijie
 *
 */
public class CameraHolder {
	
	private static CameraHolder cameraHolder;
	
	private int numOfCamera;
	
	private int user;
	
	private Camera cameraDevice;

	private CameraHolderHandler cameraholderHandler;

	private CameraInfo [] cameraInfo;

	private int cameraId;

	private Parameters parameters;

	private long keepBeforeTime = 0;

	/**
	 * 后置摄像头Id
	 */
	private int backCameraId = -1;

	/**
	 * 前置摄像头Id
	 */
	private int frontCameraId = -1;

	public static synchronized CameraHolder getInstance(){
		if(cameraHolder == null){
			cameraHolder = new CameraHolder();
		}

		return cameraHolder;
	}

	private CameraHolder(){
		HandlerThread ht = new HandlerThread("CameraHolder");
		ht.start();
		cameraholderHandler = new CameraHolderHandler(ht.getLooper());
		numOfCamera = Camera.getNumberOfCameras();
		cameraInfo = new CameraInfo[numOfCamera];
        for (int i = 0; i < numOfCamera; i++) {
        	cameraInfo[i] = new CameraInfo();
            Camera.getCameraInfo(i, cameraInfo[i]);
            if (backCameraId == -1 && cameraInfo[i].facing == CameraInfo.CAMERA_FACING_BACK) {
            	    backCameraId = i;
            }
            if (frontCameraId == -1 && cameraInfo[i].facing == CameraInfo.CAMERA_FACING_FRONT) {
                	frontCameraId = i;
            }
        }

	}

	private static final int RELEASE_CAMERA = 1;
	private class CameraHolderHandler extends Handler{

		CameraHolderHandler(Looper looper) {
            super(looper);
        }

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
            case RELEASE_CAMERA:
                synchronized (CameraHolder.this) {
                      	releaseCamera();
                }
                break;
        }
		}

	}


	public int getNumOfCamera(){
		return numOfCamera;
	}

	/**
	 * 开启照相机
	 * @param cameraId
	 * @throws CameraHardwareException
	 */
	public synchronized Camera open(int cameraId,int width,int height) throws CameraHardwareException{
		if (cameraDevice != null && this.cameraId != cameraId) {
			cameraDevice.release();
			cameraDevice = null;
            this.cameraId = -1;
        }
        if (cameraDevice == null) {
            try {
              	cameraDevice = Camera.open(cameraId);
              	this.cameraId = cameraId;
            } catch (RuntimeException e) {
                throw new CameraHardwareException(e);
            }
            parameters = cameraDevice.getParameters();
            
        } else {
            try {
            	    cameraDevice.reconnect();
            } catch (IOException e) {
//                throw new CameraHardwareException(e);
            }
            
            cameraDevice.setParameters(parameters);
        }
        
        cameraholderHandler.removeMessages(RELEASE_CAMERA);
        return cameraDevice;
	}
	
	/**
	 * release 相机
	 */
	public synchronized void release(){
        cameraDevice.stopPreview();
        releaseCamera();
	}
	
	private synchronized void releaseCamera(){
		long now = System.currentTimeMillis();
		if (now < keepBeforeTime) {
            cameraholderHandler.sendEmptyMessageDelayed(RELEASE_CAMERA,
            		keepBeforeTime - now);
            return;
        }
        cameraDevice.release();
        cameraDevice = null;
        parameters = null;
        cameraId = -1;
	}

	public int getBackCameraId() {
		return backCameraId;
	}

	public int getFrontCameraId() {
		return frontCameraId;
	}
	
	

}
