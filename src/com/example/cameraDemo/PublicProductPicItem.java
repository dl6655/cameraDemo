package com.example.cameraDemo;

import android.graphics.Bitmap;

import java.io.Serializable;

public class PublicProductPicItem implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	public String path;
	public Bitmap bitmap;
	public byte [] data;
//	public boolean isMainPic;
//	public int index;
	public int fromWhere;  //1 相机 2 相册  3网络
	public String url = "";//
	public String token = "";
	
	public String compressPath = "";//这个属性是代表从相册中用户选择了分辨率较大的图片 把图片压缩后存储的路径 这个会在完成发布或者退出发布后统一删除
	public boolean isNeedComp = false;
	public int degree;
	public transient boolean hasCompared = false;
	
	/**
	 * 照片的实际长宽
	 */
	public int picWidth;
	public int picHeight;
	
	public int picCompressWidth;
	public int picCompressHeight;
	
	public int uploadState = -1;//0正在上传 1 上传成功  2 上传失败
	
	public int isNew;//表示是否是新添加的
	
	public final static int FROM_ALBUM = 2;
	public final static int FROM_CAMERA = 1;
	public final static int FROM_NETWORK = 3;
	
	public final static int STATE_UPLOAD_INIT = -1;
	public final static int STATE_UPLOAD_ING = 0;
	public final static int STATE_UPLOAD_SUCC = 1;
	public final static int STATE_UPLOAD_FAILE = 2;

}
