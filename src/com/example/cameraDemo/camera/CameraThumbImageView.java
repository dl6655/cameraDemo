package com.example.cameraDemo.camera;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.example.cameraDemo.R;
import com.example.cameraDemo.inject.From;
import com.example.cameraDemo.inject.Injector;

/**
 * 照相机下方的预览图
 * @author cuijie
 */
@SuppressLint("ViewConstructor")
public class CameraThumbImageView extends RelativeLayout {

	@From(R.id.camera_thumb_id)
	private ImageView thumbImageView;
	@From(R.id.camera_thumb_del_id)
	private ImageView delImageView;
	private final Bitmap bitmap;
	private final ThumbListener thumbListener;
	private int index;
	public CameraThumbImageView(Context context,int width,int height,Bitmap bitmap,ThumbListener thumbListener) {
		super(context);
		View rootView  = LinearLayout.inflate(context, R.layout.camera_thumb_imageview, null);
		Injector.inject(this, rootView, false);
		thumbImageView = (ImageView)rootView.findViewById(R.id.camera_thumb_id);
		delImageView = (ImageView)rootView.findViewById(R.id.camera_thumb_del_id);
		LayoutParams thumbImageParam = new LayoutParams(width, height);
		thumbImageView.setLayoutParams(thumbImageParam);
		this.setLayoutParams(thumbImageParam);
		this.addView(rootView);
		this.bitmap = bitmap;
		thumbImageView.setImageBitmap(bitmap);
		this.thumbListener = thumbListener;
		delImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CameraThumbImageView.this.thumbListener.del(CameraThumbImageView.this.bitmap,CameraThumbImageView.this,index);
			}
		});
		thumbImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				int index = CameraPicManager.getInstance().getThumbBitmapList().indexOf(CameraThumbImageView.this.bitmap);
				CameraThumbImageView.this.thumbListener.click(CameraThumbImageView.this,index);
			}
		});
		
		
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	
	public interface ThumbListener{
		public void del(Bitmap bitmap, CameraThumbImageView cameraThumbImageView, int index);
		public void click(CameraThumbImageView cameraThumbImageView, int index);
		
	}

}
