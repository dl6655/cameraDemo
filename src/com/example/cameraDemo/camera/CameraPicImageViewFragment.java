package com.example.cameraDemo.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.example.cameraDemo.CameraActivity;
import com.example.cameraDemo.R;
import com.example.cameraDemo.inject.From;
import com.example.cameraDemo.inject.Injector;

public class CameraPicImageViewFragment extends Fragment {

	@From(R.id.image_item_id)
	private ImageView imageView;
	private CameraActivity.CameraPicInfo cameraPicInfo;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootview = inflater.inflate(R.layout.camera_image_item_view, container, false);
		return rootview;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Injector.inject(this);
		Bitmap bitmap = BitmapFactory.decodeFile(cameraPicInfo.path);
		imageView.setImageBitmap(bitmap);
	}
	
	public static CameraPicImageViewFragment newInstance(CameraActivity.CameraPicInfo cameraPicInfo){
		CameraPicImageViewFragment fragment = new CameraPicImageViewFragment();
		fragment.cameraPicInfo = cameraPicInfo;
		return fragment;
	}
	

}
