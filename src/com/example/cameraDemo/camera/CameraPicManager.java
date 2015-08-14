package com.example.cameraDemo.camera;


import android.graphics.Bitmap;
import com.example.cameraDemo.CameraActivity;

import java.util.ArrayList;
import java.util.List;


public class CameraPicManager {

	private List<CameraActivity.CameraPicInfo> cameraPicInfoList;
	
	private List<Bitmap> thumbBitmaps = new ArrayList<Bitmap>();

	private static CameraPicManager instance;
	
	private CameraPicManager(){}
	
	public static synchronized CameraPicManager getInstance(){
		if(instance == null){
			instance = new CameraPicManager();
		}
		return instance;
	}

//	public static class CameraPicInfo{
//		public int width;
//		public int height;
//		public byte[] data;
//		public Bitmap bitmap;
//		public String path;
//		public boolean isCroped;
//		public int picWidth;
//		public int picHeight;
//
//	}
	public synchronized void addCameraPicInfo(CameraActivity.CameraPicInfo cameraPicInfo){
		if(cameraPicInfoList == null){
			cameraPicInfoList = new ArrayList<CameraActivity.CameraPicInfo>();
		}
		cameraPicInfoList.add(cameraPicInfo);
	}
	
	public synchronized void removeCameraPicInfo(CameraActivity.CameraPicInfo cameraPicInfo){
		if(cameraPicInfoList != null  && cameraPicInfo != null){
			cameraPicInfoList.remove(cameraPicInfo);
		}
		
	}
	
	public synchronized void addThumbBitmaps(Bitmap bitmap){
		if(thumbBitmaps == null){
			thumbBitmaps = new ArrayList<Bitmap>();
		}
		thumbBitmaps.add(bitmap);
	}
	
	public synchronized void removeThumbBitmaps(Bitmap bitmap){
		thumbBitmaps.remove(bitmap);
	}
	
	public synchronized List<CameraActivity.CameraPicInfo> getCameraPicInfoList(){
		return cameraPicInfoList;
	}
	
	public synchronized List<Bitmap> getThumbBitmapList(){
		return thumbBitmaps;
	}
	
	public synchronized void init(){
		if(cameraPicInfoList != null){
			cameraPicInfoList.clear();
		}else{
		    cameraPicInfoList = new ArrayList<CameraActivity.CameraPicInfo>();
		}
		if(thumbBitmaps != null){
			thumbBitmaps.clear();
		}else{
		    thumbBitmaps = new ArrayList<Bitmap>();
		}
	}
}
